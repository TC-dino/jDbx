package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.Metadata;

import java.util.*;

/**
 * 数据库比较工具
 */
public class DatabaseComparator {

    /**
     * 比较结果
     */
    public static class CompareResult {
        private List<String> onlyInSource;
        private List<String> onlyInTarget;
        private List<TableDifference> differences;
        private List<String> identical;

        public CompareResult() {
            this.onlyInSource = new ArrayList<>();
            this.onlyInTarget = new ArrayList<>();
            this.differences = new ArrayList<>();
            this.identical = new ArrayList<>();
        }

        public List<String> getOnlyInSource() { return onlyInSource; }
        public List<String> getOnlyInTarget() { return onlyInTarget; }
        public List<TableDifference> getDifferences() { return differences; }
        public List<String> getIdentical() { return identical; }

        public boolean hasDifferences() {
            return !onlyInSource.isEmpty() || !onlyInTarget.isEmpty() || !differences.isEmpty();
        }

        public String toSummary() {
            StringBuilder sb = new StringBuilder();
            sb.append("比较结果摘要:\n");
            sb.append("- 仅在源数据库: ").append(onlyInSource.size()).append(" 个表\n");
            sb.append("- 仅在目标数据库: ").append(onlyInTarget.size()).append(" 个表\n");
            sb.append("- 结构不同: ").append(differences.size()).append(" 个表\n");
            sb.append("- 结构相同: ").append(identical.size()).append(" 个表\n");
            return sb.toString();
        }
    }

    /**
     * 表差异
     */
    public static class TableDifference {
        private String tableName;
        private List<ColumnDifference> columnDifferences;
        private List<String> onlyInSourceColumns;
        private List<String> onlyInTargetColumns;

        public TableDifference(String tableName) {
            this.tableName = tableName;
            this.columnDifferences = new ArrayList<>();
            this.onlyInSourceColumns = new ArrayList<>();
            this.onlyInTargetColumns = new ArrayList<>();
        }

        public String getTableName() { return tableName; }
        public List<ColumnDifference> getColumnDifferences() { return columnDifferences; }
        public List<String> getOnlyInSourceColumns() { return onlyInSourceColumns; }
        public List<String> getOnlyInTargetColumns() { return onlyInTargetColumns; }

        public String toDetail() {
            StringBuilder sb = new StringBuilder();
            sb.append("表: ").append(tableName).append("\n");

            if (!onlyInSourceColumns.isEmpty()) {
                sb.append("  仅在源表:\n");
                for (String col : onlyInSourceColumns) {
                    sb.append("    - ").append(col).append("\n");
                }
            }

            if (!onlyInTargetColumns.isEmpty()) {
                sb.append("  仅在目标表:\n");
                for (String col : onlyInTargetColumns) {
                    sb.append("    - ").append(col).append("\n");
                }
            }

            if (!columnDifferences.isEmpty()) {
                sb.append("  列差异:\n");
                for (ColumnDifference diff : columnDifferences) {
                    sb.append("    - ").append(diff.toString()).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * 列差异
     */
    public static class ColumnDifference {
        private String columnName;
        private String sourceType;
        private String targetType;
        private Boolean sourceNullable;
        private Boolean targetNullable;
        private String sourceDefault;
        private String targetDefault;

        public ColumnDifference(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() { return columnName; }
        public void setSourceType(String type) { this.sourceType = type; }
        public void setTargetType(String type) { this.targetType = type; }
        public void setSourceNullable(Boolean nullable) { this.sourceNullable = nullable; }
        public void setTargetNullable(Boolean nullable) { this.targetNullable = nullable; }
        public void setSourceDefault(String defaultVal) { this.sourceDefault = defaultVal; }
        public void setTargetDefault(String defaultVal) { this.targetDefault = defaultVal; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(columnName).append(": ");

            List<String> diffs = new ArrayList<>();
            if (!Objects.equals(sourceType, targetType)) {
                diffs.add("类型 " + sourceType + " -> " + targetType);
            }
            if (!Objects.equals(sourceNullable, targetNullable)) {
                diffs.add("可空 " + sourceNullable + " -> " + targetNullable);
            }
            if (!Objects.equals(sourceDefault, targetDefault)) {
                diffs.add("默认值 " + sourceDefault + " -> " + targetDefault);
            }

            sb.append(String.join(", ", diffs));
            return sb.toString();
        }
    }

    /**
     * 比较两个数据库
     */
    public static CompareResult compare(Connection source, Connection target) {
        CompareResult result = new CompareResult();

        // 获取两个数据库的表
        List<String> sourceTables = source.getTables();
        List<String> targetTables = target.getTables();

        // 找出仅在源数据库的表
        for (String table : sourceTables) {
            if (!targetTables.contains(table)) {
                result.getOnlyInSource().add(table);
            }
        }

        // 找出仅在目标数据库的表
        for (String table : targetTables) {
            if (!sourceTables.contains(table)) {
                result.getOnlyInTarget().add(table);
            }
        }

        // 比较共同表的结构
        for (String table : sourceTables) {
            if (targetTables.contains(table)) {
                TableDifference diff = compareTable(source, target, table);
                if (diff.getColumnDifferences().isEmpty() &&
                    diff.getOnlyInSourceColumns().isEmpty() &&
                    diff.getOnlyInTargetColumns().isEmpty()) {
                    result.getIdentical().add(table);
                } else {
                    result.getDifferences().add(diff);
                }
            }
        }

        return result;
    }

    /**
     * 比较两个表的结构
     */
    private static TableDifference compareTable(Connection source, Connection target, String tableName) {
        TableDifference diff = new TableDifference(tableName);

        Metadata sourceMeta = source.getTableMetadata(tableName);
        Metadata targetMeta = target.getTableMetadata(tableName);

        if (sourceMeta == null || targetMeta == null) {
            return diff;
        }

        // 获取列信息
        Map<String, Metadata.Column> sourceColumns = new LinkedHashMap<>();
        for (Metadata.Column col : sourceMeta.getColumns()) {
            sourceColumns.put(col.getName(), col);
        }

        Map<String, Metadata.Column> targetColumns = new LinkedHashMap<>();
        for (Metadata.Column col : targetMeta.getColumns()) {
            targetColumns.put(col.getName(), col);
        }

        // 找出仅在源表的列
        for (String colName : sourceColumns.keySet()) {
            if (!targetColumns.containsKey(colName)) {
                diff.getOnlyInSourceColumns().add(colName);
            }
        }

        // 找出仅在目标表的列
        for (String colName : targetColumns.keySet()) {
            if (!sourceColumns.containsKey(colName)) {
                diff.getOnlyInTargetColumns().add(colName);
            }
        }

        // 比较共同列
        for (String colName : sourceColumns.keySet()) {
            if (targetColumns.containsKey(colName)) {
                Metadata.Column sourceCol = sourceColumns.get(colName);
                Metadata.Column targetCol = targetColumns.get(colName);

                ColumnDifference colDiff = compareColumn(sourceCol, targetCol);
                if (colDiff != null) {
                    diff.getColumnDifferences().add(colDiff);
                }
            }
        }

        return diff;
    }

    /**
     * 比较两个列
     */
    private static ColumnDifference compareColumn(Metadata.Column source, Metadata.Column target) {
        ColumnDifference diff = new ColumnDifference(source.getName());

        boolean hasDiff = false;

        if (!Objects.equals(source.getType(), target.getType())) {
            diff.setSourceType(source.getType());
            diff.setTargetType(target.getType());
            hasDiff = true;
        }

        if (source.isNullable() != target.isNullable()) {
            diff.setSourceNullable(source.isNullable());
            diff.setTargetNullable(target.isNullable());
            hasDiff = true;
        }

        if (!Objects.equals(source.getDefaultValue(), target.getDefaultValue())) {
            diff.setSourceDefault(source.getDefaultValue());
            diff.setTargetDefault(target.getDefaultValue());
            hasDiff = true;
        }

        return hasDiff ? diff : null;
    }

    /**
     * 生成同步脚本
     */
    public static String generateSyncScript(CompareResult result, Connection source, Connection target) {
        StringBuilder script = new StringBuilder();
        script.append("-- 数据库同步脚本\n");
        script.append("-- 生成时间: ").append(new java.util.Date()).append("\n\n");

        // 删除仅在目标数据库的表
        for (String table : result.getOnlyInTarget()) {
            script.append("DROP TABLE IF EXISTS ").append(table).append(";\n");
        }

        // 创建仅在源数据库的表
        for (String table : result.getOnlyInSource()) {
            Metadata meta = source.getTableMetadata(table);
            if (meta != null) {
                script.append(generateCreateTableSql(table, meta));
                script.append("\n");
            }
        }

        // 修改有差异的表
        for (TableDifference diff : result.getDifferences()) {
            script.append("-- 表 ").append(diff.getTableName()).append(" 差异\n");

            // 添加缺失的列
            for (String colName : diff.getOnlyInSourceColumns()) {
                Metadata meta = source.getTableMetadata(diff.getTableName());
                if (meta != null) {
                    for (Metadata.Column col : meta.getColumns()) {
                        if (col.getName().equals(colName)) {
                            script.append("ALTER TABLE ").append(diff.getTableName())
                                .append(" ADD COLUMN ").append(colName).append(" ").append(col.getType());
                            if (!col.isNullable()) {
                                script.append(" NOT NULL");
                            }
                            script.append(";\n");
                            break;
                        }
                    }
                }
            }

            // 删除多余的列
            for (String colName : diff.getOnlyInTargetColumns()) {
                script.append("ALTER TABLE ").append(diff.getTableName())
                    .append(" DROP COLUMN ").append(colName).append(";\n");
            }

            // 修改列类型
            for (ColumnDifference colDiff : diff.getColumnDifferences()) {
                if (colDiff.sourceType != null && colDiff.targetType != null) {
                    script.append("ALTER TABLE ").append(diff.getTableName())
                        .append(" MODIFY COLUMN ").append(colDiff.getColumnName())
                        .append(" ").append(colDiff.sourceType).append(";\n");
                }
            }

            script.append("\n");
        }

        return script.toString();
    }

    /**
     * 生成 CREATE TABLE SQL
     */
    private static String generateCreateTableSql(String tableName, Metadata metadata) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ").append(tableName).append(" (\n");

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

        sql.append(");\n");
        return sql.toString();
    }
}
