package dino.jdbx.core.api;

import java.util.List;
import java.util.ServiceLoader;

/**
 * SQLite连接测试
 */
public class SQLiteConnectionTest {

    public static void main(String[] args) {
        System.out.println("=== SQLite 连接测试 ===\n");

        // 获取SQLite插件
        ServiceLoader<DatabasePlugin> loader = ServiceLoader.load(DatabasePlugin.class);
        DatabasePlugin sqlitePlugin = loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.getId().equals("sqlite"))
            .findFirst()
            .orElse(null);

        if (sqlitePlugin == null) {
            System.out.println("错误: 未找到SQLite插件");
            return;
        }

        // 创建连接配置
        ConnectionConfig config = new ConnectionConfig();
        config.setName("测试数据库");
        config.setType("sqlite");
        config.setDatabase(":memory:"); // 使用内存数据库

        try {
            // 创建连接
            ConnectionFactory factory = sqlitePlugin.createConnectionFactory();
            Connection connection = factory.createConnection(config);

            System.out.println("1. 连接创建成功");
            System.out.println("   连接ID: " + connection.getId());
            System.out.println("   连接名称: " + connection.getName());
            System.out.println("   数据库类型: " + connection.getDatabaseType());
            System.out.println("   是否已连接: " + connection.isConnected());
            System.out.println("   当前数据库: " + connection.getCurrentDatabase());

            // 测试查询
            System.out.println("\n2. 测试查询:");

            // 创建表
            String createTableSQL = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, email TEXT)";
            int result = connection.executeUpdate(createTableSQL);
            System.out.println("   创建表: " + (result == 0 ? "成功" : "失败"));

            // 插入数据
            String insertSQL = "INSERT INTO users (name, email) VALUES ('张三', 'zhangsan@example.com')";
            result = connection.executeUpdate(insertSQL);
            System.out.println("   插入数据: " + (result == 1 ? "成功" : "失败"));

            // 查询数据
            String selectSQL = "SELECT * FROM users";
            QueryResult queryResult = connection.executeQuery(selectSQL);
            System.out.println("   查询结果:");
            System.out.println("     列名: " + queryResult.getColumnNames());
            System.out.println("     行数: " + queryResult.getRowCount());
            System.out.println("     执行时间: " + queryResult.getExecutionTime() + "ms");

            if (queryResult.getRowCount() > 0) {
                System.out.println("     数据:");
                for (var row : queryResult.getRows()) {
                    System.out.println("       " + row);
                }
            }

            // 测试元数据
            System.out.println("\n3. 测试元数据:");
            List<String> tables = connection.getTables();
            System.out.println("   表数量: " + tables.size());
            System.out.println("   表名: " + tables);

            // 获取表结构
            if (!tables.isEmpty()) {
                Metadata metadata = connection.getTableMetadata(tables.get(0));
                System.out.println("   表 '" + metadata.getTableName() + "' 结构:");
                for (Metadata.Column column : metadata.getColumns()) {
                    System.out.println("     - " + column.getName() + " (" + column.getType() + ")");
                }
            }

            // 关闭连接
            connection.close();
            System.out.println("\n4. 连接已关闭");

        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== 测试完成 ===");
    }
}