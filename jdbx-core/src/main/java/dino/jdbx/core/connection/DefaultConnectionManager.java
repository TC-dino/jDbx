package dino.jdbx.core.connection;

import dino.jdbx.core.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认连接管理器实现
 */
public class DefaultConnectionManager implements ConnectionManager {

    private final Map<String, ConnectionConfig> configs = new ConcurrentHashMap<>();
    private final Map<String, Connection> connections = new ConcurrentHashMap<>();
    private final PluginManager pluginManager;

    public DefaultConnectionManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
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
        configs.put(config.getId(), config);
    }

    @Override
    public void deleteConnection(String id) {
        configs.remove(id);
        closeConnection(id);
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
        // 如果已经有活跃连接，先关闭
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

        // 保存配置
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
                // 忽略关闭异常
            }
        }
    }

    @Override
    public List<Connection> getActiveConnections() {
        return new ArrayList<>(connections.values());
    }

    /**
     * 根据ID获取活跃连接
     */
    public Connection getActiveConnection(String id) {
        return connections.get(id);
    }

    /**
     * 检查连接是否活跃
     */
    public boolean isConnected(String id) {
        Connection connection = connections.get(id);
        return connection != null && connection.isConnected();
    }
}