package dino.jdbx.plugin.sqlite;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.ConnectionConfig;
import dino.jdbx.core.api.ConnectionFactory;

/**
 * SQLite 连接工厂
 */
public class SQLiteConnectionFactory implements ConnectionFactory {

    @Override
    public Connection createConnection(ConnectionConfig config) throws Exception {
        return new SQLiteConnection(config);
    }
}