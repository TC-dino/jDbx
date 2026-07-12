package dino.jdbx.core.api;

import java.util.List;

/**
 * 元数据接口
 */
public interface Metadata {

    /**
     * 获取表名
     */
    String getTableName();

    /**
     * 获取列信息
     */
    List<Column> getColumns();

    /**
     * 获取索引信息
     */
    List<Index> getIndexes();

    /**
     * 获取外键信息
     */
    List<ForeignKey> getForeignKeys();

    /**
     * 列信息
     */
    class Column {
        private String name;
        private String type;
        private boolean nullable;
        private String defaultValue;
        private boolean primaryKey;
        private String comment;

        public Column(String name, String type, boolean nullable, String defaultValue, boolean primaryKey, String comment) {
            this.name = name;
            this.type = type;
            this.nullable = nullable;
            this.defaultValue = defaultValue;
            this.primaryKey = primaryKey;
            this.comment = comment;
        }

        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public boolean isNullable() { return nullable; }
        public String getDefaultValue() { return defaultValue; }
        public boolean isPrimaryKey() { return primaryKey; }
        public String getComment() { return comment; }
    }

    /**
     * 索引信息
     */
    class Index {
        private String name;
        private List<String> columns;
        private boolean unique;

        public Index(String name, List<String> columns, boolean unique) {
            this.name = name;
            this.columns = columns;
            this.unique = unique;
        }

        // Getters
        public String getName() { return name; }
        public List<String> getColumns() { return columns; }
        public boolean isUnique() { return unique; }
    }

    /**
     * 外键信息
     */
    class ForeignKey {
        private String name;
        private String column;
        private String referencedTable;
        private String referencedColumn;

        public ForeignKey(String name, String column, String referencedTable, String referencedColumn) {
            this.name = name;
            this.column = column;
            this.referencedTable = referencedTable;
            this.referencedColumn = referencedColumn;
        }

        // Getters
        public String getName() { return name; }
        public String getColumn() { return column; }
        public String getReferencedTable() { return referencedTable; }
        public String getReferencedColumn() { return referencedColumn; }
    }
}