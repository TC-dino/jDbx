package dino.jdbx.plugin.mysql;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * MySQL 数据库插件
 */
public class MySQLPlugin implements DatabasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(MySQLPlugin.class);

    private PluginContext context;

    @Override
    public String getId() {
        return "mysql";
    }

    @Override
    public String getName() {
        return "MySQL";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "MySQL 数据库插件";
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
        logger.info("MySQL 插件已初始化");
    }

    @Override
    public void unload() {
        logger.info("MySQL 插件已卸载");
    }

    @Override
    public String getDatabaseType() {
        return "MySQL";
    }

    @Override
    public int getDefaultPort() {
        return 3306;
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new MySQLConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return Arrays.asList(
            new ConnectionParam("host", "主机", "localhost", true),
            new ConnectionParam("port", "端口", "3306", true),
            new ConnectionParam("database", "数据库", "", true),
            new ConnectionParam("username", "用户名", "root", true),
            new ConnectionParam("password", "密码", "", false)
        );
    }

    @Override
    public List<String> getKeywords() {
        return Arrays.asList(
            "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IN", "BETWEEN", "LIKE",
            "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE", "FROM",
            "CREATE", "ALTER", "DROP", "TABLE", "INDEX", "VIEW",
            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON",
            "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING",
            "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT",
            "AS", "IS", "NULL", "TRUE", "FALSE",
            "COUNT", "SUM", "AVG", "MIN", "MAX",
            "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION",
            "GRANT", "REVOKE", "TRIGGER", "PROCEDURE", "FUNCTION"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        return Arrays.asList(
            "CONCAT", "CONCAT_WS", "LENGTH", "CHAR_LENGTH", "UPPER", "LOWER",
            "TRIM", "LTRIM", "RTRIM", "SUBSTRING", "SUBSTR", "REPLACE",
            "NOW", "CURDATE", "CURTIME", "DATE_FORMAT", "STR_TO_DATE",
            "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND",
            "DATE_ADD", "DATE_SUB", "DATEDIFF", "TIMESTAMPDIFF",
            "IF", "IFNULL", "NULLIF", "CASE", "WHEN", "THEN", "ELSE", "END",
            "CAST", "CONVERT", "COALESCE",
            "ABS", "CEIL", "FLOOR", "ROUND", "MOD", "POWER", "SQRT",
            "RAND", "SIGN", "TRUNCATE",
            "JSON_EXTRACT", "JSON_OBJECT", "JSON_ARRAY", "JSON_CONTAINS",
            "GROUP_CONCAT", "GROUP_BY", "DISTINCT",
            "EXISTS", "NOT_EXISTS", "ANY", "SOME"
        );
    }
}
