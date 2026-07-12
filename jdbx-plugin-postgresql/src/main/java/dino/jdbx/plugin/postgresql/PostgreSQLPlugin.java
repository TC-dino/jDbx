package dino.jdbx.plugin.postgresql;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * PostgreSQL 数据库插件
 */
public class PostgreSQLPlugin implements DatabasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLPlugin.class);

    private PluginContext context;

    @Override
    public String getId() {
        return "postgresql";
    }

    @Override
    public String getName() {
        return "PostgreSQL";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "PostgreSQL 数据库插件";
    }

    @Override
    public String getAuthor() {
        return "jDbx Team";
    }

    @Override
    public String[] getDependencies() {
        return new String[]{"java.sql"};
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        logger.info("PostgreSQL 插件已初始化");
    }

    @Override
    public void unload() {
        logger.info("PostgreSQL 插件已卸载");
    }

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public int getDefaultPort() {
        return 5432;
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new PostgreSQLConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return Arrays.asList(
            new ConnectionParam("host", "主机", "localhost", true),
            new ConnectionParam("port", "端口", "5432", true),
            new ConnectionParam("database", "数据库", "postgres", true),
            new ConnectionParam("username", "用户名", "postgres", true),
            new ConnectionParam("password", "密码", "", false)
        );
    }

    @Override
    public List<String> getKeywords() {
        return Arrays.asList(
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IN", "BETWEEN", "LIKE",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "FROM",
            "CREATE", "ALTER", "DROP", "TABLE", "INDEX", "VIEW", "SCHEMA",
            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "FULL", "CROSS", "ON",
            "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING",
            "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT", "INTERSECT", "EXCEPT",
            "AS", "IS", "NULL", "TRUE", "FALSE",
            "COUNT", "SUM", "AVG", "MIN", "MAX",
            "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "SAVEPOINT",
            "GRANT", "REVOKE", "TRIGGER", "PROCEDURE", "FUNCTION",
            "WITH", "RECURSIVE", "LATERAL", "WINDOW", "PARTITION",
            "RETURNING", "CONFLICT", "DO", "NOTHING", "EXISTS"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        return Arrays.asList(
            "CONCAT", "CONCAT_WS", "LENGTH", "CHAR_LENGTH", "UPPER", "LOWER",
            "TRIM", "LTRIM", "RTRIM", "SUBSTRING", "SUBSTR", "REPLACE",
            "NOW", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
            "EXTRACT", "DATE_PART", "DATE_TRUNC", "AGE", "INTERVAL",
            "TO_CHAR", "TO_DATE", "TO_TIMESTAMP", "TO_NUMBER",
            "COALESCE", "NULLIF", "GREATEST", "LEAST",
            "CASE", "WHEN", "THEN", "ELSE", "END",
            "CAST", "CONVERT",
            "ABS", "CEIL", "CEILING", "FLOOR", "ROUND", "MOD", "POWER", "SQRT",
            "RANDOM", "SIGN", "TRUNC", "DIV",
            "ARRAY_AGG", "ARRAY_APPEND", "ARRAY_CAT", "ARRAY_LENGTH",
            "STRING_AGG", "STRING_TO_ARRAY", "ARRAY_TO_STRING",
            "JSON_AGG", "JSON_BUILD_OBJECT", "JSON_EXTRACT_PATH",
            "JSONB_AGG", "JSONB_BUILD_OBJECT", "JSONB_EXTRACT_PATH",
            "ROW_NUMBER", "RANK", "DENSE_RANK", "NTILE", "LAG", "LEAD",
            "FIRST_VALUE", "LAST_VALUE", "NTH_VALUE",
            "EXISTS", "NOT_EXISTS", "ANY", "SOME", "ALL"
        );
    }
}
