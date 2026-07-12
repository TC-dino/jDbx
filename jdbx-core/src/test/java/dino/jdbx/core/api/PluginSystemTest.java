package dino.jdbx.core.api;

import java.util.List;
import java.util.ServiceLoader;

/**
 * 插件系统测试
 */
public class PluginSystemTest {

    public static void main(String[] args) {
        System.out.println("=== jDbx 插件系统测试 ===\n");

        // 测试插件加载
        testPluginLoading();

        // 测试SQLite插件
        testSQLitePlugin();

        System.out.println("\n=== 测试完成 ===");
    }

    private static void testPluginLoading() {
        System.out.println("1. 测试插件加载:");

        ServiceLoader<DatabasePlugin> loader = ServiceLoader.load(DatabasePlugin.class);
        List<DatabasePlugin> plugins = loader.stream()
            .map(ServiceLoader.Provider::get)
            .toList();

        System.out.println("   找到 " + plugins.size() + " 个数据库插件");

        for (DatabasePlugin plugin : plugins) {
            System.out.println("   - " + plugin.getName() + " (ID: " + plugin.getId() + ")");
            System.out.println("     版本: " + plugin.getVersion());
            System.out.println("     描述: " + plugin.getDescription());
            System.out.println("     作者: " + plugin.getAuthor());
        }
    }

    private static void testSQLitePlugin() {
        System.out.println("\n2. 测试SQLite插件:");

        ServiceLoader<DatabasePlugin> loader = ServiceLoader.load(DatabasePlugin.class);
        DatabasePlugin sqlitePlugin = loader.stream()
            .map(ServiceLoader.Provider::get)
            .filter(p -> p.getId().equals("sqlite"))
            .findFirst()
            .orElse(null);

        if (sqlitePlugin != null) {
            System.out.println("   找到SQLite插件");
            System.out.println("   数据库类型: " + sqlitePlugin.getDatabaseType());
            System.out.println("   默认端口: " + sqlitePlugin.getDefaultPort());

            // 测试连接工厂
            ConnectionFactory factory = sqlitePlugin.createConnectionFactory();
            System.out.println("   连接工厂创建成功: " + factory.getClass().getSimpleName());

            // 测试连接参数
            List<DatabasePlugin.ConnectionParam> params = sqlitePlugin.getConnectionParams();
            System.out.println("   连接参数数量: " + params.size());

            // 测试关键词
            List<String> keywords = sqlitePlugin.getKeywords();
            System.out.println("   SQL关键词数量: " + keywords.size());

            // 测试内置函数
            List<String> functions = sqlitePlugin.getBuiltinFunctions();
            System.out.println("   内置函数数量: " + functions.size());
        } else {
            System.out.println("   未找到SQLite插件");
        }
    }
}