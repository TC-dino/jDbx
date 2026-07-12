package dino.jdbx.plugin.postgresql;

import dino.jdbx.core.api.*;

/**
 * PostgreSQL 连接工厂
 */
public class PostgreSQLConnectionFactory implements ConnectionFactory {

    @Override
    public dino.jdbx.core.api.Connection createConnection(ConnectionConfig config) throws Exception {
        return new PostgreSQLConnection(config);
    }
}
