package dino.jdbx.plugin.elasticsearch;

import dino.jdbx.core.api.*;

/**
 * Elasticsearch 连接工厂
 */
public class ElasticsearchConnectionFactory implements ConnectionFactory {

    @Override
    public dino.jdbx.core.api.Connection createConnection(ConnectionConfig config) throws Exception {
        return new ElasticsearchConnection(config);
    }
}
