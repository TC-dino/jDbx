package dino.jdbx.core.api;

import dino.jdbx.core.config.DefaultConfigManager;
import dino.jdbx.core.connection.DefaultConnectionManager;
import dino.jdbx.core.plugin.DefaultPluginManager;
import dino.jdbx.core.theme.DefaultThemeManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 插件系统测试
 */
class PluginSystemTestJunit {

    private PluginManager pluginManager;
    private ConnectionManager connectionManager;
    private ConfigManager configManager;
    private ThemeManager themeManager;

    @BeforeEach
    void setUp() {
        configManager = new DefaultConfigManager();
        themeManager = new DefaultThemeManager();
        pluginManager = new DefaultPluginManager();
        connectionManager = new DefaultConnectionManager(pluginManager);
    }

    @Test
    void testPluginLoading() {
        // 加载插件
        pluginManager.loadPlugins();

        // 验证插件已加载
        List<Plugin> plugins = pluginManager.getPlugins();
        assertNotNull(plugins);
        assertTrue(plugins.size() > 0, "应该至少加载一个插件");

        System.out.println("已加载 " + plugins.size() + " 个插件");
        for (Plugin plugin : plugins) {
            System.out.println("  - " + plugin.getName() + " (ID: " + plugin.getId() + ")");
        }
    }

    @Test
    void testDatabasePlugins() {
        pluginManager.loadPlugins();

        // 获取数据库插件
        List<DatabasePlugin> dbPlugins = pluginManager.getDatabasePlugins();
        assertNotNull(dbPlugins);
        assertTrue(dbPlugins.size() > 0, "应该至少加载一个数据库插件");

        // 验证每个数据库插件
        for (DatabasePlugin plugin : dbPlugins) {
            assertNotNull(plugin.getId(), "插件ID不能为空");
            assertNotNull(plugin.getName(), "插件名称不能为空");
            assertNotNull(plugin.getVersion(), "插件版本不能为空");
            assertNotNull(plugin.getDatabaseType(), "数据库类型不能为空");
            assertTrue(plugin.getDefaultPort() > 0, "默认端口必须大于0");
            assertNotNull(plugin.createConnectionFactory(), "连接工厂不能为空");
        }
    }

    @Test
    void testSQLitePlugin() {
        pluginManager.loadPlugins();

        // 查找SQLite插件
        DatabasePlugin sqlitePlugin = pluginManager.getDatabasePlugin("sqlite");
        assertNotNull(sqlitePlugin, "应该找到SQLite插件");

        // 验证SQLite插件属性
        assertEquals("sqlite", sqlitePlugin.getId());
        assertEquals("SQLite", sqlitePlugin.getName());
        assertEquals("SQLite", sqlitePlugin.getDatabaseType());

        // 验证连接工厂
        ConnectionFactory factory = sqlitePlugin.createConnectionFactory();
        assertNotNull(factory);

        // 验证连接参数
        List<DatabasePlugin.ConnectionParam> params = sqlitePlugin.getConnectionParams();
        assertNotNull(params);

        // 验证SQL关键词
        List<String> keywords = sqlitePlugin.getKeywords();
        assertNotNull(keywords);
        assertTrue(keywords.size() > 0, "应该有SQL关键词");

        // 验证内置函数
        List<String> functions = sqlitePlugin.getBuiltinFunctions();
        assertNotNull(functions);
    }

    @Test
    void testPluginContext() {
        pluginManager.loadPlugins();

        // 创建插件上下文
        PluginContext context = new dino.jdbx.core.plugin.DefaultPluginContext(
            connectionManager, configManager, themeManager);

        // 验证上下文
        assertNotNull(context);
        assertEquals(connectionManager, context.getConnectionManager());
        assertEquals(configManager, context.getConfigManager());
        assertEquals(themeManager, context.getThemeManager());
    }

    @Test
    void testThemeManager() {
        // 测试初始主题
        assertEquals(ThemeManager.Theme.LIGHT, themeManager.getCurrentTheme());

        // 测试切换主题
        themeManager.setTheme(ThemeManager.Theme.DARK);
        assertEquals(ThemeManager.Theme.DARK, themeManager.getCurrentTheme());

        // 测试切换回亮色主题
        themeManager.setTheme(ThemeManager.Theme.LIGHT);
        assertEquals(ThemeManager.Theme.LIGHT, themeManager.getCurrentTheme());

        // 测试CSS路径
        String lightCss = themeManager.getThemeCss();
        assertNotNull(lightCss);
        assertTrue(lightCss.contains("light"));

        themeManager.setTheme(ThemeManager.Theme.DARK);
        String darkCss = themeManager.getThemeCss();
        assertNotNull(darkCss);
        assertTrue(darkCss.contains("dark"));
    }

    @Test
    void testConfigManager() {
        // 测试设置和获取
        configManager.set("test.key", "test.value");
        assertEquals("test.value", configManager.getString("test.key", null));

        // 测试默认值
        assertNull(configManager.getString("nonexistent.key", null));
        assertEquals("default", configManager.getString("nonexistent.key", "default"));

        // 测试整数配置
        configManager.set("test.int", 42);
        assertEquals(42, configManager.getInt("test.int", 0));

        // 测试布尔配置
        configManager.set("test.bool", true);
        assertTrue(configManager.getBoolean("test.bool", false));
    }
}
