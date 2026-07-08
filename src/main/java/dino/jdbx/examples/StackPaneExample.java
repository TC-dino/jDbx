package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class StackPaneExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 StackPane
        StackPane stack = new StackPane();
        stack.setAlignment(Pos.CENTER);

        // 底层 - 背景
        Rectangle bg = new Rectangle(300, 200);
        bg.setFill(Color.LIGHTBLUE);
        bg.setArcWidth(20);
        bg.setArcHeight(20);

        // 中间层 - 半透明遮罩
        Rectangle overlay = new Rectangle(300, 200);
        overlay.setFill(Color.rgb(0, 0, 0, 0.3));  // 30%黑色透明
        overlay.setArcWidth(20);
        overlay.setArcHeight(20);

        // 顶层 - 文字
        Label label = new Label("加载中...");
        label.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        label.setTextFill(Color.WHITE);

        // 按顺序添加，后添加的在上层
        stack.getChildren().addAll(bg, overlay, label);

        Scene scene = new Scene(stack, 320, 240);
        stage.setTitle("StackPane 示例 - 层叠效果");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
