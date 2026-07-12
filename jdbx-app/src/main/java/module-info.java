module dino.jdbx.app {
    requires dino.jdbx.core;
    requires dino.jdbx.plugin.sqlite;
    requires javafx.controls;
    requires javafx.fxml;

    opens dino.jdbx.app to javafx.fxml;
    exports dino.jdbx.app;
}