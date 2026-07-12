package dino.jdbx.core.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PasswordCipherTest {

    @TempDir
    Path tempDir;

    @Test
    void roundTripEncryptDecrypt() throws Exception {
        Path keyPath = tempDir.resolve("secret.key");
        PasswordCipher cipher = createWithKey(keyPath);

        String encrypted = cipher.encrypt("s3cret!");
        assertTrue(PasswordCipher.isEncrypted(encrypted));
        assertEquals("s3cret!", cipher.decrypt(encrypted));
    }

    @Test
    void encryptIsIdempotentOnAlreadyEncrypted() throws Exception {
        Path keyPath = tempDir.resolve("secret.key");
        PasswordCipher cipher = createWithKey(keyPath);

        String once = cipher.encrypt("abc");
        String twice = cipher.encrypt(once);
        assertEquals(once, twice);
    }

    private static PasswordCipher createWithKey(Path keyPath) throws Exception {
        Constructor<PasswordCipher> ctor = PasswordCipher.class.getDeclaredConstructor(Path.class);
        ctor.setAccessible(true);
        return ctor.newInstance(keyPath);
    }
}
