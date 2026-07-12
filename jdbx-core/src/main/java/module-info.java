module dino.jdbx.core {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.zaxxer.hikari;
    requires com.google.gson;
    requires org.slf4j;
    requires ch.qos.logback.classic;

    exports dino.jdbx.core.api;
    exports dino.jdbx.core.connection;
    exports dino.jdbx.core.plugin;
    exports dino.jdbx.core.config;
    exports dino.jdbx.core.theme;
    exports dino.jdbx.core.history;
    exports dino.jdbx.core.security;

    opens dino.jdbx.core.api to com.google.gson;
    opens dino.jdbx.core.connection to com.google.gson;
    opens dino.jdbx.core.history to com.google.gson;
    opens dino.jdbx.core.config to com.google.gson;

    uses dino.jdbx.core.api.DatabasePlugin;
}