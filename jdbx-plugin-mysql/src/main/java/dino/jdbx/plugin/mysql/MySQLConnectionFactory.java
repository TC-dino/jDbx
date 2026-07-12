package dino.jdbx.plugin.mysql;

import dino.jdbx.core.api.*;

/**
 * MySQL 连接工厂
 */
public class MySQLConnectionFactory implements ConnectionFactory {

    @Override
    public dino.jdbx.core.api.Connection createConnection(ConnectionConfig config) throws Exception {
        return new MySQLConnection(config);
    }
}
