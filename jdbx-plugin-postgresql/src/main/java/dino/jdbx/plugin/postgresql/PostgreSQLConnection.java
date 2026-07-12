package dino.jdbx.plugin.postgresql;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * PostgreSQL 连接实现
 */
public class PostgreSQLConnection implements dino.jdbx.core.api.Connection {

    private static final Logger logger = LoggerFactory.getLogger(PostgreSQLConnection.class);

    private String id;
    private String name;
    private ConnectionConfig config;
    private java.sql.Connection connection;
    private boolean connected;
    private String currentDatabase;
    private String currentSchema;

    public PostgreSQLConnection(ConnectionConfig config) throws Exception {
        this.id = config.getId();
        this.name = config.getName();
        this.config = config;
        this.currentDatabase = config.getDatabase();
        this.currentSchema = "public";
        connect();
    }

    private void connect() throws Exception {
        try {
            Class.forName("org.postgresql.Driver");

            String url = config.getUrl();
            Properties props = new Properties();
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                props.setProperty("user", config.getUsername());
            }
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                props.setProperty("password", config.getPassword());
            }
            if (config.isUseSsl()) {
                props.setProperty("ssl", "true");
            }

            connection = DriverManager.getConnection(url, props);
            connected = true;
            logger.info("已连接到 PostgreSQL: {}", config.getName());
        } catch (Exception e) {
            logger.error("连接 PostgreSQL 失败: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }

    @Override
    public boolean isConnected() {
        try {
            return connected && connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public String getCurrentDatabase() {
        return currentDatabase;
    }

    @Override
    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT datname FROM pg_database WHERE datistemplate = false ORDER BY datname")) {
            while (rs.next()) {
                databases.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error("获取数据库列表失败: {}", e.getMessage());
        }
        return databases;
    }

    @Override
    public void useDatabase(String database) {
        // PostgreSQL 需要重新连接来切换数据库
        try {
            connection.close();
            config.setDatabase(database);
            this.currentDatabase = database;
            connect();
        } catch (Exception e) {
            logger.error("切换数据库失败: {}", e.getMessage());
        }
    }

    @Override
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT table_name FROM information_schema.tables " +
                 "WHERE table_schema = '" + currentSchema + "' AND table_type = 'BASE TABLE' " +
                 "ORDER BY table_name")) {
            while (rs.next()) {
                tables.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error("获取表列表失败: {}", e.getMessage());
        }
        return tables;
    }

    @Override
    public List<String> getViews() {
        List<String> views = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT table_name FROM information_schema.views " +
                 "WHERE table_schema = '" + currentSchema + "' " +
                 "ORDER BY table_name")) {
            while (rs.next()) {
                views.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error("获取视图列表失败: {}", e.getMessage());
        }
        return views;
    }

    @Override
    public Metadata getTableMetadata(String table) {
        try {
            List<Metadata.Column> columns = new ArrayList<>();
            List<Metadata.Index> indexes = new ArrayList<>();
            List<Metadata.ForeignKey> foreignKeys = new ArrayList<>();

            // 获取主键
            Set<String> primaryKeys = new HashSet<>();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT kcu.column_name " +
                     "FROM information_schema.table_constraints tc " +
                     "JOIN information_schema.key_column_usage kcu " +
                     "ON tc.constraint_name = kcu.constraint_name " +
                     "WHERE tc.table_schema = '" + currentSchema + "' " +
                     "AND tc.table_name = '" + table + "' " +
                     "AND tc.constraint_type = 'PRIMARY KEY'")) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString(1));
                }
            }

            // 获取列信息
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT column_name, data_type, is_nullable, column_default, " +
                     "character_maximum_length, numeric_precision " +
                     "FROM information_schema.columns " +
                     "WHERE table_schema = '" + currentSchema + "' " +
                     "AND table_name = '" + table + "' " +
                     "ORDER BY ordinal_position")) {
                while (rs.next()) {
                    String colName = rs.getString("column_name");
                    String colType = rs.getString("data_type");
                    boolean nullable = "YES".equals(rs.getString("is_nullable"));
                    String defaultValue = rs.getString("column_default");
                    boolean isPrimaryKey = primaryKeys.contains(colName);
                    String comment = ""; // PostgreSQL 需要单独查询注释

                    Metadata.Column column = new Metadata.Column(colName, colType, nullable, defaultValue, isPrimaryKey, comment);
                    columns.add(column);
                }
            }

            // 获取索引信息
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT indexname, indexdef FROM pg_indexes " +
                     "WHERE schemaname = '" + currentSchema + "' " +
                     "AND tablename = '" + table + "'")) {
                while (rs.next()) {
                    String indexName = rs.getString("indexname");
                    // 简化处理，实际需要解析 indexdef 获取列信息
                    List<String> indexColumns = new ArrayList<>();
                    Metadata.Index index = new Metadata.Index(indexName, indexColumns, false);
                    indexes.add(index);
                }
            }

            // 获取外键信息
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(
                     "SELECT tc.constraint_name, kcu.column_name, " +
                     "ccu.table_name AS referenced_table, ccu.column_name AS referenced_column " +
                     "FROM information_schema.table_constraints tc " +
                     "JOIN information_schema.key_column_usage kcu " +
                     "ON tc.constraint_name = kcu.constraint_name " +
                     "JOIN information_schema.constraint_column_usage ccu " +
                     "ON ccu.constraint_name = tc.constraint_name " +
                     "WHERE tc.table_schema = '" + currentSchema + "' " +
                     "AND tc.table_name = '" + table + "' " +
                     "AND tc.constraint_type = 'FOREIGN KEY'")) {
                while (rs.next()) {
                    String fkName = rs.getString("constraint_name");
                    String fkColumn = rs.getString("column_name");
                    String pkTable = rs.getString("referenced_table");
                    String pkColumn = rs.getString("referenced_column");

                    Metadata.ForeignKey fk = new Metadata.ForeignKey(fkName, fkColumn, pkTable, pkColumn);
                    foreignKeys.add(fk);
                }
            }

            return new DefaultMetadata(table, columns, indexes, foreignKeys);
        } catch (SQLException e) {
            logger.error("获取表元数据失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        long startTime = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            List<String> columnNames = new ArrayList<>();
            List<String> columnTypes = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(meta.getColumnName(i));
                columnTypes.add(meta.getColumnTypeName(i));
            }

            List<Map<String, Object>> rows = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(columnNames.get(i - 1), rs.getObject(i));
                }
                rows.add(row);
            }

            long executionTime = System.currentTimeMillis() - startTime;
            return new PostgreSQLQueryResult(columnNames, columnTypes, rows, executionTime);
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new PostgreSQLQueryResult(e.getMessage(), executionTime);
        }
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return new PostgreSQLQueryExecutor(this);
    }

    @Override
    public Object getNativeConnection() {
        return connection;
    }

    @Override
    public boolean isValid() {
        try {
            return connection != null && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connected = false;
            logger.info("已关闭 PostgreSQL 连接: {}", name);
        }
    }

    /**
     * 默认元数据实现
     */
    private static class DefaultMetadata implements Metadata {
        private final String tableName;
        private final List<Column> columns;
        private final List<Index> indexes;
        private final List<ForeignKey> foreignKeys;

        DefaultMetadata(String tableName, List<Column> columns, List<Index> indexes, List<ForeignKey> foreignKeys) {
            this.tableName = tableName;
            this.columns = columns;
            this.indexes = indexes;
            this.foreignKeys = foreignKeys;
        }

        @Override
        public String getTableName() { return tableName; }

        @Override
        public List<Column> getColumns() { return columns; }

        @Override
        public List<Index> getIndexes() { return indexes; }

        @Override
        public List<ForeignKey> getForeignKeys() { return foreignKeys; }
    }
}
