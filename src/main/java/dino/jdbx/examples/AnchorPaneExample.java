package dino.jdbx.examples;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class AnchorPaneExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 AnchorPane
        AnchorPane root = new AnchorPane();

        // 顶部按钮 - 固定在左上角
        Button topLeftBtn = new Button("左上角");
        AnchorPane.setTopAnchor(topLeftBtn, 10.0);
        AnchorPane.setLeftAnchor(topLeftBtn, 10.0);

        // 顶部按钮 - 固定在右上角
        Button topRightBtn = new Button("右上角");
        AnchorPane.setTopAnchor(topRightBtn, 10.0);
        AnchorPane.setRightAnchor(topRightBtn, 10.0);

        // 中间文本框 - 四边锚定，自动填充
        TextArea centerArea = new TextArea();
        centerArea.setPromptText("我会随着窗口大小变化...");
        AnchorPane.setTopAnchor(centerArea, 50.0);
        AnchorPane.setBottomAnchor(centerArea, 50.0);
        AnchorPane.setLeftAnchor(centerArea, 10.0);
        AnchorPane.setRightAnchor(centerArea, 10.0);

        // 底部按钮 - 固定在左下角
        Button bottomLeftBtn = new Button("左下角");
        AnchorPane.setBottomAnchor(bottomLeftBtn, 10.0);
        AnchorPane.setLeftAnchor(bottomLeftBtn, 10.0);

        // 底部按钮 - 固定在右下角
        Button bottomRightBtn = new Button("右下角");
        AnchorPane.setBottomAnchor(bottomRightBtn, 10.0);
        AnchorPane.setRightAnchor(bottomRightBtn, 10.0);

        // 添加所有节点
        root.getChildren().addAll(topLeftBtn, topRightBtn, centerArea, bottomLeftBtn, bottomRightBtn);

        Scene scene = new Scene(root, 400, 350);
        stage.setTitle("AnchorPane 示例 - 锚点定位");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
