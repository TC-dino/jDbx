module dino.jdbx {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;

    opens dino.jdbx to javafx.fxml;
    exports dino.jdbx;
}