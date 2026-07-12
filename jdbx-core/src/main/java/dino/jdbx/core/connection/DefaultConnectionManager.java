package dino.jdbx.core.connection;

import dino.jdbx.core.api.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认连接管理器实现（含 connections.json 持久化）
 */
public class DefaultConnectionManager implements ConnectionManager {

    private final Map<String, ConnectionConfig> configs = new ConcurrentHashMap<>();
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final PluginManager pluginManager;
    private final ConnectionStore store;

    public DefaultConnectionManager(PluginManager pluginManager) {
        this(pluginManager, new ConnectionStore());
    }

    public DefaultConnectionManager(PluginManager pluginManager, ConnectionStore store) {
        this.pluginManager = pluginManager;
        this.store = store;
        for (ConnectionConfig config : store.load()) {
            configs.put(config.getId(), config);
        }
    }

    @Override
    public List<ConnectionConfig> getAllConnections() {
        return new ArrayList<>(configs.values());
    }

    @Override
    public ConnectionConfig getConnection(String id) {
        return configs.get(id);
    }

    @Override
    public void saveConnection(ConnectionConfig config) {
        config.setUpdatedAt(LocalDateTime.now());
        configs.put(config.getId(), config);
        persist();
    }

    @Override
    public void deleteConnection(String id) {
        configs.remove(id);
        closeConnection(id);
        persist();
    }

    @Override
    public boolean testConnection(ConnectionConfig config) {
        try {
            DatabasePlugin plugin = pluginManager.getDatabasePlugin(config.getType());
            if (plugin == null) {
                return false;
            }
            ConnectionFactory factory = plugin.createConnectionFactory();
            return factory.testConnection(config);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Connection connect(ConnectionConfig config) throws Exception {
        if (connections.containsKey(config.getId())) {
            closeConnection(config.getId());
        }

        DatabasePlugin plugin = pluginManager.getDatabasePlugin(config.getType());
        if (plugin == null) {
            throw new Exception("未找到数据库插件: " + config.getType());
        }

        ConnectionFactory factory = plugin.createConnectionFactory();
        Connection connection = factory.createConnection(config);
        connections.put(config.getId(), connection);
        saveConnection(config);
        return connection;
    }

    @Override
    public void closeConnection(String id) {
        Connection connection = connections.remove(id);
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }

    @Override
    public List<Connection> getActiveConnections() {
        return new ArrayList<>(connections.values());
    }

    public Connection getActiveConnection(String id) {
        return connections.get(id);
    }

    public boolean isConnected(String id) {
        Connection connection = connections.get(id);
        return connection != null && connection.isConnected();
    }

    private void persist() {
        try {
            store.save(new ArrayList<>(configs.values()));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
