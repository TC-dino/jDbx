package dino.jdbx.plugin.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import dino.jdbx.core.api.*;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB 连接实现
 */
public class MongoDBConnection implements dino.jdbx.core.api.Connection {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBConnection.class);

    private String id;
    private String name;
    private ConnectionConfig config;
    private MongoClient mongoClient;
    private MongoDatabase mongoDatabase;
    private boolean connected;
    private String currentDatabase;

    public MongoDBConnection(ConnectionConfig config) throws Exception {
        this.id = config.getId();
        this.name = config.getName();
        this.config = config;
        this.currentDatabase = config.getDatabase();
        connect();
    }

    private void connect() throws Exception {
        try {
            String host = config.getHost();
            int port = config.getPort();
            String databaseName = config.getDatabase();
            String username = config.getUsername();
            String password = config.getPassword();

            // 构建连接字符串
            String connectionString;
            if (username != null && !username.isEmpty()) {
                connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
                    username, password, host, port, databaseName);
            } else {
                connectionString = String.format("mongodb://%s:%d/%s", host, port, databaseName);
            }

            // 创建客户端
            MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder ->
                    builder.connectTimeout(5, TimeUnit.SECONDS))
                .applyToClusterSettings(builder ->
                    builder.serverSelectionTimeout(5, TimeUnit.SECONDS))
                .build();

            mongoClient = MongoClients.create(settings);
            mongoDatabase = mongoClient.getDatabase(databaseName);

            // 测试连接
            mongoDatabase.runCommand(new Document("ping", 1));

            connected = true;
            logger.info("已连接到 MongoDB: {}:{}", host, port);
        } catch (Exception e) {
            logger.error("连接 MongoDB 失败: {}", e.getMessage());
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
        return "MongoDB";
    }

    @Override
    public boolean isConnected() {
        return connected && mongoClient != null;
    }

    @Override
    public String getCurrentDatabase() {
        return currentDatabase;
    }

    @Override
    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();
        for (String dbName : mongoClient.listDatabaseNames()) {
            databases.add(dbName);
        }
        return databases;
    }

    @Override
    public void useDatabase(String database) {
        this.mongoDatabase = mongoClient.getDatabase(database);
        this.currentDatabase = database;
    }

    @Override
    public List<String> getTables() {
        // MongoDB 使用集合（collections）而不是表（tables）
        List<String> collections = new ArrayList<>();
        for (String collectionName : mongoDatabase.listCollectionNames()) {
            collections.add(collectionName);
        }
        return collections;
    }

    @Override
    public List<String> getViews() {
        // MongoDB 没有视图
        return new ArrayList<>();
    }

    @Override
    public Metadata getTableMetadata(String table) {
        // 获取集合的元数据
        MongoCollection<Document> collection = mongoDatabase.getCollection(table);

        // 获取文档样本
        Document sample = collection.find().first();
        if (sample == null) {
            return null;
        }

        // 从样本中提取字段信息
        List<Metadata.Column> columns = new ArrayList<>();
        for (Map.Entry<String, Object> entry : sample.entrySet()) {
            String fieldName = entry.getKey();
            String fieldType = getMongoType(entry.getValue());
            boolean nullable = true; // MongoDB 字段默认可空
            String defaultValue = null;
            boolean isPrimaryKey = "_id".equals(fieldName);
            String comment = "";

            Metadata.Column column = new Metadata.Column(fieldName, fieldType, nullable, defaultValue, isPrimaryKey, comment);
            columns.add(column);
        }

        return new DefaultMetadata(table, columns, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * 获取 MongoDB 类型
     */
    private String getMongoType(Object value) {
        if (value == null) return "null";
        if (value instanceof String) return "string";
        if (value instanceof Integer) return "int";
        if (value instanceof Long) return "long";
        if (value instanceof Double) return "double";
        if (value instanceof Boolean) return "boolean";
        if (value instanceof Date) return "date";
        if (value instanceof Document) return "object";
        if (value instanceof List) return "array";
        if (value instanceof org.bson.types.ObjectId) return "objectId";
        return value.getClass().getSimpleName();
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            // 解析 MongoDB 命令
            String trimmed = sql.trim();
            String upper = trimmed.toUpperCase();

            if (upper.startsWith("FIND") || upper.startsWith("DB.")) {
                return executeFind(trimmed, startTime);
            } else if (upper.startsWith("COUNT")) {
                return executeCount(trimmed, startTime);
            } else if (upper.startsWith("AGGREGATE")) {
                return executeAggregate(trimmed, startTime);
            } else {
                // 尝试作为 JSON 命令执行
                return executeJsonCommand(trimmed, startTime);
            }
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new MongoDBQueryResult(e.getMessage(), executionTime);
        }
    }

    /**
     * 执行 find 命令
     */
    private QueryResult executeFind(String command, long startTime) {
        // 解析 db.collection.find() 格式
        String collectionName = extractCollectionName(command);
        if (collectionName == null) {
            return new MongoDBQueryResult("Invalid command format", 0);
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        List<Document> documents = collection.find().limit(100).into(new ArrayList<>());

        return buildResult(documents, startTime);
    }

    /**
     * 执行 count 命令
     */
    private QueryResult executeCount(String command, long startTime) {
        String collectionName = extractCollectionName(command);
        if (collectionName == null) {
            return new MongoDBQueryResult("Invalid command format", 0);
        }

        MongoCollection<Document> collection = mongoDatabase.getCollection(collectionName);
        long count = collection.countDocuments();

        List<String> columnNames = Arrays.asList("count");
        List<String> columnTypes = Arrays.asList("long");
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("count", count);
        rows.add(row);

        long executionTime = System.currentTimeMillis() - startTime;
        return new MongoDBQueryResult(columnNames, columnTypes, rows, executionTime);
    }

    /**
     * 执行 aggregate 命令
     */
    private QueryResult executeAggregate(String command, long startTime) {
        // 简化实现
        return new MongoDBQueryResult("Aggregate not implemented yet", System.currentTimeMillis() - startTime);
    }

    /**
     * 执行 JSON 命令
     */
    private QueryResult executeJsonCommand(String command, long startTime) {
        try {
            Document cmd = Document.parse(command);
            Document result = mongoDatabase.runCommand(cmd);

            List<Document> documents = new ArrayList<>();
            documents.add(result);

            return buildResult(documents, startTime);
        } catch (Exception e) {
            return new MongoDBQueryResult(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 提取集合名
     */
    private String extractCollectionName(String command) {
        // 解析 db.collection.xxx() 格式
        if (command.startsWith("db.")) {
            int dotIndex = command.indexOf('.', 3);
            if (dotIndex > 0) {
                return command.substring(3, dotIndex);
            }
        }
        return null;
    }

    /**
     * 构建查询结果
     */
    private QueryResult buildResult(List<Document> documents, long startTime) {
        if (documents.isEmpty()) {
            return new MongoDBQueryResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                System.currentTimeMillis() - startTime);
        }

        // 收集所有字段名
        Set<String> allFields = new LinkedHashSet<>();
        for (Document doc : documents) {
            allFields.addAll(doc.keySet());
        }

        List<String> columnNames = new ArrayList<>(allFields);
        List<String> columnTypes = new ArrayList<>();
        for (String field : columnNames) {
            Object value = documents.get(0).get(field);
            columnTypes.add(getMongoType(value));
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (Document doc : documents) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (String field : columnNames) {
                row.put(field, doc.get(field));
            }
            rows.add(row);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        return new MongoDBQueryResult(columnNames, columnTypes, rows, executionTime);
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        // MongoDB 更新命令
        long startTime = System.currentTimeMillis();
        QueryResult result = executeQuery(sql);
        return result.hasError() ? 0 : 1;
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return new MongoDBQueryExecutor(this);
    }

    @Override
    public Object getNativeConnection() {
        return mongoClient;
    }

    @Override
    public boolean isValid() {
        try {
            mongoDatabase.runCommand(new Document("ping", 1));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (mongoClient != null) {
            mongoClient.close();
            connected = false;
            logger.info("已关闭 MongoDB 连接: {}", name);
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
