package dino.jdbx.core.api;

import java.util.List;

/**
 * 数据库连接插件接口
 * 继承 Plugin，提供数据库连接能力
 */
public interface DatabasePlugin extends Plugin {

    /**
     * 获取数据库类型名称（如 "MySQL", "PostgreSQL"）
     */
    String getDatabaseType();

    /**
     * 获取数据库图标
     */
    default byte[] getIcon() {
        return null;
    }

    /**
     * 获取默认端口
     */
    default int getDefaultPort() {
        return -1;
    }

    /**
     * 创建连接工厂
     */
    ConnectionFactory createConnectionFactory();

    /**
     * 获取支持的连接参数
     */
    default List<ConnectionParam> getConnectionParams() {
        return List.of();
    }

    /**
     * 获取关键词（用于 SQL 高亮）
     */
    default List<String> getKeywords() {
        return List.of();
    }

    /**
     * 获取内置函数（用于自动补全）
     */
    default List<String> getBuiltinFunctions() {
        return List.of();
    }

    /**
     * 连接参数类
     */
    class ConnectionParam {
        private String name;
        private String label;
        private String type; // text, password, number, boolean, select
        private boolean required;
        private String defaultValue;
        private List<String> options;

        public ConnectionParam(String name, String label, String type, boolean required) {
            this.name = name;
            this.label = label;
            this.type = type;
            this.required = required;
        }

        // Getters and Setters
        public String getName() { return name; }
        public String getLabel() { return label; }
        public String getType() { return type; }
        public boolean isRequired() { return required; }
        public String getDefaultValue() { return defaultValue; }
        public void setDefaultValue(String defaultValue) { this.defaultValue = defaultValue; }
        public List<String> getOptions() { return options; }
        public void setOptions(List<String> options) { this.options = options; }
    }
}