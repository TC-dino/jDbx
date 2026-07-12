package dino.jdbx.plugin.mysql;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

/**
 * MySQL 连接实现
 */
public class MySQLConnection implements dino.jdbx.core.api.Connection {

    private static final Logger logger = LoggerFactory.getLogger(MySQLConnection.class);

    private String id;
    private String name;
    private ConnectionConfig config;
    private java.sql.Connection connection;
    private boolean connected;
    private String currentDatabase;

    public MySQLConnection(ConnectionConfig config) throws Exception {
        this.id = config.getId();
        this.name = config.getName();
        this.config = config;
        this.currentDatabase = config.getDatabase();
        connect();
    }

    private void connect() throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = config.getUrl();
            Properties props = new Properties();
            if (config.getUsername() != null && !config.getUsername().isEmpty()) {
                props.setProperty("user", config.getUsername());
            }
            if (config.getPassword() != null && !config.getPassword().isEmpty()) {
                props.setProperty("password", config.getPassword());
            }
            props.setProperty("useSSL", String.valueOf(config.isUseSsl()));
            props.setProperty("allowPublicKeyRetrieval", "true");
            props.setProperty("serverTimezone", "UTC");

            connection = DriverManager.getConnection(url, props);
            connected = true;
            logger.info("已连接到 MySQL: {}", config.getName());
        } catch (Exception e) {
            logger.error("连接 MySQL 失败: {}", e.getMessage());
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
        return "MySQL";
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
             ResultSet rs = stmt.executeQuery("SHOW DATABASES")) {
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
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("USE `" + database + "`");
            this.currentDatabase = database;
        } catch (SQLException e) {
            logger.error("切换数据库失败: {}", e.getMessage());
        }
    }

    @Override
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW TABLES")) {
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
             ResultSet rs = stmt.executeQuery("SHOW FULL TABLES WHERE Table_type = 'VIEW'")) {
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
            DatabaseMetaData dbMeta = connection.getMetaData();
            List<Metadata.Column> columns = new ArrayList<>();
            List<Metadata.Index> indexes = new ArrayList<>();
            List<Metadata.ForeignKey> foreignKeys = new ArrayList<>();

            // 获取主键
            Set<String> primaryKeys = new HashSet<>();
            try (ResultSet rs = dbMeta.getPrimaryKeys(null, currentDatabase, table)) {
                while (rs.next()) {
                    primaryKeys.add(rs.getString("COLUMN_NAME"));
                }
            }

            // 获取列信息
            try (ResultSet rs = dbMeta.getColumns(null, currentDatabase, table, null)) {
                while (rs.next()) {
                    String colName = rs.getString("COLUMN_NAME");
                    String colType = rs.getString("TYPE_NAME");
                    boolean nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                    String defaultValue = rs.getString("COLUMN_DEF");
                    boolean isPrimaryKey = primaryKeys.contains(colName);
                    String comment = rs.getString("REMARKS");

                    Metadata.Column column = new Metadata.Column(colName, colType, nullable, defaultValue, isPrimaryKey, comment);
                    columns.add(column);
                }
            }

            // 获取索引信息
            Map<String, List<String>> indexColumnsMap = new LinkedHashMap<>();
            try (ResultSet rs = dbMeta.getIndexInfo(null, currentDatabase, table, false, false)) {
                while (rs.next()) {
                    String indexName = rs.getString("INDEX_NAME");
                    String columnName = rs.getString("COLUMN_NAME");
                    boolean unique = !rs.getBoolean("NON_UNIQUE");

                    indexColumnsMap.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnName);
                }
            }

            // 创建索引对象
            for (Map.Entry<String, List<String>> entry : indexColumnsMap.entrySet()) {
                Metadata.Index index = new Metadata.Index(entry.getKey(), entry.getValue(), false);
                indexes.add(index);
            }

            // 获取外键信息
            try (ResultSet rs = dbMeta.getImportedKeys(null, currentDatabase, table)) {
                while (rs.next()) {
                    String fkName = rs.getString("FK_NAME");
                    String fkColumn = rs.getString("FKCOLUMN_NAME");
                    String pkTable = rs.getString("PKTABLE_NAME");
                    String pkColumn = rs.getString("PKCOLUMN_NAME");

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
            return new MySQLQueryResult(columnNames, columnTypes, rows, executionTime);
        } catch (SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new MySQLQueryResult(e.getMessage(), executionTime);
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
        return new MySQLQueryExecutor(this);
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
            logger.info("已关闭 MySQL 连接: {}", name);
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
