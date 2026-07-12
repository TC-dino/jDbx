package dino.jdbx.plugin.redis;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.params.ScanParams;

import java.util.*;

/**
 * Redis 连接实现
 */
public class RedisConnection implements dino.jdbx.core.api.Connection {

    private static final Logger logger = LoggerFactory.getLogger(RedisConnection.class);

    private String id;
    private String name;
    private ConnectionConfig config;
    private JedisPool jedisPool;
    private boolean connected;
    private int currentDatabase;

    public RedisConnection(ConnectionConfig config) throws Exception {
        this.id = config.getId();
        this.name = config.getName();
        this.config = config;
        this.currentDatabase = config.getPort() > 0 ? Integer.parseInt(config.getDatabase()) : 0;
        connect();
    }

    private void connect() throws Exception {
        try {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(10);
            poolConfig.setMaxIdle(5);
            poolConfig.setMinIdle(1);

            String host = config.getHost();
            int port = config.getPort();
            String password = config.getPassword();

            if (password != null && !password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, 2000, password, currentDatabase);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, 2000, null, currentDatabase);
            }

            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
            }

            connected = true;
            logger.info("已连接到 Redis: {}:{}", host, port);
        } catch (Exception e) {
            logger.error("连接 Redis 失败: {}", e.getMessage());
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
        return "Redis";
    }

    @Override
    public boolean isConnected() {
        return connected && jedisPool != null && !jedisPool.isClosed();
    }

    @Override
    public String getCurrentDatabase() {
        return String.valueOf(currentDatabase);
    }

    @Override
    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();
        for (int i = 0; i < 16; i++) {
            databases.add(String.valueOf(i));
        }
        return databases;
    }

    @Override
    public void useDatabase(String database) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.select(Integer.parseInt(database));
            this.currentDatabase = Integer.parseInt(database);
        }
    }

    @Override
    public List<String> getTables() {
        // Redis 没有表的概念，返回键的模式
        List<String> patterns = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()) {
            // 获取所有键（使用 KEYS 命令，生产环境应使用 SCAN）
            Set<String> allKeys = jedis.keys("*");

            // 按前缀分组
            Map<String, Integer> prefixCount = new HashMap<>();
            for (String key : allKeys) {
                String prefix = key.contains(":") ? key.substring(0, key.indexOf(":")) : key;
                prefixCount.merge(prefix, 1, Integer::sum);
            }

            for (Map.Entry<String, Integer> entry : prefixCount.entrySet()) {
                patterns.add(entry.getKey() + " (" + entry.getValue() + " keys)");
            }
        }
        return patterns;
    }

    @Override
    public List<String> getViews() {
        // Redis 没有视图
        return new ArrayList<>();
    }

    @Override
    public Metadata getTableMetadata(String table) {
        // Redis 没有表元数据
        return null;
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        long startTime = System.currentTimeMillis();

        try (Jedis jedis = jedisPool.getResource()) {
            // 解析 Redis 命令
            String[] parts = sql.trim().split("\\s+");
            if (parts.length == 0) {
                return new RedisQueryResult("Empty command", 0);
            }

            String command = parts[0].toUpperCase();
            List<String> args = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));

            // 执行命令
            Object result = executeRedisCommand(jedis, command, args);
            long executionTime = System.currentTimeMillis() - startTime;

            // 构建结果
            return buildResult(command, result, executionTime);
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new RedisQueryResult(e.getMessage(), executionTime);
        }
    }

    /**
     * 执行 Redis 命令
     */
    private Object executeRedisCommand(Jedis jedis, String command, List<String> args) {
        switch (command) {
            case "PING":
                return jedis.ping();
            case "GET":
                return args.size() > 0 ? jedis.get(args.get(0)) : null;
            case "SET":
                if (args.size() >= 2) {
                    return jedis.set(args.get(0), args.get(1));
                }
                return "ERR wrong number of arguments";
            case "DEL":
                if (!args.isEmpty()) {
                    return jedis.del(args.toArray(new String[0]));
                }
                return 0;
            case "EXISTS":
                if (!args.isEmpty()) {
                    return jedis.exists(args.toArray(new String[0]));
                }
                return 0;
            case "KEYS":
                if (!args.isEmpty()) {
                    return jedis.keys(args.get(0));
                }
                return jedis.keys("*");
            case "TYPE":
                if (!args.isEmpty()) {
                    return jedis.type(args.get(0));
                }
                return "none";
            case "TTL":
                if (!args.isEmpty()) {
                    return jedis.ttl(args.get(0));
                }
                return -1;
            case "EXPIRE":
                if (args.size() >= 2) {
                    return jedis.expire(args.get(0), Long.parseLong(args.get(1)));
                }
                return 0;
            case "DBSIZE":
                return jedis.dbSize();
            case "INFO":
                return jedis.info();
            case "SELECT":
                if (!args.isEmpty()) {
                    jedis.select(Integer.parseInt(args.get(0)));
                    this.currentDatabase = Integer.parseInt(args.get(0));
                    return "OK";
                }
                return "ERR wrong number of arguments";
            case "FLUSHDB":
                jedis.flushDB();
                return "OK";
            case "LPUSH":
                if (args.size() >= 2) {
                    return jedis.lpush(args.get(0), args.subList(1, args.size()).toArray(new String[0]));
                }
                return 0;
            case "RPUSH":
                if (args.size() >= 2) {
                    return jedis.rpush(args.get(0), args.subList(1, args.size()).toArray(new String[0]));
                }
                return 0;
            case "LRANGE":
                if (args.size() >= 3) {
                    return jedis.lrange(args.get(0), Long.parseLong(args.get(1)), Long.parseLong(args.get(2)));
                }
                return new ArrayList<>();
            case "LLEN":
                if (!args.isEmpty()) {
                    return jedis.llen(args.get(0));
                }
                return 0;
            case "SADD":
                if (args.size() >= 2) {
                    return jedis.sadd(args.get(0), args.subList(1, args.size()).toArray(new String[0]));
                }
                return 0;
            case "SMEMBERS":
                if (!args.isEmpty()) {
                    return jedis.smembers(args.get(0));
                }
                return new HashSet<>();
            case "HSET":
                if (args.size() >= 3) {
                    return jedis.hset(args.get(0), args.get(1), args.get(2));
                }
                return 0;
            case "HGET":
                if (args.size() >= 2) {
                    return jedis.hget(args.get(0), args.get(1));
                }
                return null;
            case "HGETALL":
                if (!args.isEmpty()) {
                    return jedis.hgetAll(args.get(0));
                }
                return new HashMap<>();
            case "ZADD":
                if (args.size() >= 3) {
                    return jedis.zadd(args.get(0), Double.parseDouble(args.get(1)), args.get(2));
                }
                return 0;
            case "ZRANGE":
                if (args.size() >= 3) {
                    return jedis.zrange(args.get(0), Long.parseLong(args.get(1)), Long.parseLong(args.get(2)));
                }
                return new LinkedHashSet<>();
            default:
                return "ERR unknown command '" + command + "'";
        }
    }

    /**
     * 构建查询结果
     */
    private QueryResult buildResult(String command, Object result, long executionTime) {
        List<String> columnNames = new ArrayList<>();
        List<String> columnTypes = new ArrayList<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        if (result == null) {
            columnNames.add("result");
            columnTypes.add("string");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", "(nil)");
            rows.add(row);
        } else if (result instanceof String) {
            columnNames.add("result");
            columnTypes.add("string");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", result);
            rows.add(row);
        } else if (result instanceof Long || result instanceof Integer) {
            columnNames.add("result");
            columnTypes.add("integer");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", result);
            rows.add(row);
        } else if (result instanceof Collection) {
            columnNames.add("value");
            columnTypes.add("string");
            for (Object item : (Collection<?>) result) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("value", item);
                rows.add(row);
            }
        } else if (result instanceof Map) {
            columnNames.add("field");
            columnNames.add("value");
            columnTypes.add("string");
            columnTypes.add("string");
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) result).entrySet()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("field", entry.getKey());
                row.put("value", entry.getValue());
                rows.add(row);
            }
        } else {
            columnNames.add("result");
            columnTypes.add("string");
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("result", result.toString());
            rows.add(row);
        }

        return new RedisQueryResult(columnNames, columnTypes, rows, executionTime);
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        // Redis 命令都通过 executeQuery 执行
        QueryResult result = executeQuery(sql);
        return result.hasError() ? 0 : 1;
    }

    @Override
    public QueryExecutor getQueryExecutor() {
        return new RedisQueryExecutor(this);
    }

    @Override
    public Object getNativeConnection() {
        return jedisPool;
    }

    @Override
    public boolean isValid() {
        try (Jedis jedis = jedisPool.getResource()) {
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void close() throws Exception {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            connected = false;
            logger.info("已关闭 Redis 连接: {}", name);
        }
    }
}
