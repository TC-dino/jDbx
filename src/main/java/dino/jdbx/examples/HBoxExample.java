package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class HBoxExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 HBox，间距为 10 像素
        HBox hbox = new HBox(10);

        // 内边距
        hbox.setPadding(new Insets(20));

        // 对齐方式：垂直居中
        hbox.setAlignment(Pos.CENTER);

        // 添加子节点
        Label searchLabel = new Label("搜索：");

        TextField searchField = new TextField();
        searchField.setPromptText("输入关键词...");
        searchField.setPrefWidth(200);

        Button searchBtn = new Button("搜索");
        Button clearBtn = new Button("清空");

        // 按顺序添加到 HBox
        hbox.getChildren().addAll(
                searchLabel,
                searchField,
                searchBtn,
                clearBtn
        );

        Scene scene = new Scene(hbox, 450, 80);
        stage.setTitle("HBox 示例");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
