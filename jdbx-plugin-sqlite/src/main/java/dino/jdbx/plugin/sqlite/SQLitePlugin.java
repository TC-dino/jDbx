package dino.jdbx.plugin.sqlite;

import dino.jdbx.core.api.*;

import java.util.List;

/**
 * SQLite 插件实现
 */
public class SQLitePlugin implements DatabasePlugin {

    @Override
    public String getId() {
        return "sqlite";
    }

    @Override
    public String getName() {
        return "SQLite";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "SQLite 数据库插件";
    }

    @Override
    public String getAuthor() {
        return "jDbx Team";
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }

    @Override
    public int getDefaultPort() {
        return -1; // SQLite 不需要端口
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new SQLiteConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return List.of(
            new ConnectionParam("database", "数据库文件", "file", true)
        );
    }

    @Override
    public List<String> getKeywords() {
        return List.of(
            "SELECT", "FROM", "WHERE", "JOIN", "LEFT", "RIGHT", "INNER",
            "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP",
            "GROUP", "BY", "HAVING", "ORDER", "LIMIT", "OFFSET",
            "AND", "OR", "NOT", "IN", "LIKE", "BETWEEN", "IS", "NULL",
            "PRAGMA", "VACUUM", "REINDEX", "ATTACH", "DETACH",
            "AUTOINCREMENT", "OR", "REPLACE", "CONFLICT"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        return List.of(
            "ABS", "LENGTH", "LOWER", "UPPER", "TRIM", "LTRIM", "RTRIM",
            "SUBSTR", "REPLACE", "INSTR", "CAST", "TYPEOF", "LAST_INSERT_ROWID",
            "CHANGES", "TOTAL_CHANGES", "SQLITE_VERSION", "SQLITE_SOURCE_ID"
        );
    }

    @Override
    public void initialize(PluginContext context) {
        // 初始化插件
        System.out.println("SQLite 插件已加载");
    }
}