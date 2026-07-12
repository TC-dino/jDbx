module dino.jdbx.app {
    requires dino.jdbx.core;
    requires dino.jdbx.plugin.sqlite;
    requires dino.jdbx.plugin.mysql;
    requires dino.jdbx.plugin.postgresql;
    requires dino.jdbx.plugin.redis;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires jdk.jsobject;
    requires com.google.gson;

    opens dino.jdbx.app to javafx.fxml;
    opens dino.jdbx.app.web to javafx.web, javafx.fxml;
    exports dino.jdbx.app;
    exports dino.jdbx.app.web;
}