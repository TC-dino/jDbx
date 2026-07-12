package dino.jdbx.core.plugin;

import dino.jdbx.core.api.*;

import java.util.Optional;

/**
 * 默认插件上下文实现
 */
public class DefaultPluginContext implements PluginContext {

    private final ConnectionManager connectionManager;
    private final ConfigManager configManager;
    private final ThemeManager themeManager;

    public DefaultPluginContext(ConnectionManager connectionManager, ConfigManager configManager, ThemeManager themeManager) {
        this.connectionManager = connectionManager;
        this.configManager = configManager;
        this.themeManager = themeManager;
    }

    @Override
    public void registerMenuItem(String menuPath, MenuItem menuItem) {
        // TODO: 实现菜单项注册
        System.out.println("注册菜单项: " + menuPath + " -> " + menuItem.getText());
    }

    @Override
    public void registerToolbarButton(Button button) {
        // TODO: 实现工具栏按钮注册
        System.out.println("注册工具栏按钮: " + button.getText());
    }

    @Override
    public void registerContextMenu(String target, MenuItem menuItem) {
        // TODO: 实现右键菜单注册
        System.out.println("注册右键菜单: " + target + " -> " + menuItem.getText());
    }

    @Override
    public void registerShortcut(String keyCombination, Runnable action) {
        // TODO: 实现快捷键注册
        System.out.println("注册快捷键: " + keyCombination);
    }

    @Override
    public ConnectionManager getConnectionManager() {
        return connectionManager;
    }

    @Override
    public ConfigManager getConfigManager() {
        return configManager;
    }

    @Override
    public ThemeManager getThemeManager() {
        return themeManager;
    }

    @Override
    public void showNotification(String title, String message, NotificationType type) {
        // TODO: 实现通知显示
        System.out.println("[" + type + "] " + title + ": " + message);
    }

    @Override
    public Optional<String> showInputDialog(String title, String message) {
        // TODO: 实现输入对话框
        return Optional.empty();
    }
}