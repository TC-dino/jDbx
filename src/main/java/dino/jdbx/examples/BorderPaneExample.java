package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BorderPaneExample extends Application {

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        // 顶部 - 菜单栏
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("文件");
        Menu editMenu = new Menu("编辑");
        Menu helpMenu = new Menu("帮助");
        fileMenu.getItems().addAll(new MenuItem("新建"), new MenuItem("打开"), new MenuItem("保存"));
        editMenu.getItems().addAll(new MenuItem("撤销"), new MenuItem("重做"));
        helpMenu.getItems().add(new MenuItem("关于"));
        menuBar.getMenus().addAll(fileMenu, editMenu, helpMenu);
        root.setTop(menuBar);

        // 左侧 - 导航树
        TreeView<String> tree = new TreeView<>();
        TreeItem<String> rootItem = new TreeItem<>("项目");
        rootItem.getChildren().addAll(
                new TreeItem<>("src"),
                new TreeItem<>("resources"),
                new TreeItem<>("pom.xml")
        );
        tree.setRoot(rootItem);
        rootItem.setExpanded(true);
        tree.setPrefWidth(150);
        root.setLeft(tree);

        // 中间 - 编辑区
        TextArea editor = new TextArea();
        editor.setPromptText("在这里编辑代码...");
        root.setCenter(editor);

        // 右侧 - 属性面板
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.getChildren().addAll(
                new Label("属性面板"),
                new Label("文件名: demo.java"),
                new Label("大小: 1.2 KB"),
                new Label("修改时间: 2026-07-08")
        );
        root.setRight(rightPanel);

        // 底部 - 状态栏
        HBox statusBar = new HBox(20);
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.getChildren().addAll(
                new Label("行: 1"),
                new Label("列: 1"),
                new Label("UTF-8"),
                new Label("Java")
        );
        root.setBottom(statusBar);

        Scene scene = new Scene(root, 700, 500);
        stage.setTitle("BorderPane 示例 - 编辑器");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
