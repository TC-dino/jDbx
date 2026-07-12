package dino.jdbx.core.api;

/**
 * 配置管理器接口
 */
public interface ConfigManager {

    /**
     * 获取配置值
     */
    String getString(String key, String defaultValue);

    /**
     * 获取整数配置值
     */
    int getInt(String key, int defaultValue);

    /**
     * 获取布尔配置值
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * 设置配置值
     */
    void set(String key, String value);

    /**
     * 设置整数配置值
     */
    void set(String key, int value);

    /**
     * 设置布尔配置值
     */
    void set(String key, boolean value);

    /**
     * 获取主题
     */
    String getTheme();

    /**
     * 设置主题
     */
    void setTheme(String theme);

    /**
     * 获取字体
     */
    String getFontFamily();

    /**
     * 获取字体大小
     */
    int getFontSize();

    /**
     * 获取编辑器配置
     */
    EditorConfig getEditorConfig();

    /**
     * 获取查询配置
     */
    QueryConfig getQueryConfig();

    /**
     * 编辑器配置
     */
    class EditorConfig {
        private int tabSize = 4;
        private boolean wordWrap = false;
        private boolean showLineNumbers = true;
        private boolean highlightCurrentLine = true;
        private boolean autoSave = true;
        private int autoSaveInterval = 30;

        // Getters and Setters
        public int getTabSize() { return tabSize; }
        public void setTabSize(int tabSize) { this.tabSize = tabSize; }
        public boolean isWordWrap() { return wordWrap; }
        public void setWordWrap(boolean wordWrap) { this.wordWrap = wordWrap; }
        public boolean isShowLineNumbers() { return showLineNumbers; }
        public void setShowLineNumbers(boolean showLineNumbers) { this.showLineNumbers = showLineNumbers; }
        public boolean isHighlightCurrentLine() { return highlightCurrentLine; }
        public void setHighlightCurrentLine(boolean highlightCurrentLine) { this.highlightCurrentLine = highlightCurrentLine; }
        public boolean isAutoSave() { return autoSave; }
        public void setAutoSave(boolean autoSave) { this.autoSave = autoSave; }
        public int getAutoSaveInterval() { return autoSaveInterval; }
        public void setAutoSaveInterval(int autoSaveInterval) { this.autoSaveInterval = autoSaveInterval; }
    }

    /**
     * 查询配置
     */
    class QueryConfig {
        private int timeout = 30;
        private int maxRows = 10000;
        private boolean autoLimit = true;

        // Getters and Setters
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getMaxRows() { return maxRows; }
        public void setMaxRows(int maxRows) { this.maxRows = maxRows; }
        public boolean isAutoLimit() { return autoLimit; }
        public void setAutoLimit(boolean autoLimit) { this.autoLimit = autoLimit; }
    }
}