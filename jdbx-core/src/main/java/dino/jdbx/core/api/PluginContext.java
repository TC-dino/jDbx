package dino.jdbx.core.api;

import java.util.Optional;

/**
 * 插件上下文接口
 * 提供插件与核心系统的交互能力
 */
public interface PluginContext {

    /**
     * 注册菜单项
     */
    void registerMenuItem(String menuPath, MenuItem menuItem);

    /**
     * 注册工具栏按钮
     */
    void registerToolbarButton(Button button);

    /**
     * 注册右键菜单项
     */
    void registerContextMenu(String target, MenuItem menuItem);

    /**
     * 注册快捷键
     */
    void registerShortcut(String keyCombination, Runnable action);

    /**
     * 获取连接管理器
     */
    ConnectionManager getConnectionManager();

    /**
     * 获取配置管理器
     */
    ConfigManager getConfigManager();

    /**
     * 获取主题管理器
     */
    ThemeManager getThemeManager();

    /**
     * 显示通知
     */
    void showNotification(String title, String message, NotificationType type);

    /**
     * 显示对话框
     */
    Optional<String> showInputDialog(String title, String message);

    /**
     * 通知类型枚举
     */
    enum NotificationType {
        INFO, WARNING, ERROR, SUCCESS
    }

    /**
     * 菜单项类（简化版，实际实现时需要扩展）
     */
    class MenuItem {
        private String text;
        private Runnable action;

        public MenuItem(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }

        public String getText() { return text; }
        public Runnable getAction() { return action; }
    }

    /**
     * 按钮类（简化版，实际实现时需要扩展）
     */
    class Button {
        private String text;
        private Runnable action;

        public Button(String text, Runnable action) {
            this.text = text;
            this.action = action;
        }

        public String getText() { return text; }
        public Runnable getAction() { return action; }
    }
}