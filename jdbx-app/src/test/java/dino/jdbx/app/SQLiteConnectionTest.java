package dino.jdbx.app;

import dino.jdbx.core.api.*;
import dino.jdbx.core.connection.DefaultConnectionManager;
import dino.jdbx.core.plugin.DefaultPluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SQLite 连接测试
 */
class SQLiteConnectionTest {

    private PluginManager pluginManager;
    private ConnectionManager connectionManager;

    @BeforeEach
    void setUp() {
        pluginManager = new DefaultPluginManager();
        pluginManager.loadPlugins();
        connectionManager = new DefaultConnectionManager(pluginManager);
    }

    @Test
    void testSQLiteConnection() throws Exception {
        // 创建SQLite连接配置
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试连接");
        config.setType("sqlite");
        config.setDatabase(":memory:"); // 使用内存数据库

        // 测试连接
        Connection connection = connectionManager.connect(config);
        assertNotNull(connection);
        assertTrue(connection.isConnected());
        assertEquals("SQLite", connection.getDatabaseType());

        // 关闭连接
        connection.close();
        assertFalse(connection.isConnected());
    }

    @Test
    void testSQLiteQueryExecution() throws Exception {
        // 创建SQLite连接
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试连接");
        config.setType("sqlite");
        config.setDatabase(":memory:");

        Connection connection = connectionManager.connect(config);

        try {
            // 创建表
            int result = connection.executeUpdate("CREATE TABLE test (id INTEGER PRIMARY KEY, name TEXT, value REAL)");
            assertTrue(result >= 0);

            // 插入数据
            result = connection.executeUpdate("INSERT INTO test (id, name, value) VALUES (1, 'test1', 1.5)");
            assertEquals(1, result);

            result = connection.executeUpdate("INSERT INTO test (id, name, value) VALUES (2, 'test2', 2.5)");
            assertEquals(1, result);

            // 查询数据
            QueryResult queryResult = connection.executeQuery("SELECT * FROM test ORDER BY id");
            assertNotNull(queryResult);
            assertFalse(queryResult.hasError());
            assertTrue(queryResult.isQuery());
            assertEquals(2, queryResult.getRowCount());

            // 验证列名
            List<String> columns = queryResult.getColumnNames();
            assertNotNull(columns);
            assertTrue(columns.contains("id"));
            assertTrue(columns.contains("name"));
            assertTrue(columns.contains("value"));

            // 验证数据
            List<java.util.Map<String, Object>> rows = queryResult.getRows();
            assertEquals(1, rows.get(0).get("id"));
            assertEquals("test1", rows.get(0).get("name"));
            assertEquals(1.5, rows.get(0).get("value"));

            // 更新数据
            result = connection.executeUpdate("UPDATE test SET name = 'updated' WHERE id = 1");
            assertEquals(1, result);

            // 验证更新
            queryResult = connection.executeQuery("SELECT name FROM test WHERE id = 1");
            assertEquals("updated", queryResult.getRows().get(0).get("name"));

            // 删除数据
            result = connection.executeUpdate("DELETE FROM test WHERE id = 1");
            assertEquals(1, result);

            // 验证删除
            queryResult = connection.executeQuery("SELECT COUNT(*) as cnt FROM test");
            assertEquals(1, queryResult.getRows().get(0).get("cnt"));

        } finally {
            connection.close();
        }
    }

    @Test
    void testSQLiteTableMetadata() throws Exception {
        // 创建SQLite连接
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试连接");
        config.setType("sqlite");
        config.setDatabase(":memory:");

        Connection connection = connectionManager.connect(config);

        try {
            // 创建表
            connection.executeUpdate("CREATE TABLE users (id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT, age INTEGER DEFAULT 0)");

            // 获取表元数据
            Metadata metadata = connection.getTableMetadata("users");
            assertNotNull(metadata);
            assertEquals("users", metadata.getTableName());

            // 验证列
            List<Metadata.Column> columns = metadata.getColumns();
            assertNotNull(columns);
            assertEquals(4, columns.size());

            // 验证id列
            Metadata.Column idColumn = columns.stream()
                .filter(c -> c.getName().equals("id"))
                .findFirst()
                .orElse(null);
            assertNotNull(idColumn);
            assertTrue(idColumn.isPrimaryKey());

            // 验证name列
            Metadata.Column nameColumn = columns.stream()
                .filter(c -> c.getName().equals("name"))
                .findFirst()
                .orElse(null);
            assertNotNull(nameColumn);
            assertFalse(nameColumn.isNullable());

            // 获取表列表
            List<String> tables = connection.getTables();
            assertNotNull(tables);
            assertTrue(tables.contains("users"));

        } finally {
            connection.close();
        }
    }

    @Test
    void testSQLiteErrorHandling() throws Exception {
        // 创建SQLite连接
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试连接");
        config.setType("sqlite");
        config.setDatabase(":memory:");

        Connection connection = connectionManager.connect(config);

        try {
            // 测试无效SQL
            QueryResult result = connection.executeQuery("SELECT * FROM nonexistent_table");
            assertNotNull(result);
            assertTrue(result.hasError());
            assertNotNull(result.getError());

            // 测试语法错误
            result = connection.executeQuery("INVALID SQL STATEMENT");
            assertNotNull(result);
            assertTrue(result.hasError());

        } finally {
            connection.close();
        }
    }

    @Test
    void testConnectionManager() throws Exception {
        // 测试获取所有连接
        List<ConnectionConfig> configs = connectionManager.getAllConnections();
        assertNotNull(configs);

        // 测试保存连接配置
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试连接");
        config.setType("sqlite");
        config.setDatabase(":memory:");

        connectionManager.saveConnection(config);

        // 验证配置已保存
        configs = connectionManager.getAllConnections();
        assertTrue(configs.stream().anyMatch(c -> c.getName().equals("测试连接")));

        // 测试删除连接配置
        connectionManager.deleteConnection(config.getId());
        configs = connectionManager.getAllConnections();
        assertFalse(configs.stream().anyMatch(c -> c.getName().equals("测试连接")));
    }
}
