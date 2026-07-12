package dino.jdbx.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * jDbx 应用入口
 */
public class JdbxApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dino/jdbx/app/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(getClass().getResource("/dino/jdbx/app/styles/dark.css").toExternalForm());

        primaryStage.setTitle("jDbx");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.show();

        MainController controller = loader.getController();
        if (controller != null) {
            controller.onStageReady(primaryStage);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
