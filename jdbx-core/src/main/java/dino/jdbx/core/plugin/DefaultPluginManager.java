package dino.jdbx.core.plugin;

import dino.jdbx.core.api.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认插件管理器实现
 */
public class DefaultPluginManager implements PluginManager {

    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, Boolean> enabledPlugins = new ConcurrentHashMap<>();
    private PluginContext context;

    public DefaultPluginManager() {
    }

    public void setContext(PluginContext context) {
        this.context = context;
    }

    @Override
    public void loadPlugins() {
        // 使用 ServiceLoader 加载所有数据库插件
        ServiceLoader<DatabasePlugin> loader = ServiceLoader.load(DatabasePlugin.class);
        for (DatabasePlugin plugin : loader) {
            plugins.put(plugin.getId(), plugin);
            enabledPlugins.put(plugin.getId(), true);

            // 初始化插件
            if (context != null) {
                plugin.initialize(context);
            }

            System.out.println("已加载插件: " + plugin.getName() + " (ID: " + plugin.getId() + ")");
        }
    }

    @Override
    public List<Plugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public Plugin getPlugin(String id) {
        return plugins.get(id);
    }

    @Override
    public List<DatabasePlugin> getDatabasePlugins() {
        return plugins.values().stream()
            .filter(p -> p instanceof DatabasePlugin)
            .map(p -> (DatabasePlugin) p)
            .filter(p -> enabledPlugins.getOrDefault(p.getId(), true))
            .collect(Collectors.toList());
    }

    @Override
    public DatabasePlugin getDatabasePlugin(String type) {
        return plugins.values().stream()
            .filter(p -> p instanceof DatabasePlugin)
            .map(p -> (DatabasePlugin) p)
            .filter(p -> p.getDatabaseType().equalsIgnoreCase(type) || p.getId().equalsIgnoreCase(type))
            .filter(p -> enabledPlugins.getOrDefault(p.getId(), true))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void installPlugin(String pluginPath) {
        // TODO: 实现插件安装
        throw new UnsupportedOperationException("插件安装功能尚未实现");
    }

    @Override
    public void uninstallPlugin(String pluginId) {
        Plugin plugin = plugins.get(pluginId);
        if (plugin != null) {
            plugin.unload();
            plugins.remove(pluginId);
            enabledPlugins.remove(pluginId);
        }
    }

    @Override
    public void enablePlugin(String pluginId) {
        enabledPlugins.put(pluginId, true);
    }

    @Override
    public void disablePlugin(String pluginId) {
        enabledPlugins.put(pluginId, false);
    }

    @Override
    public boolean isPluginEnabled(String pluginId) {
        return enabledPlugins.getOrDefault(pluginId, false);
    }
}