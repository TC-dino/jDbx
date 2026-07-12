package dino.jdbx.core.config;

import dino.jdbx.core.api.ConfigManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 默认配置管理器实现
 */
public class DefaultConfigManager implements ConfigManager {

    private static final String CONFIG_DIR = System.getProperty("user.home") + File.separator + ".jdbx";
    private static final String CONFIG_FILE = CONFIG_DIR + File.separator + "config.json";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, Object> config = new HashMap<>();
    private EditorConfig editorConfig = new EditorConfig();
    private QueryConfig queryConfig = new QueryConfig();

    public DefaultConfigManager() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                String json = Files.readString(configPath);
                @SuppressWarnings("unchecked")
                Map<String, Object> loaded = gson.fromJson(json, Map.class);
                if (loaded != null) {
                    config = loaded;
                }
            }
        } catch (Exception e) {
            System.err.println("加载配置失败: " + e.getMessage());
        }

        // 加载编辑器配置
        Object editorObj = config.get("editor");
        if (editorObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> editorMap = (Map<String, Object>) editorObj;
            editorConfig.setTabSize(getInt(editorMap, "tabSize", 4));
            editorConfig.setWordWrap(getBoolean(editorMap, "wordWrap", false));
            editorConfig.setShowLineNumbers(getBoolean(editorMap, "showLineNumbers", true));
            editorConfig.setHighlightCurrentLine(getBoolean(editorMap, "highlightCurrentLine", true));
            editorConfig.setAutoSave(getBoolean(editorMap, "autoSave", true));
            editorConfig.setAutoSaveInterval(getInt(editorMap, "autoSaveInterval", 30));
        }

        // 加载查询配置
        Object queryObj = config.get("query");
        if (queryObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> queryMap = (Map<String, Object>) queryObj;
            queryConfig.setTimeout(getInt(queryMap, "timeout", 30));
            queryConfig.setMaxRows(getInt(queryMap, "maxRows", 10000));
            queryConfig.setAutoLimit(getBoolean(queryMap, "autoLimit", true));
        }
    }

    private void saveConfig() {
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }

            // 保存编辑器配置
            Map<String, Object> editorMap = new HashMap<>();
            editorMap.put("tabSize", editorConfig.getTabSize());
            editorMap.put("wordWrap", editorConfig.isWordWrap());
            editorMap.put("showLineNumbers", editorConfig.isShowLineNumbers());
            editorMap.put("highlightCurrentLine", editorConfig.isHighlightCurrentLine());
            editorMap.put("autoSave", editorConfig.isAutoSave());
            editorMap.put("autoSaveInterval", editorConfig.getAutoSaveInterval());
            config.put("editor", editorMap);

            // 保存查询配置
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("timeout", queryConfig.getTimeout());
            queryMap.put("maxRows", queryConfig.getMaxRows());
            queryMap.put("autoLimit", queryConfig.isAutoLimit());
            config.put("query", queryMap);

            String json = gson.toJson(config);
            Files.writeString(Paths.get(CONFIG_FILE), json);
        } catch (Exception e) {
            System.err.println("保存配置失败: " + e.getMessage());
        }
    }

    private int getInt(Map<String, Object> map, String key, int defaultValue) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private boolean getBoolean(Map<String, Object> map, String key, boolean defaultValue) {
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    @Override
    public String getString(String key, String defaultValue) {
        Object value = config.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        Object value = config.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    @Override
    public void set(String key, String value) {
        config.put(key, value);
        saveConfig();
    }

    @Override
    public void set(String key, int value) {
        config.put(key, value);
        saveConfig();
    }

    @Override
    public void set(String key, boolean value) {
        config.put(key, value);
        saveConfig();
    }

    @Override
    public String getTheme() {
        return getString("theme", "dark");
    }

    @Override
    public void setTheme(String theme) {
        set("theme", theme);
    }

    @Override
    public String getFontFamily() {
        Object fontObj = config.get("font");
        if (fontObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fontMap = (Map<String, Object>) fontObj;
            Object family = fontMap.get("family");
            return family != null ? family.toString() : "Consolas";
        }
        return "Consolas";
    }

    @Override
    public int getFontSize() {
        Object fontObj = config.get("font");
        if (fontObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fontMap = (Map<String, Object>) fontObj;
            return getInt(fontMap, "size", 14);
        }
        return 14;
    }

    @Override
    public EditorConfig getEditorConfig() {
        return editorConfig;
    }

    @Override
    public QueryConfig getQueryConfig() {
        return queryConfig;
    }
}