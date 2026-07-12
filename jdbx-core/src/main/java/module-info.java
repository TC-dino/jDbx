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
}