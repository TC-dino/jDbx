package dino.jdbx.core.api;

import java.util.List;

/**
 * 插件管理器接口
 */
public interface PluginManager {

    /**
     * 加载所有插件
     */
    void loadPlugins();

    /**
     * 获取所有已加载的插件
     */
    List<Plugin> getPlugins();

    /**
     * 根据ID获取插件
     */
    Plugin getPlugin(String id);

    /**
     * 获取所有数据库插件
     */
    List<DatabasePlugin> getDatabasePlugins();

    /**
     * 根据类型获取数据库插件
     */
    DatabasePlugin getDatabasePlugin(String type);

    /**
     * 安装插件
     */
    void installPlugin(String pluginPath);

    /**
     * 卸载插件
     */
    void uninstallPlugin(String pluginId);

    /**
     * 启用插件
     */
    void enablePlugin(String pluginId);

    /**
     * 禁用插件
     */
    void disablePlugin(String pluginId);

    /**
     * 检查插件是否已启用
     */
    boolean isPluginEnabled(String pluginId);
}