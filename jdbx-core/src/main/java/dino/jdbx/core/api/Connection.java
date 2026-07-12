package dino.jdbx.core.api;

import java.util.List;

/**
 * 数据库连接接口
 */
public interface Connection extends AutoCloseable {

    /**
     * 获取连接 ID
     */
    String getId();

    /**
     * 获取连接名称
     */
    String getName();

    /**
     * 获取数据库类型
     */
    String getDatabaseType();

    /**
     * 是否已连接
     */
    boolean isConnected();

    /**
     * 获取当前数据库
     */
    String getCurrentDatabase();

    /**
     * 获取所有数据库
     */
    List<String> getDatabases();

    /**
     * 切换数据库
     */
    void useDatabase(String database);

    /**
     * 获取所有表
     */
    List<String> getTables();

    /**
     * 获取所有视图
     */
    List<String> getViews();

    /**
     * 获取表结构
     */
    Metadata getTableMetadata(String table);

    /**
     * 执行查询
     */
    QueryResult executeQuery(String sql) throws Exception;

    /**
     * 执行更新
     */
    int executeUpdate(String sql) throws Exception;

    /**
     * 获取查询执行器
     */
    QueryExecutor getQueryExecutor();

    /**
     * 获取原生连接对象
     */
    Object getNativeConnection();

    /**
     * 验证连接是否有效
     */
    boolean isValid();
}