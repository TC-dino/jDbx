package dino.jdbx.app;

import dino.jdbx.core.api.QueryResult;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 数据导出工具
 */
public class DataExporter {

    /**
     * 导出查询结果为 CSV 文件
     */
    public static void exportToCsv(Window owner, QueryResult result) {
        if (result == null || !result.isQuery()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出 CSV 文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV 文件", "*.csv")
        );
        fileChooser.setInitialFileName("export.csv");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                exportToCsv(file, result);
                System.out.println("导出成功: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("导出失败: " + e.getMessage());
            }
        }
    }

    /**
     * 导出查询结果为 CSV 文件
     */
    public static void exportToCsv(File file, QueryResult result) throws IOException {
        if (result == null || !result.isQuery()) {
            return;
        }

        List<String> columns = result.getColumnNames();
        List<Map<String, Object>> rows = result.getRows();

        try (FileWriter writer = new FileWriter(file)) {
            // 写入表头
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    writer.write(",");
                }
                writer.write(escapeCsv(columns.get(i)));
            }
            writer.write("\n");

            // 写入数据行
            for (Map<String, Object> row : rows) {
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) {
                        writer.write(",");
                    }
                    Object value = row.get(columns.get(i));
                    writer.write(escapeCsv(value != null ? value.toString() : "NULL"));
                }
                writer.write("\n");
            }
        }
    }

    /**
     * 导出为 SQL INSERT 语句
     */
    public static void exportToSql(Window owner, QueryResult result, String tableName) {
        if (result == null || !result.isQuery()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出 SQL 文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("SQL 文件", "*.sql")
        );
        fileChooser.setInitialFileName(tableName + ".sql");

        File file = fileChooser.showSaveDialog(owner);
        if (file != null) {
            try {
                exportToSql(file, result, tableName);
                System.out.println("导出成功: " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("导出失败: " + e.getMessage());
            }
        }
    }

    /**
     * 导出为 SQL INSERT 语句
     */
    public static void exportToSql(File file, QueryResult result, String tableName) throws IOException {
        if (result == null || !result.isQuery()) {
            return;
        }

        List<String> columns = result.getColumnNames();
        List<Map<String, Object>> rows = result.getRows();

        try (FileWriter writer = new FileWriter(file)) {
            writer.write("-- 导出表: " + tableName + "\n");
            writer.write("-- 行数: " + rows.size() + "\n\n");

            for (Map<String, Object> row : rows) {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO ").append(tableName).append(" (");

                // 列名
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(columns.get(i));
                }
                sb.append(") VALUES (");

                // 值
                for (int i = 0; i < columns.size(); i++) {
                    if (i > 0) {
                        sb.append(", ");
                    }
                    Object value = row.get(columns.get(i));
                    if (value == null) {
                        sb.append("NULL");
                    } else if (value instanceof String) {
                        sb.append("'").append(((String) value).replace("'", "''")).append("'");
                    } else if (value instanceof Number) {
                        sb.append(value.toString());
                    } else if (value instanceof Boolean) {
                        sb.append((Boolean) value ? "TRUE" : "FALSE");
                    } else {
                        sb.append("'").append(value.toString().replace("'", "''")).append("'");
                    }
                }
                sb.append(");\n");

                writer.write(sb.toString());
            }
        }
    }

    /**
     * CSV 转义
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        // 如果包含逗号、引号或换行符，需要用引号包裹
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
