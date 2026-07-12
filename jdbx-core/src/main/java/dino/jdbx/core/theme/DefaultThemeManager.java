package dino.jdbx.core.theme;

import dino.jdbx.core.api.ThemeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * 默认主题管理器实现
 */
public class DefaultThemeManager implements ThemeManager {

    private Theme currentTheme = Theme.DARK;
    private final List<ThemeChangeListener> listeners = new ArrayList<>();

    @Override
    public Theme getCurrentTheme() {
        return currentTheme;
    }

    @Override
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        notifyListeners();
    }

    @Override
    public void toggleTheme() {
        switch (currentTheme) {
            case LIGHT -> setTheme(Theme.DARK);
            case DARK -> setTheme(Theme.SYSTEM);
            case SYSTEM -> setTheme(Theme.LIGHT);
        }
    }

    @Override
    public String getThemeCss() {
        return switch (currentTheme) {
            case LIGHT -> "/dino/jdbx/app/styles/light.css";
            case DARK -> "/dino/jdbx/app/styles/dark.css";
            case SYSTEM -> {
                // 检测系统主题
                boolean isDark = isSystemDarkTheme();
                yield isDark ? "/dino/jdbx/app/styles/dark.css" : "/dino/jdbx/app/styles/light.css";
            }
        };
    }

    @Override
    public String getColor(String name) {
        // TODO: 实现颜色获取
        return switch (name) {
            case "primary" -> "#0078D4";
            case "background" -> currentTheme == Theme.DARK ? "#1E1E1E" : "#FFFFFF";
            case "text" -> currentTheme == Theme.DARK ? "#CCCCCC" : "#1A1A1A";
            default -> "#000000";
        };
    }

    @Override
    public void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (ThemeChangeListener listener : listeners) {
            listener.onThemeChanged(currentTheme);
        }
    }

    private boolean isSystemDarkTheme() {
        // 简单检测：检查系统属性
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows: 检查注册表或使用默认值
            return false;
        } else if (os.contains("mac")) {
            // macOS: 检查系统属性
            String theme = System.getProperty("apple.awt.application.appearance");
            return "NSAppearanceNameDarkAqua".equals(theme);
        }
        return false;
    }
}