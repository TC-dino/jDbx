module dino.jdbx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires com.dustinredmond.fxtrayicon;

    opens dino.jdbx to javafx.fxml;
    exports dino.jdbx;
    exports dino.jdbx.examples;
    exports dino.jdbx.examples.ui;
}
