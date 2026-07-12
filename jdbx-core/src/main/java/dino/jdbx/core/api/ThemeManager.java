package dino.jdbx.core.api;

/**
 * 主题管理器接口
 */
public interface ThemeManager {

    /**
     * 主题枚举
     */
    enum Theme {
        LIGHT, DARK, SYSTEM
    }

    /**
     * 获取当前主题
     */
    Theme getCurrentTheme();

    /**
     * 设置主题
     */
    void setTheme(Theme theme);

    /**
     * 切换到下一个主题
     */
    void toggleTheme();

    /**
     * 获取主题CSS文件路径
     */
    String getThemeCss();

    /**
     * 获取颜色变量值
     */
    String getColor(String name);

    /**
     * 主题变更监听器
     */
    interface ThemeChangeListener {
        void onThemeChanged(Theme newTheme);
    }

    /**
     * 添加主题变更监听器
     */
    void addThemeChangeListener(ThemeChangeListener listener);

    /**
     * 移除主题变更监听器
     */
    void removeThemeChangeListener(ThemeChangeListener listener);
}