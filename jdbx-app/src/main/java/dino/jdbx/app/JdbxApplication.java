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
        // 加载主窗口FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/dino/jdbx/app/main.fxml"));
        Parent root = loader.load();

        // 创建场景
        Scene scene = new Scene(root, 1200, 800);

        // 设置样式
        scene.getStylesheets().add(getClass().getResource("/dino/jdbx/app/styles/light.css").toExternalForm());

        // 配置窗口
        primaryStage.setTitle("jDbx - 中间件管理工具");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);

        // 显示窗口
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}