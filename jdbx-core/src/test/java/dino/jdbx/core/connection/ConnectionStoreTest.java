package dino.jdbx.core.connection;

import dino.jdbx.core.api.ConnectionConfig;
import dino.jdbx.core.security.PasswordCipher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void saveAndLoadPreservesFieldsAndDecryptsPassword() throws Exception {
        Path storePath = tempDir.resolve("connections.json");
        Path keyPath = tempDir.resolve("secret.key");
        PasswordCipher cipher = createCipher(keyPath);
        ConnectionStore store = new ConnectionStore(storePath, cipher);

        ConnectionConfig config = new ConnectionConfig("Local", "sqlite", "", 0, "test.db", "", "plain-pass");
        config.setColor("#0078D4");
        store.save(List.of(config));

        String raw = java.nio.file.Files.readString(storePath);
        assertTrue(raw.contains("enc:v1:"));
        assertFalse(raw.contains("plain-pass"));

        List<ConnectionConfig> loaded = store.load();
        assertEquals(1, loaded.size());
        assertEquals("Local", loaded.get(0).getName());
        assertEquals("sqlite", loaded.get(0).getType());
        assertEquals("plain-pass", loaded.get(0).getPassword());
        assertEquals("#0078D4", loaded.get(0).getColor());
    }

    private static PasswordCipher createCipher(Path keyPath) throws Exception {
        Constructor<PasswordCipher> ctor = PasswordCipher.class.getDeclaredConstructor(Path.class);
        ctor.setAccessible(true);
        return ctor.newInstance(keyPath);
    }
}
