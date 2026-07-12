module dino.jdbx.plugin.mysql {
    requires dino.jdbx.core;
    requires java.sql;
    requires org.slf4j;

    provides dino.jdbx.core.api.DatabasePlugin with dino.jdbx.plugin.mysql.MySQLPlugin;
}
