package dino.jdbx.plugin.redis;

import dino.jdbx.core.api.*;

/**
 * Redis 连接工厂
 */
public class RedisConnectionFactory implements ConnectionFactory {

    @Override
    public dino.jdbx.core.api.Connection createConnection(ConnectionConfig config) throws Exception {
        return new RedisConnection(config);
    }
}
