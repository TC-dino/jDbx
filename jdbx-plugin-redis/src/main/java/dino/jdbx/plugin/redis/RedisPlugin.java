package dino.jdbx.plugin.redis;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Redis 数据库插件
 */
public class RedisPlugin implements DatabasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(RedisPlugin.class);

    private PluginContext context;

    @Override
    public String getId() {
        return "redis";
    }

    @Override
    public String getName() {
        return "Redis";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Redis 数据库插件";
    }

    @Override
    public String getAuthor() {
        return "jDbx Team";
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        logger.info("Redis 插件已初始化");
    }

    @Override
    public void unload() {
        logger.info("Redis 插件已卸载");
    }

    @Override
    public String getDatabaseType() {
        return "Redis";
    }

    @Override
    public int getDefaultPort() {
        return 6379;
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new RedisConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return Arrays.asList(
            new ConnectionParam("host", "主机", "localhost", true),
            new ConnectionParam("port", "端口", "6379", true),
            new ConnectionParam("password", "密码", "", false),
            new ConnectionParam("database", "数据库(0-15)", "0", true)
        );
    }

    @Override
    public List<String> getKeywords() {
        return Arrays.asList(
            // String commands
            "SET", "GET", "MSET", "MGET", "INCR", "DECR", "INCRBY", "DECRBY",
            "APPEND", "STRLEN", "GETRANGE", "SETRANGE",
            // Key commands
            "DEL", "EXISTS", "EXPIRE", "TTL", "PTTL", "PERSIST", "RENAME",
            "KEYS", "SCAN", "TYPE", "RANDOMKEY",
            // List commands
            "LPUSH", "RPUSH", "LPOP", "RPOP", "LRANGE", "LLEN", "LINDEX",
            "LSET", "LREM", "LTRIM",
            // Set commands
            "SADD", "SREM", "SMEMBERS", "SISMEMBER", "SCARD", "SINTER",
            "SUNION", "SDIFF",
            // Hash commands
            "HSET", "HGET", "HMSET", "HMGET", "HGETALL", "HDEL", "HEXISTS",
            "HKEYS", "HVALS", "HLEN", "HINCRBY",
            // Sorted Set commands
            "ZADD", "ZREM", "ZRANGE", "ZREVRANGE", "ZRANGEBYSCORE",
            "ZCARD", "ZSCORE", "ZINCRBY",
            // Transaction commands
            "MULTI", "EXEC", "DISCARD", "WATCH", "UNWATCH",
            // Server commands
            "PING", "ECHO", "INFO", "DBSIZE", "FLUSHDB", "FLUSHALL",
            "SELECT", "AUTH", "CONFIG", "SLOWLOG", "CLIENT",
            // Pub/Sub commands
            "PUBLISH", "SUBSCRIBE", "UNSUBSCRIBE", "PSUBSCRIBE", "PUNSUBSCRIBE"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        // Redis 没有传统意义上的函数
        return Arrays.asList();
    }
}
