package dino.jdbx.core.api;

import java.util.List;

/**
 * 连接管理器接口
 */
public interface ConnectionManager {

    /**
     * 获取所有连接配置
     */
    List<ConnectionConfig> getAllConnections();

    /**
     * 根据ID获取连接配置
     */
    ConnectionConfig getConnection(String id);

    /**
     * 保存连接配置
     */
    void saveConnection(ConnectionConfig config);

    /**
     * 删除连接配置
     */
    void deleteConnection(String id);

    /**
     * 测试连接
     */
    boolean testConnection(ConnectionConfig config);

    /**
     * 建立连接
     */
    Connection connect(ConnectionConfig config) throws Exception;

    /**
     * 关闭连接
     */
    void closeConnection(String id);

    /**
     * 获取所有已建立的连接
     */
    List<Connection> getActiveConnections();
}