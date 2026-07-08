package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class FlowPaneExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 FlowPane（水平方向，默认）
        FlowPane flow = new FlowPane(Orientation.HORIZONTAL, 10, 10);
        flow.setPadding(new Insets(20));

        // 添加标签
        Label titleLabel = new Label("标签云：");
        flow.getChildren().add(titleLabel);

        // 模拟标签云
        String[] tags = {"Java", "JavaFX", "Maven", "Git", "MySQL",
                "Spring", "Redis", "Docker", "Linux", "WebSocket",
                "HTTP", "JSON", "REST", "Microservice", "Kotlin"};

        for (String tag : tags) {
            Button tagBtn = new Button(tag);
            tagBtn.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 15;");
            flow.getChildren().add(tagBtn);
        }

        Scene scene = new Scene(flow, 400, 300);
        stage.setTitle("FlowPane 示例 - 标签云");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
