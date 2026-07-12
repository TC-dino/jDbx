package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据导入工具
 */
public class DataImporter {

    /**
     * 从 CSV 文件导入数据
     */
    public static ImportResult importFromCsv(Window owner, Connection connection, String tableName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择 CSV 文件");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV 文件", "*.csv")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file != null) {
            try {
                return importFromCsv(connection, tableName, file);
            } catch (Exception e) {
                return new ImportResult(0, 0, e.getMessage());
            }
        }
        return null;
    }

    /**
     * 从 CSV 文件导入数据
     */
    public static ImportResult importFromCsv(Connection connection, String tableName, File file) throws Exception {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // 读取表头
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isEmpty()) {
                return new ImportResult(0, 0, "文件为空");
            }

            String[] headers = parseCsvLine(headerLine);

            // 读取数据行
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    String[] values = parseCsvLine(line);
                    if (values.length != headers.length) {
                        errors.add("第 " + lineNumber + " 行: 列数不匹配");
                        failCount++;
                        continue;
                    }

                    // 构建 INSERT 语句
                    StringBuilder sql = new StringBuilder();
                    sql.append("INSERT INTO ").append(tableName).append(" (");
                    sql.append(String.join(", ", headers));
                    sql.append(") VALUES (");

                    List<String> escapedValues = new ArrayList<>();
                    for (String value : values) {
                        if (value.equalsIgnoreCase("NULL") || value.isEmpty()) {
                            escapedValues.add("NULL");
                        } else {
                            escapedValues.add("'" + value.replace("'", "''") + "'");
                        }
                    }
                    sql.append(String.join(", ", escapedValues));
                    sql.append(")");

                    connection.executeUpdate(sql.toString());
                    successCount++;
                } catch (Exception e) {
                    errors.add("第 " + lineNumber + " 行: " + e.getMessage());
                    failCount++;
                }
            }
        }

        String errorMessage = errors.isEmpty() ? null : String.join("\n", errors.subList(0, Math.min(10, errors.size())));
        return new ImportResult(successCount, failCount, errorMessage);
    }

    /**
     * 解析 CSV 行
     */
    private static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    values.add(current.toString().trim());
                    current = new StringBuilder();
                } else {
                    current.append(c);
                }
            }
        }
        values.add(current.toString().trim());

        return values.toArray(new String[0]);
    }

    /**
     * 导入结果
     */
    public static class ImportResult {
        private final int successCount;
        private final int failCount;
        private final String errorMessage;

        public ImportResult(int successCount, int failCount, String errorMessage) {
            this.successCount = successCount;
            this.failCount = failCount;
            this.errorMessage = errorMessage;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public int getFailCount() {
            return failCount;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean hasErrors() {
            return errorMessage != null && !errorMessage.isEmpty();
        }

        @Override
        public String toString() {
            if (failCount == 0) {
                return "成功导入 " + successCount + " 行数据";
            } else {
                return "成功导入 " + successCount + " 行，失败 " + failCount + " 行";
            }
        }
    }
}
