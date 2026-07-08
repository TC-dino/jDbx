package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class TilePaneExample extends Application {

    @Override
    public void start(Stage stage) {
        // 创建 TilePane
        TilePane tile = new TilePane(10, 10);
        tile.setPadding(new Insets(20));
        tile.setAlignment(Pos.CENTER);

        // 添加彩色方块，模拟图标网格
        Color[] colors = {Color.RED, Color.ORANGE, Color.YELLOW,
                Color.GREEN, Color.CYAN, Color.BLUE,
                Color.PURPLE, Color.PINK, Color.GRAY};

        for (int i = 0; i < colors.length; i++) {
            Rectangle rect = new Rectangle(60, 60);
            rect.setFill(colors[i]);
            rect.setArcWidth(10);
            rect.setArcHeight(10);

            // 用 StackPane 给方块加文字
            javafx.scene.layout.StackPane cell = new javafx.scene.layout.StackPane(rect,
                    new Label(String.valueOf(i + 1)));
            tile.getChildren().add(cell);
        }

        Scene scene = new Scene(tile, 350, 350);
        stage.setTitle("TilePane 示例 - 图标网格");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
