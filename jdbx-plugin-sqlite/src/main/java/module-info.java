module dino.jdbx.plugin.sqlite {
    requires dino.jdbx.core;
    requires java.sql;
    requires org.slf4j;

    provides dino.jdbx.core.api.DatabasePlugin with dino.jdbx.plugin.sqlite.SQLitePlugin;
}