package dino.jdbx.plugin.elasticsearch;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Elasticsearch 连接实现
 */
public class ElasticsearchConnection implements dino.jdbx.core.api.Connection {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchConnection.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String id;
    private String name;
    private ConnectionConfig config;
    private String baseUrl;
    private boolean connected;

    public ElasticsearchConnection(ConnectionConfig config) throws Exception {
        this.id = config.getId();
        this.name = config.getName();
        this.config = config;
        this.baseUrl = String.format("http://%s:%d", config.getHost(), config.getPort());
        connect();
    }

    private void connect() throws Exception {
        try {
            // 测试连接
            String response = executeHttpGet("/");
            JsonNode root = objectMapper.readTree(response);

            if (root.has("cluster_name")) {
                connected = true;
                logger.info("已连接到 Elasticsearch: {}", root.get("cluster_name").asText());
            } else {
                throw new Exception("无法解析 Elasticsearch 响应");
            }
        } catch (Exception e) {
            logger.error("连接 Elasticsearch 失败: {}", e.getMessage());
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
        return "Elasticsearch";
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public String getCurrentDatabase() {
        return "default";
    }

    @Override
    public List<String> getDatabases() {
        // Elasticsearch 没有数据库概念，返回索引列表
        return getIndices();
    }

    @Override
    public void useDatabase(String database) {
        // Elasticsearch 不需要切换数据库
    }

    @Override
    public List<String> getTables() {
        // 返回索引列表
        return getIndices();
    }

    @Override
    public List<String> getViews() {
        // Elasticsearch 没有视图
        return new ArrayList<>();
    }

    @Override
    public Metadata getTableMetadata(String table) {
        // 获取索引映射
        try {
            String response = executeHttpGet("/" + table + "/_mapping");
            JsonNode root = objectMapper.readTree(response);

            if (root.has(table)) {
                JsonNode mapping = root.get(table).get("mappings").get("properties");
                List<Metadata.Column> columns = new ArrayList<>();

                // 添加 _id 和 _source 字段
                columns.add(new Metadata.Column("_id", "keyword", true, null, true, "文档ID"));
                columns.add(new Metadata.Column("_source", "object", true, null, false, "文档内容"));

                if (mapping != null) {
                    Iterator<Map.Entry<String, JsonNode>> fields = mapping.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String fieldName = entry.getKey();
                        String fieldType = entry.getValue().has("type") ?
                            entry.getValue().get("type").asText() : "object";

                        columns.add(new Metadata.Column(fieldName, fieldType, true, null, false, ""));
                    }
                }

                return new DefaultMetadata(table, columns, new ArrayList<>(), new ArrayList<>());
            }
        } catch (Exception e) {
            logger.error("获取索引映射失败: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        long startTime = System.currentTimeMillis();

        try {
            String trimmed = sql.trim();

            // 解析 Elasticsearch 命令
            if (trimmed.startsWith("GET") || trimmed.startsWith("POST") || trimmed.startsWith("PUT")) {
                return executeRestCommand(trimmed, startTime);
            } else if (trimmed.startsWith("{")) {
                // JSON 查询
                return executeJsonQuery(trimmed, startTime);
            } else {
                // 尝试作为 _search 查询
                return executeSearch(trimmed, startTime);
            }
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ElasticsearchQueryResult(e.getMessage(), executionTime);
        }
    }

    /**
     * 执行 REST 命令
     */
    private QueryResult executeRestCommand(String command, long startTime) throws Exception {
        String[] parts = command.split("\\s+", 3);
        if (parts.length < 2) {
            return new ElasticsearchQueryResult("Invalid command", 0);
        }

        String method = parts[0];
        String path = parts[1];
        String body = parts.length > 2 ? parts[2] : null;

        String response;
        switch (method.toUpperCase()) {
            case "GET":
                response = executeHttpGet(path);
                break;
            case "POST":
                response = executeHttpPost(path, body);
                break;
            case "PUT":
                response = executeHttpPut(path, body);
                break;
            case "DELETE":
                response = executeHttpDelete(path);
                break;
            default:
                return new ElasticsearchQueryResult("Unsupported method: " + method, 0);
        }

        return parseResponse(response, startTime);
    }

    /**
     * 执行 JSON 查询
     */
    private QueryResult executeJsonQuery(String json, long startTime) throws Exception {
        // 默认对所有索引执行查询
        String response = executeHttpPost("/_search", json);
        return parseResponse(response, startTime);
    }

    /**
     * 执行搜索
     */
    private QueryResult executeSearch(String query, long startTime) throws Exception {
        // 构建简单的 match 查询
        String json = String.format("{\"query\":{\"match\":{\"_all\":\"%s\"}}}", query);
        String response = executeHttpPost("/_search", json);
        return parseResponse(response, startTime);
    }

    /**
     * 解析响应
     */
    private QueryResult parseResponse(String response, long startTime) {
        try {
            JsonNode root = objectMapper.readTree(response);
            long executionTime = System.currentTimeMillis() - startTime;

            // 检查是否是错误
            if (root.has("error")) {
                String error = root.get("error").has("reason") ?
                    root.get("error").get("reason").asText() : root.get("error").toString();
                return new ElasticsearchQueryResult(error, executionTime);
            }

            // 检查是否是搜索结果
            if (root.has("hits")) {
                return parseSearchResult(root, executionTime);
            }

            // 其他结果
            List<String> columnNames = Arrays.asList("result");
            List<String> columnTypes = Arrays.asList("json");
            List<Map<String, Object>> rows = new ArrayList<>();
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", root.toString());
            rows.add(row);

            return new ElasticsearchQueryResult(columnNames, columnTypes, rows, executionTime);
        } catch (Exception e) {
            return new ElasticsearchQueryResult(e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    /**
     * 解析搜索结果
     */
    private QueryResult parseSearchResult(JsonNode root, long executionTime) {
        JsonNode hits = root.get("hits").get("hits");
        long total = root.get("hits").get("total").has("value") ?
            root.get("hits").get("total").get("value").asLong() : 0;

        // 收集所有字段
        Set<String> allFields = new LinkedHashSet<>();
        allFields.add("_index");
        allFields.add("_id");
        allFields.add("_score");

        for (JsonNode hit : hits) {
            if (hit.has("_source")) {
                Iterator<String> fieldNames = hit.get("_source").fieldNames();
                while (fieldNames.hasNext()) {
                    allFields.add(fieldNames.next());
                }
            }
        }

        List<String> columnNames = new ArrayList<>(allFields);
        List<String> columnTypes = new ArrayList<>();
        for (String field : columnNames) {
            columnTypes.add("string");
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        for (JsonNode hit : hits) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("_index", hit.get("_index").asText());
            row.put("_id", hit.get("_id").asText());
            row.put("_score", hit.get("_score").asDouble());

            if (hit.has("_source")) {
                JsonNode source = hit.get("_source");
                for (String field : allFields) {
                    if (!field.equals("_index") && !field.equals("_id") && !field.equals("_score")) {
                        if (source.has(field)) {
                            row.put(field, source.get(field).toString());
                        } else {
                            row.put(field, null);
                        }
                    }
                }
            }

            rows.add(row);
        }

        return new ElasticsearchQueryResult(columnNames, columnTypes, rows, executionTime);
    }

    /**
     * 获取索引列表
     */
    private List<String> getIndices() {
        List<String> indices = new ArrayList<>();
        try {
            String response = executeHttpGet("/_cat/indices?format=json");
            JsonNode root = objectMapper.readTree(response);

            for (JsonNode index : root) {
                if (index.has("index")) {
                    indices.add(index.get("index").asText());
                }
            }
        } catch (Exception e) {
            logger.error("获取索引列表失败: {}", e.getMessage());
        }
        return indices;
    }

    /**
     * 执行 HTTP GET 请求
     */
    private String executeHttpGet(String path) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }

        return readResponse(conn);
    }

    /**
     * 执行 HTTP POST 请求
     */
    private String executeHttpPost(String path, String body) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }

        if (body != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        return readResponse(conn);
    }

    /**
     * 执行 HTTP PUT 请求
     */
    private String executeHttpPut(String path, String body) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PUT");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }

        if (body != null) {
            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }
        }

        return readResponse(conn);
    }

    /**
     * 执行 HTTP DELETE 请求
     */
    private String executeHttpDelete(String path) throws Exception {
        URL url = new URL(baseUrl + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Content-Type", "application/json");

        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            String auth = config.getUsername() + ":" + config.getPassword();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
            conn.setRequestProperty("Authorization", "Basic " + encodedAuth);
        }

        return readResponse(conn);
    }

    /**
     * 读取响应
     */
    private String readResponse(HttpURLConnection conn) throws Exception {
        int responseCode = conn.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) ?
            conn.getInputStream() : conn.getErrorStream();

        if (inputStream == null) {
            return "{\"error\":\"No response\"}";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        QueryResult result = executeQuery(sql);
        return result.hasError() ? 0 : 1;
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return new ElasticsearchQueryExecutor(this);
    }

    @Override
    public Object getNativeConnection() {
        return baseUrl;
    }

    @Override
    public boolean isValid() {
        try {
            executeHttpGet("/_cluster/health");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        connected = false;
        logger.info("已关闭 Elasticsearch 连接: {}", name);
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
