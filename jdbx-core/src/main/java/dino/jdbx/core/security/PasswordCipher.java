package dino.jdbx.core.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-GCM password encryption for connection secrets at rest.
 * Key material lives in {@code ~/.jdbx/secret.key}.
 */
public final class PasswordCipher {

    private static final String KEY_FILE = System.getProperty("user.home")
            + File.separator + ".jdbx" + File.separator + "secret.key";
    private static final String PREFIX = "enc:v1:";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecretKey secretKey;

    public PasswordCipher() {
        this.secretKey = loadOrCreateKey();
    }

    /** Package-visible for tests with custom key path. */
    PasswordCipher(Path keyPath) {
        this.secretKey = loadOrCreateKey(keyPath);
    }

    public String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return plain;
        }
        if (isEncrypted(plain)) {
            return plain;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] cipherText = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
            buffer.put(iv);
            buffer.put(cipherText);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new IllegalStateException("加密密码失败", e);
        }
    }

    public String decrypt(String value) {
        if (value == null || value.isEmpty() || !isEncrypted(value)) {
            return value;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("解密密码失败", e);
        }
    }

    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private static SecretKey loadOrCreateKey() {
        return loadOrCreateKey(Paths.get(KEY_FILE));
    }

    private static SecretKey loadOrCreateKey(Path keyPath) {
        try {
            Path parent = keyPath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }
            if (Files.exists(keyPath)) {
                byte[] raw = Files.readAllBytes(keyPath);
                return new SecretKeySpec(raw, "AES");
            }
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(128);
            SecretKey key = keyGen.generateKey();
            Files.write(keyPath, key.getEncoded());
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("无法初始化密钥文件: " + keyPath, e);
        }
    }
}
