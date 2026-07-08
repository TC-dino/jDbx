package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class GridPaneExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 GridPane
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);  // 水平间距
        grid.setVgap(10);  // 垂直间距
        grid.setPadding(new Insets(25));

        // 标题（跨2列）
        Label titleLabel = new Label("用户登录");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        grid.add(titleLabel, 0, 0, 2, 1);  // (节点, 列, 行, 列跨度, 行跨度)

        // 用户名
        Label userLabel = new Label("用户名：");
        grid.add(userLabel, 0, 1);

        TextField userField = new TextField();
        userField.setPromptText("请输入用户名");
        grid.add(userField, 1, 1);

        // 密码
        Label passLabel = new Label("密码：");
        grid.add(passLabel, 0, 2);

        PasswordField passField = new PasswordField();
        passField.setPromptText("请输入密码");
        grid.add(passField, 1, 2);

        // 按钮（跨2列居中）
        HBox btnBox = new HBox(10);
        btnBox.setAlignment(Pos.CENTER_RIGHT);
        btnBox.getChildren().addAll(
                new Button("注册"),
                new Button("登录")
        );
        grid.add(btnBox, 0, 3, 2, 1);

        Scene scene = new Scene(grid, 350, 250);
        stage.setTitle("GridPane 示例 - 登录表单");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
