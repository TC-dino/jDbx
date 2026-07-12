package dino.jdbx.core.api;

/**
 * 连接工厂接口
 * 负责创建和管理数据库连接
 */
public interface ConnectionFactory {

    /**
     * 创建连接
     */
    Connection createConnection(ConnectionConfig config) throws Exception;

    /**
     * 测试连接
     */
    default boolean testConnection(ConnectionConfig config) {
        try (Connection conn = createConnection(config)) {
            return conn != null && conn.isValid();
        } catch (Exception e) {
            return false;
        }
    }
}