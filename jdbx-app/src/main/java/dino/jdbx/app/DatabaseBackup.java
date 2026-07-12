package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.Metadata;
import dino.jdbx.core.api.QueryResult;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 数据库备份工具
 */
public class DatabaseBackup {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /**
     * 备份数据库结构
     */
    public static void backupStructure(Window owner, Connection connection) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存数据库结构备份");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL 文件", "*.sql")
        );
        fileChooser.setInitialFileName("backup_structure_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".sql");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                backupStructure(file, connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 备份数据库结构
     */
    public static void backupStructure(File file, Connection connection) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("-- jDbx 数据库结构备份");
            writer.println("-- 时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- 数据库: " + connection.getCurrentDatabase());
            writer.println();

            // 获取所有表
            List<String> tables = connection.getTables();
            for (String table : tables) {
                writer.println("-- 表: " + table);
                writer.println(generateCreateTableSql(connection, table));
                writer.println();
            }

            // 获取所有视图
            List<String> views = connection.getViews();
            for (String view : views) {
                writer.println("-- 视图: " + view);
                writer.println(generateCreateViewSql(connection, view));
                writer.println();
            }
        }
    }

    /**
     * 备份数据库数据
     */
    public static void backupData(Window owner, Connection connection) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存数据库数据备份");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL 文件", "*.sql")
        );
        fileChooser.setInitialFileName("backup_data_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".sql");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                backupData(file, connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 备份数据库数据
     */
    public static void backupData(File file, Connection connection) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("-- jDbx 数据库数据备份");
            writer.println("-- 时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- 数据库: " + connection.getCurrentDatabase());
            writer.println();

            // 禁用外键检查
            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println();

            // 获取所有表
            List<String> tables = connection.getTables();
            for (String table : tables) {
                writer.println("-- 表: " + table);
                writer.println("TRUNCATE TABLE " + table + ";");

                // 获取表数据
                QueryResult result = connection.executeQuery("SELECT * FROM " + table);
                if (result != null && !result.hasError()) {
                    List<String> columns = result.getColumnNames();
                    List<Map<String, Object>> rows = result.getRows();

                    for (Map<String, Object> row : rows) {
                        StringBuilder sql = new StringBuilder();
                        sql.append("INSERT INTO ").append(table).append(" (");
                        sql.append(String.join(", ", columns));
                        sql.append(") VALUES (");

                        List<String> values = new ArrayList<>();
                        for (String col : columns) {
                            Object value = row.get(col);
                            if (value == null) {
                                values.add("NULL");
                            } else if (value instanceof String) {
                                values.add("'" + ((String) value).replace("'", "''") + "'");
                            } else if (value instanceof Boolean) {
                                values.add((Boolean) value ? "TRUE" : "FALSE");
                            } else {
                                values.add(value.toString());
                            }
                        }
                        sql.append(String.join(", ", values));
                        sql.append(");");

                        writer.println(sql.toString());
                    }
                }
                writer.println();
            }

            // 启用外键检查
            writer.println("SET FOREIGN_KEY_CHECKS = 1;");
        }
    }

    /**
     * 完整备份（结构 + 数据）
     */
    public static void backupFull(Window owner, Connection connection) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("保存完整数据库备份");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL 文件", "*.sql")
        );
        fileChooser.setInitialFileName("backup_full_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".sql");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                backupFull(file, connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 完整备份
     */
    public static void backupFull(File file, Connection connection) throws Exception {
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("-- jDbx 完整数据库备份");
            writer.println("-- 时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            writer.println("-- 数据库: " + connection.getCurrentDatabase());
            writer.println();

            // 禁用外键检查
            writer.println("SET FOREIGN_KEY_CHECKS = 0;");
            writer.println();

            // 获取所有表
            List<String> tables = connection.getTables();
            for (String table : tables) {
                writer.println("-- 表: " + table);

                // 创建表结构
                writer.println(generateCreateTableSql(connection, table));
                writer.println();

                // 插入数据
                QueryResult result = connection.executeQuery("SELECT * FROM " + table);
                if (result != null && !result.hasError()) {
                    List<String> columns = result.getColumnNames();
                    List<Map<String, Object>> rows = result.getRows();

                    if (!rows.isEmpty()) {
                        writer.println("INSERT INTO " + table + " (" + String.join(", ", columns) + ") VALUES");
                        for (int i = 0; i < rows.size(); i++) {
                            Map<String, Object> row = rows.get(i);
                            StringBuilder values = new StringBuilder();
                            values.append("(");

                            List<String> valueList = new ArrayList<>();
                            for (String col : columns) {
                                Object value = row.get(col);
                                if (value == null) {
                                    valueList.add("NULL");
                                } else if (value instanceof String) {
                                    valueList.add("'" + ((String) value).replace("'", "''") + "'");
                                } else if (value instanceof Boolean) {
                                    valueList.add((Boolean) value ? "TRUE" : "FALSE");
                                } else {
                                    valueList.add(value.toString());
                                }
                            }
                            values.append(String.join(", ", valueList));
                            values.append(")");

                            if (i < rows.size() - 1) {
                                writer.println(values.toString() + ",");
                            } else {
                                writer.println(values.toString() + ";");
                            }
                        }
                    }
                }
                writer.println();
            }

            // 启用外键检查
            writer.println("SET FOREIGN_KEY_CHECKS = 1;");
        }
    }

    /**
     * 生成 CREATE TABLE SQL
     */
    private static String generateCreateTableSql(Connection connection, String tableName) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

        Metadata metadata = connection.getTableMetadata(tableName);
        if (metadata != null) {
            List<Metadata.Column> columns = metadata.getColumns();
            for (int i = 0; i < columns.size(); i++) {
                Metadata.Column col = columns.get(i);
                sql.append("    ").append(col.getName()).append(" ").append(col.getType());

                if (!col.isNullable()) {
                    sql.append(" NOT NULL");
                }

                if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
                    sql.append(" DEFAULT ").append(col.getDefaultValue());
                }

                if (i < columns.size() - 1) {
                    sql.append(",");
                }
                sql.append("\n");
            }
        }

        sql.append(");");
        return sql.toString();
    }

    /**
     * 生成 CREATE VIEW SQL
     */
    private static String generateCreateViewSql(Connection connection, String viewName) {
        // 这里简化处理，实际应该获取视图的定义
        return "CREATE VIEW " + viewName + " AS\n-- 视图定义需要手动添加\nSELECT 1;";
    }

    /**
     * 恢复数据库
     */
    public static void restore(Window owner, Connection connection) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择备份文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL 文件", "*.sql")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            try {
                restore(file, connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 恢复数据库
     */
    public static void restore(File file, Connection connection) throws Exception {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder currentStatement = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // 跳过注释和空行
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                currentStatement.append(line).append(" ");

                // 检查是否是完整语句
                if (line.endsWith(";")) {
                    String sql = currentStatement.toString().trim();
                    if (!sql.isEmpty()) {
                        try {
                            connection.executeUpdate(sql);
                        } catch (Exception e) {
                            System.err.println("执行失败: " + sql + "\n错误: " + e.getMessage());
                        }
                    }
                    currentStatement = new StringBuilder();
                }
            }

            // 执行最后一个语句（如果没有分号结尾）
            if (currentStatement.length() > 0) {
                String sql = currentStatement.toString().trim();
                if (!sql.isEmpty()) {
                    try {
                        connection.executeUpdate(sql);
                    } catch (Exception e) {
                        System.err.println("执行失败: " + sql + "\n错误: " + e.getMessage());
                    }
                }
            }
        }
    }
}
