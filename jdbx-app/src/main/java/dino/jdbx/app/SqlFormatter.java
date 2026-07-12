package dino.jdbx.app;

import java.util.*;

/**
 * SQL 格式化工具
 */
public class SqlFormatter {

    // SQL 关键字
    private static final Set<String> MAIN_CLAUSES = new HashSet<>(Arrays.asList(
        "SELECT", "FROM", "WHERE", "GROUP BY", "HAVING", "ORDER BY",
        "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM",
        "CREATE TABLE", "ALTER TABLE", "DROP TABLE"
    ));

    private static final Set<String> JOIN_CLAUSES = new HashSet<>(Arrays.asList(
        "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN", "OUTER JOIN",
        "FULL JOIN", "CROSS JOIN", "LEFT OUTER JOIN", "RIGHT OUTER JOIN"
    ));

    private static final Set<String> LOGICAL_OPERATORS = new HashSet<>(Arrays.asList(
        "AND", "OR", "NOT"
    ));

    /**
     * 格式化 SQL（美化）
     */
    public static String format(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return sql;
        }

        // 预处理
        sql = sql.trim();
        sql = sql.replaceAll("\\s+", " ");

        // 移除多余空格
        sql = sql.replaceAll("\\s*([(),])\\s*", "$1");
        sql = sql.replaceAll("\\s*=\\s*", " = ");
        sql = sql.replaceAll("\\s*<>\\s*", " <> ");
        sql = sql.replaceAll("\\s*<\\s*", " < ");
        sql = sql.replaceAll("\\s*>\\s*", " > ");
        sql = sql.replaceAll("\\s*<=\\s*", " <= ");
        sql = sql.replaceAll("\\s*>=\\s*", " >= ");

        StringBuilder result = new StringBuilder();
        int indent = 0;
        boolean newLine = false;

        // 分词处理
        String[] tokens = tokenize(sql);
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].toUpperCase();
            String originalToken = tokens[i];

            // 检查是否需要换行
            if (isMainClause(token)) {
                if (i > 0) {
                    result.append("\n");
                }
                result.append(indent(indent));
                result.append(originalToken);
                newLine = true;
            } else if (isJoinClause(token)) {
                result.append("\n");
                result.append(indent(indent));
                result.append(originalToken);
                newLine = true;
            } else if (isLogicalOperator(token)) {
                result.append("\n");
                result.append(indent(indent + 1));
                result.append(originalToken);
                newLine = true;
            } else if (token.equals("(")) {
                result.append(originalToken);
                indent++;
                newLine = false;
            } else if (token.equals(")")) {
                indent--;
                result.append("\n");
                result.append(indent(indent));
                result.append(originalToken);
                newLine = false;
            } else if (token.equals(",")) {
                result.append(originalToken);
                result.append("\n");
                result.append(indent(indent + 1));
                newLine = true;
            } else if (token.equals("CASE")) {
                result.append("\n");
                result.append(indent(indent + 1));
                result.append(originalToken);
                indent++;
                newLine = true;
            } else if (token.equals("END")) {
                indent--;
                result.append("\n");
                result.append(indent(indent + 1));
                result.append(originalToken);
                newLine = false;
            } else if (token.equals("WHEN") || token.equals("THEN") || token.equals("ELSE")) {
                result.append("\n");
                result.append(indent(indent + 1));
                result.append(originalToken);
                newLine = true;
            } else {
                if (newLine) {
                    result.append(" ");
                    newLine = false;
                } else {
                    result.append(" ");
                }
                result.append(originalToken);
            }
        }

        return result.toString().trim();
    }

    /**
     * 压缩 SQL（去除多余空白）
     */
    public static String minify(String sql) {
        if (sql == null) {
            return null;
        }

        // 移除注释
        sql = sql.replaceAll("--[^\n]*", "");
        sql = sql.replaceAll("/\\*[\\s\\S]*?\\*/", "");

        // 移除多余空白
        sql = sql.replaceAll("\\s+", " ");
        sql = sql.trim();

        return sql;
    }

    /**
     * 分词
     */
    private static String[] tokenize(String sql) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (inString) {
                current.append(c);
                if (c == stringChar) {
                    inString = false;
                }
            } else {
                if (c == '\'' || c == '"') {
                    inString = true;
                    stringChar = c;
                    current.append(c);
                } else if (c == ' ' || c == '\t' || c == '\n' || c == '\r') {
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current = new StringBuilder();
                    }
                } else if (c == '(' || c == ')' || c == ',') {
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current = new StringBuilder();
                    }
                    tokens.add(String.valueOf(c));
                } else {
                    current.append(c);
                }
            }
        }

        if (current.length() > 0) {
            tokens.add(current.toString());
        }

        // 合并多字关键字
        return mergeKeywords(tokens);
    }

    /**
     * 合并多字关键字
     */
    private static String[] mergeKeywords(List<String> tokens) {
        List<String> result = new ArrayList<>();
        int i = 0;

        while (i < tokens.size()) {
            String token = tokens.get(i).toUpperCase();

            // 检查两字关键字
            if (i + 1 < tokens.size()) {
                String twoWord = token + " " + tokens.get(i + 1).toUpperCase();
                if (MAIN_CLAUSES.contains(twoWord) || JOIN_CLAUSES.contains(twoWord)) {
                    result.add(tokens.get(i) + " " + tokens.get(i + 1));
                    i += 2;
                    continue;
                }
            }

            // 检查三字关键字
            if (i + 2 < tokens.size()) {
                String threeWord = token + " " + tokens.get(i + 1).toUpperCase() + " " + tokens.get(i + 2).toUpperCase();
                if (JOIN_CLAUSES.contains(threeWord)) {
                    result.add(tokens.get(i) + " " + tokens.get(i + 1) + " " + tokens.get(i + 2));
                    i += 3;
                    continue;
                }
            }

            result.add(tokens.get(i));
            i++;
        }

        return result.toArray(new String[0]);
    }

    /**
     * 是否是主句
     */
    private static boolean isMainClause(String token) {
        return MAIN_CLAUSES.contains(token);
    }

    /**
     * 是否是 JOIN 句
     */
    private static boolean isJoinClause(String token) {
        return JOIN_CLAUSES.contains(token);
    }

    /**
     * 是否是逻辑运算符
     */
    private static boolean isLogicalOperator(String token) {
        return LOGICAL_OPERATORS.contains(token);
    }

    /**
     * 生成缩进
     */
    private static String indent(int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
        return sb.toString();
    }

    /**
     * 高亮关键字（返回 HTML）
     */
    public static String highlightAsHtml(String sql) {
        if (sql == null || sql.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        String[] tokens = tokenize(sql);

        for (String token : tokens) {
            String upperToken = token.toUpperCase();

            if (isMainClause(upperToken) || isJoinClause(upperToken)) {
                result.append("<span style='color: blue; font-weight: bold;'>").append(escapeHtml(token)).append("</span>");
            } else if (isLogicalOperator(upperToken)) {
                result.append("<span style='color: blue;'>").append(escapeHtml(token)).append("</span>");
            } else if (upperToken.equals("NULL") || upperToken.equals("TRUE") || upperToken.equals("FALSE")) {
                result.append("<span style='color: purple;'>").append(escapeHtml(token)).append("</span>");
            } else if (token.startsWith("'") || token.startsWith("\"")) {
                result.append("<span style='color: red;'>").append(escapeHtml(token)).append("</span>");
            } else if (token.matches("\\d+")) {
                result.append("<span style='color: green;'>").append(escapeHtml(token)).append("</span>");
            } else {
                result.append(escapeHtml(token));
            }

            result.append(" ");
        }

        return result.toString().trim();
    }

    /**
     * HTML 转义
     */
    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    /**
     * 转换为大写
     */
    public static String toUpperCase(String sql) {
        if (sql == null) {
            return null;
        }

        // 只转换关键字，不转换字符串内容
        StringBuilder result = new StringBuilder();
        String[] tokens = tokenize(sql);
        boolean inString = false;

        for (String token : tokens) {
            if (token.startsWith("'") || token.startsWith("\"")) {
                result.append(token);
                inString = !inString;
            } else if (inString) {
                result.append(token);
            } else {
                result.append(token.toUpperCase());
            }
            result.append(" ");
        }

        return result.toString().trim();
    }

    /**
     * 转换为小写
     */
    public static String toLowerCase(String sql) {
        if (sql == null) {
            return null;
        }

        // 只转换关键字，不转换字符串内容
        StringBuilder result = new StringBuilder();
        String[] tokens = tokenize(sql);
        boolean inString = false;

        for (String token : tokens) {
            if (token.startsWith("'") || token.startsWith("\"")) {
                result.append(token);
                inString = !inString;
            } else if (inString) {
                result.append(token);
            } else {
                result.append(token.toLowerCase());
            }
            result.append(" ");
        }

        return result.toString().trim();
    }
}
