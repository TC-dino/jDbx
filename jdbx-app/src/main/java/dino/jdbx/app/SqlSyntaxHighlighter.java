package dino.jdbx.app;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 语法高亮器
 */
public class SqlSyntaxHighlighter {

    // SQL 关键字
    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
        "SELECT", "FROM", "WHERE", "AND", "OR", "NOT", "IN", "BETWEEN", "LIKE",
        "INSERT", "INTO", "VALUES", "UPDATE", "SET", "DELETE",
        "CREATE", "ALTER", "DROP", "TABLE", "INDEX", "VIEW", "SCHEMA",
        "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "FULL", "CROSS", "ON",
        "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING",
        "LIMIT", "OFFSET", "UNION", "ALL", "DISTINCT", "INTERSECT", "EXCEPT",
        "AS", "IS", "NULL", "TRUE", "FALSE", "UNKNOWN",
        "BEGIN", "COMMIT", "ROLLBACK", "TRANSACTION", "SAVEPOINT",
        "GRANT", "REVOKE", "TRIGGER", "PROCEDURE", "FUNCTION",
        "WITH", "RECURSIVE", "LATERAL", "WINDOW", "PARTITION",
        "RETURNING", "CONFLICT", "DO", "NOTHING", "EXISTS",
        "CASE", "WHEN", "THEN", "ELSE", "END",
        "CAST", "CONVERT", "USING",
        "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT",
        "UNIQUE", "DEFAULT", "CHECK", "AUTO_INCREMENT",
        "VARCHAR", "INT", "INTEGER", "BIGINT", "SMALLINT", "TINYINT",
        "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "REAL",
        "DATE", "TIME", "TIMESTAMP", "DATETIME", "YEAR",
        "CHAR", "TEXT", "BLOB", "CLOB", "BOOLEAN",
        "ANY", "SOME"
    ));

    // SQL 函数
    private static final Set<String> FUNCTIONS = new HashSet<>(Arrays.asList(
        "COUNT", "SUM", "AVG", "MIN", "MAX",
        "CONCAT", "CONCAT_WS", "LENGTH", "CHAR_LENGTH", "UPPER", "LOWER",
        "TRIM", "LTRIM", "RTRIM", "SUBSTRING", "SUBSTR", "REPLACE",
        "NOW", "CURDATE", "CURTIME", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP",
        "DATE_FORMAT", "STR_TO_DATE", "EXTRACT", "DATE_PART", "DATE_TRUNC",
        "YEAR", "MONTH", "DAY", "HOUR", "MINUTE", "SECOND",
        "COALESCE", "NULLIF", "GREATEST", "LEAST", "IF", "IFNULL",
        "ABS", "CEIL", "CEILING", "FLOOR", "ROUND", "MOD", "POWER", "SQRT",
        "RANDOM", "RAND", "SIGN", "TRUNC", "TRUNCATE",
        "JSON_EXTRACT", "JSON_OBJECT", "JSON_ARRAY",
        "ARRAY_AGG", "STRING_AGG", "GROUP_CONCAT",
        "ROW_NUMBER", "RANK", "DENSE_RANK", "NTILE", "LAG", "LEAD",
        "FIRST_VALUE", "LAST_VALUE", "NTH_VALUE",
        "EXISTS", "NOT_EXISTS"
    ));

    // 颜色定义
    private static final String KEYWORD_COLOR = "#0000FF";      // 蓝色
    private static final String FUNCTION_COLOR = "#7B2FBE";     // 紫色
    private static final String STRING_COLOR = "#A31515";       // 红色
    private static final String NUMBER_COLOR = "#098658";       // 绿色
    private static final String COMMENT_COLOR = "#008000";      // 绿色
    private static final String OPERATOR_COLOR = "#000000";     // 黑色
    private static final String DEFAULT_COLOR = "#000000";      // 黑色

    // 正则表达式模式
    private static final Pattern STRING_PATTERN = Pattern.compile("'[^']*'|\"[^\"]*\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("--[^\n]*|/\\*[\\s\\S]*?\\*/");
    private static final Pattern KEYWORD_PATTERN = Pattern.compile("\\b[A-Z_]+\\b");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("\\b[a-zA-Z_][a-zA-Z0-9_]*\\b");

    /**
     * 高亮 SQL 文本
     */
    public static TextFlow highlight(String sql) {
        TextFlow textFlow = new TextFlow();
        if (sql == null || sql.isEmpty()) {
            return textFlow;
        }

        // 使用简单的逐字符分析
        int i = 0;
        int len = sql.length();
        StringBuilder currentToken = new StringBuilder();

        while (i < len) {
            char c = sql.charAt(i);

            // 处理注释
            if (c == '-' && i + 1 < len && sql.charAt(i + 1) == '-') {
                // 单行注释
                int end = sql.indexOf('\n', i);
                if (end == -1) end = len;
                textFlow.getChildren().add(createText(sql.substring(i, end), COMMENT_COLOR));
                i = end;
                continue;
            }

            if (c == '/' && i + 1 < len && sql.charAt(i + 1) == '*') {
                // 多行注释
                int end = sql.indexOf("*/", i + 2);
                if (end == -1) end = len;
                else end += 2;
                textFlow.getChildren().add(createText(sql.substring(i, end), COMMENT_COLOR));
                i = end;
                continue;
            }

            // 处理字符串
            if (c == '\'' || c == '"') {
                int end = sql.indexOf(c, i + 1);
                if (end == -1) end = len;
                else end++;
                textFlow.getChildren().add(createText(sql.substring(i, end), STRING_COLOR));
                i = end;
                continue;
            }

            // 处理数字
            if (Character.isDigit(c)) {
                int start = i;
                while (i < len && (Character.isDigit(sql.charAt(i)) || sql.charAt(i) == '.')) {
                    i++;
                }
                textFlow.getChildren().add(createText(sql.substring(start, i), NUMBER_COLOR));
                continue;
            }

            // 处理标识符和关键字
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                String token = sql.substring(start, i);
                String upperToken = token.toUpperCase();

                if (KEYWORDS.contains(upperToken)) {
                    textFlow.getChildren().add(createText(token, KEYWORD_COLOR));
                } else if (FUNCTIONS.contains(upperToken)) {
                    textFlow.getChildren().add(createText(token, FUNCTION_COLOR));
                } else {
                    textFlow.getChildren().add(createText(token, DEFAULT_COLOR));
                }
                continue;
            }

            // 处理运算符
            if (c == '=' || c == '<' || c == '>' || c == '!' || c == '+' || c == '-' ||
                c == '*' || c == '/' || c == '%' || c == '&' || c == '|' || c == '^') {
                textFlow.getChildren().add(createText(String.valueOf(c), OPERATOR_COLOR));
                i++;
                continue;
            }

            // 其他字符（空格、换行、标点等）
            textFlow.getChildren().add(createText(String.valueOf(c), DEFAULT_COLOR));
            i++;
        }

        return textFlow;
    }

    /**
     * 创建带颜色的文本
     */
    private static Text createText(String content, String color) {
        Text text = new Text(content);
        text.setFill(javafx.scene.paint.Color.web(color));
        text.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px;");
        return text;
    }

    /**
     * 获取高亮后的纯文本（用于复制）
     */
    public static String getPlainText(TextFlow textFlow) {
        StringBuilder sb = new StringBuilder();
        for (javafx.scene.Node node : textFlow.getChildren()) {
            if (node instanceof Text) {
                sb.append(((Text) node).getText());
            }
        }
        return sb.toString();
    }
}
