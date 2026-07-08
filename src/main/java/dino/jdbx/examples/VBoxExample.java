package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VBoxExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 VBox，间距为 10 像素
        VBox vbox = new VBox(10);

        // 内边距（内容与边框的距离）
        vbox.setPadding(new Insets(20));

        // 对齐方式：居中
        vbox.setAlignment(Pos.CENTER);

        // 添加子节点
        Label titleLabel = new Label("用户注册");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("请输入用户名");
        nameField.setMaxWidth(200);

        TextField emailField = new TextField();
        emailField.setPromptText("请输入邮箱");
        emailField.setMaxWidth(200);

        Button submitBtn = new Button("提交");
        submitBtn.setMaxWidth(200);

        Button cancelBtn = new Button("取消");
        cancelBtn.setMaxWidth(200);

        // 按顺序添加到 VBox
        vbox.getChildren().addAll(
                titleLabel,
                nameField,
                emailField,
                submitBtn,
                cancelBtn
        );

        Scene scene = new Scene(vbox, 300, 350);
        stage.setTitle("VBox 示例");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
