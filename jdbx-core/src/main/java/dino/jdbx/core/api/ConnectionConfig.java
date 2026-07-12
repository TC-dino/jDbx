package dino.jdbx.core.api;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 连接配置类
 */
public class ConnectionConfig {

    private String id;
    private String name;
    private String type; // mysql, postgresql, sqlite, redis, mongodb, elasticsearch
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String color;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean useSsl;
    private boolean useSsh;
    private String sshHost;
    private int sshPort;
    private String sshUsername;
    private String sshPassword;

    public ConnectionConfig() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public ConnectionConfig(String name, String type, String host, int port, String database, String username, String password) {
        this();
        this.name = name;
        this.type = type;
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getDatabase() { return database; }
    public void setDatabase(String database) { this.database = database; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isUseSsl() { return useSsl; }
    public void setUseSsl(boolean useSsl) { this.useSsl = useSsl; }

    public boolean isUseSsh() { return useSsh; }
    public void setUseSsh(boolean useSsh) { this.useSsh = useSsh; }

    public String getSshHost() { return sshHost; }
    public void setSshHost(String sshHost) { this.sshHost = sshHost; }

    public int getSshPort() { return sshPort; }
    public void setSshPort(int sshPort) { this.sshPort = sshPort; }

    public String getSshUsername() { return sshUsername; }
    public void setSshUsername(String sshUsername) { this.sshUsername = sshUsername; }

    public String getSshPassword() { return sshPassword; }
    public void setSshPassword(String sshPassword) { this.sshPassword = sshPassword; }

    /**
     * 获取连接URL
     */
    public String getUrl() {
        return switch (type.toLowerCase()) {
            case "mysql" -> String.format("jdbc:mysql://%s:%d/%s", host, port, database);
            case "postgresql" -> String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
            case "sqlite" -> String.format("jdbc:sqlite:%s", database);
            default -> "";
        };
    }

    @Override
    public String toString() {
        return name != null ? name : type + " - " + host + ":" + port;
    }
}