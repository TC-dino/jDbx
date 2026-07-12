module dino.jdbx.plugin.redis {
    requires dino.jdbx.core;
    requires org.slf4j;
    requires redis.clients.jedis;

    provides dino.jdbx.core.api.DatabasePlugin with dino.jdbx.plugin.redis.RedisPlugin;
}
