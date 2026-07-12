package dino.jdbx.core.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dino.jdbx.core.api.ConnectionConfig;
import dino.jdbx.core.history.LocalDateTimeAdapter;
import dino.jdbx.core.security.PasswordCipher;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists connection configs to {@code ~/.jdbx/connections.json}.
 * Passwords are encrypted at rest via {@link PasswordCipher}.
 */
public class ConnectionStore {

    private static final String DEFAULT_FILE = System.getProperty("user.home")
            + File.separator + ".jdbx" + File.separator + "connections.json";

    private final Path storePath;
    private final PasswordCipher cipher;
    private final Gson gson;

    public ConnectionStore() {
        this(Paths.get(DEFAULT_FILE), new PasswordCipher());
    }

    public ConnectionStore(Path storePath, PasswordCipher cipher) {
        this.storePath = storePath;
        this.cipher = cipher;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    public List<ConnectionConfig> load() {
        try {
            if (!Files.exists(storePath)) {
                return new ArrayList<>();
            }
            String json = Files.readString(storePath);
            ConnectionsFile file = gson.fromJson(json, ConnectionsFile.class);
            if (file == null || file.connections == null) {
                return new ArrayList<>();
            }
            List<ConnectionConfig> result = new ArrayList<>();
            for (ConnectionConfig config : file.connections) {
                if (config.getPassword() != null) {
                    config.setPassword(cipher.decrypt(config.getPassword()));
                }
                if (config.getSshPassword() != null) {
                    config.setSshPassword(cipher.decrypt(config.getSshPassword()));
                }
                result.add(config);
            }
            return result;
        } catch (Exception e) {
            System.err.println("加载连接配置失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void save(List<ConnectionConfig> configs) {
        try {
            Path parent = storePath.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            List<ConnectionConfig> toWrite = new ArrayList<>();
            for (ConnectionConfig src : configs) {
                ConnectionConfig copy = copyConfig(src);
                if (copy.getPassword() != null && !copy.getPassword().isEmpty()) {
                    copy.setPassword(cipher.encrypt(copy.getPassword()));
                }
                if (copy.getSshPassword() != null && !copy.getSshPassword().isEmpty()) {
                    copy.setSshPassword(cipher.encrypt(copy.getSshPassword()));
                }
                toWrite.add(copy);
            }

            ConnectionsFile file = new ConnectionsFile();
            file.version = 1;
            file.connections = toWrite;
            Files.writeString(storePath, gson.toJson(file));
        } catch (Exception e) {
            throw new IllegalStateException("保存连接配置失败: " + e.getMessage(), e);
        }
    }

    private static ConnectionConfig copyConfig(ConnectionConfig src) {
        ConnectionConfig copy = new ConnectionConfig();
        copy.setId(src.getId());
        copy.setName(src.getName());
        copy.setType(src.getType());
        copy.setHost(src.getHost());
        copy.setPort(src.getPort());
        copy.setDatabase(src.getDatabase());
        copy.setUsername(src.getUsername());
        copy.setPassword(src.getPassword());
        copy.setColor(src.getColor());
        copy.setCreatedAt(src.getCreatedAt());
        copy.setUpdatedAt(src.getUpdatedAt());
        copy.setUseSsl(src.isUseSsl());
        copy.setUseSsh(src.isUseSsh());
        copy.setSshHost(src.getSshHost());
        copy.setSshPort(src.getSshPort());
        copy.setSshUsername(src.getSshUsername());
        copy.setSshPassword(src.getSshPassword());
        return copy;
    }

    public static final class ConnectionsFile {
        public int version;
        public List<ConnectionConfig> connections;
    }
}
