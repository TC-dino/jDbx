package dino.jdbx.plugin.sqlite;

import dino.jdbx.core.api.*;

import java.util.*;

/**
 * SQLite 连接实现
 */
public class SQLiteConnection implements dino.jdbx.core.api.Connection {

    private final ConnectionConfig config;
    private java.sql.Connection connection;
    private boolean connected = false;
    private String currentDatabase;

    public SQLiteConnection(ConnectionConfig config) throws Exception {
        this.config = config;
        this.currentDatabase = config.getDatabase();
        connect();
    }

    private void connect() throws Exception {
        try {
            Class.forName("org.sqlite.JDBC");
            String url = config.getUrl();
            connection = java.sql.DriverManager.getConnection(url);
            connected = true;
        } catch (Exception e) {
            connected = false;
            throw new Exception("无法连接到 SQLite 数据库: " + e.getMessage(), e);
        }
    }

    @Override
    public String getId() {
        return config.getId();
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public String getDatabaseType() {
        return "SQLite";
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getCurrentDatabase() {
        return currentDatabase;
    }

    @Override
    public List<String> getDatabases() {
        // SQLite 只有一个数据库
        return List.of(currentDatabase);
    }

    @Override
    public void useDatabase(String database) {
        // SQLite 不支持切换数据库
        throw new UnsupportedOperationException("SQLite 不支持切换数据库");
    }

    @Override
    public List<String> getTables() {
        List<String> tables = new ArrayList<>();
        try {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            java.sql.ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"});
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    @Override
    public List<String> getViews() {
        List<String> views = new ArrayList<>();
        try {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();
            java.sql.ResultSet rs = metaData.getTables(null, null, "%", new String[]{"VIEW"});
            while (rs.next()) {
                views.add(rs.getString("TABLE_NAME"));
            }
            rs.close();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
        return views;
    }

    @Override
    public Metadata getTableMetadata(String table) {
        try {
            java.sql.DatabaseMetaData metaData = connection.getMetaData();

            // 获取列信息
            List<Metadata.Column> columns = new ArrayList<>();
            java.sql.ResultSet rs = metaData.getColumns(null, null, table, "%");
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String columnType = rs.getString("TYPE_NAME");
                boolean nullable = rs.getInt("NULLABLE") == java.sql.DatabaseMetaData.columnNullable;
                String defaultValue = rs.getString("COLUMN_DEF");
                boolean primaryKey = false;
                String comment = rs.getString("REMARKS");

                // 检查是否是主键
                java.sql.ResultSet pkRs = metaData.getPrimaryKeys(null, null, table);
                while (pkRs.next()) {
                    if (pkRs.getString("COLUMN_NAME").equals(columnName)) {
                        primaryKey = true;
                        break;
                    }
                }
                pkRs.close();

                columns.add(new Metadata.Column(columnName, columnType, nullable, defaultValue, primaryKey, comment));
            }
            rs.close();

            // 获取索引信息
            List<Metadata.Index> indexes = new ArrayList<>();
            java.sql.ResultSet indexRs = metaData.getIndexInfo(null, null, table, false, false);
            Map<String, List<String>> indexColumns = new HashMap<>();
            Map<String, Boolean> indexUnique = new HashMap<>();
            while (indexRs.next()) {
                String indexName = indexRs.getString("INDEX_NAME");
                if (indexName != null) {
                    String columnName = indexRs.getString("COLUMN_NAME");
                    boolean nonUnique = indexRs.getBoolean("NON_UNIQUE");
                    indexColumns.computeIfAbsent(indexName, k -> new ArrayList<>()).add(columnName);
                    indexUnique.put(indexName, !nonUnique);
                }
            }
            indexRs.close();
            for (Map.Entry<String, List<String>> entry : indexColumns.entrySet()) {
                indexes.add(new Metadata.Index(entry.getKey(), entry.getValue(), indexUnique.get(entry.getKey())));
            }

            // 获取外键信息
            List<Metadata.ForeignKey> foreignKeys = new ArrayList<>();
            java.sql.ResultSet fkRs = metaData.getImportedKeys(null, null, table);
            while (fkRs.next()) {
                String fkName = fkRs.getString("FK_NAME");
                String fkColumn = fkRs.getString("FKCOLUMN_NAME");
                String pkTable = fkRs.getString("PKTABLE_NAME");
                String pkColumn = fkRs.getString("PKCOLUMN_NAME");
                foreignKeys.add(new Metadata.ForeignKey(fkName, fkColumn, pkTable, pkColumn));
            }
            fkRs.close();

            return new Metadata() {
                @Override
                public String getTableName() { return table; }

                @Override
                public List<Column> getColumns() { return columns; }

                @Override
                public List<Index> getIndexes() { return indexes; }

                @Override
                public List<ForeignKey> getForeignKeys() { return foreignKeys; }
            };
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            java.sql.Statement stmt = connection.createStatement();
            stmt.setQueryTimeout(30);

            if (sql.trim().toUpperCase().startsWith("SELECT") || sql.trim().toUpperCase().startsWith("PRAGMA")) {
                java.sql.ResultSet rs = stmt.executeQuery(sql);
                java.sql.ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();

                List<String> columnNames = new ArrayList<>();
                List<String> columnTypes = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    columnNames.add(metaData.getColumnName(i));
                    columnTypes.add(metaData.getColumnTypeName(i));
                }

                List<Map<String, Object>> rows = new ArrayList<>();
                int rowCount = 0;
                int maxRows = 10000;
                while (rs.next() && rowCount < maxRows) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(columnNames.get(i - 1), rs.getObject(i));
                    }
                    rows.add(row);
                    rowCount++;
                }
                rs.close();
                stmt.close();

                long executionTime = System.currentTimeMillis() - startTime;
                return new SQLiteQueryResult(columnNames, columnTypes, rows, executionTime);
            } else {
                int affectedRows = stmt.executeUpdate(sql);
                stmt.close();

                long executionTime = System.currentTimeMillis() - startTime;
                return new SQLiteQueryResult(affectedRows, executionTime);
            }
        } catch (java.sql.SQLException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new SQLiteQueryResult(e.getMessage(), executionTime);
        }
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        try (java.sql.Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        }
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return new SQLiteQueryExecutor(this);
    }

    @Override
    public Object getNativeConnection() {
        return connection;
    }

    @Override
    public boolean isValid() {
        try {
            return connection != null && !connection.isClosed();
        } catch (java.sql.SQLException e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            connected = false;
        }
    }
}