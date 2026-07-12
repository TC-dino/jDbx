package dino.jdbx.plugin.mongodb;

import dino.jdbx.core.api.*;

/**
 * MongoDB 连接工厂
 */
public class MongoDBConnectionFactory implements ConnectionFactory {

    @Override
    public dino.jdbx.core.api.Connection createConnection(ConnectionConfig config) throws Exception {
        return new MongoDBConnection(config);
    }
}
