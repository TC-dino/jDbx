package dino.jdbx.examples;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;

/**
 * 使用 WebView 加载 Bootstrap 3 管理仪表板 HTML 原型
 */
public class BootstrapDemo extends Application {

    private WebView webView;
    private WebEngine webEngine;

    @Override
    public void start(Stage stage) {
        // 创建 WebView
        webView = new WebView();
        webEngine = webView.getEngine();

        // 加载 HTML 文件
        URL htmlUrl = getClass().getResource("/dino/index.html");
        if (htmlUrl != null) {
            webEngine.load(htmlUrl.toExternalForm());
        } else {
            System.err.println("未找到 HTML 文件: /dino/index.html");
        }

        // 创建工具栏
        ToolBar toolBar = createToolBar();

        // 主布局
        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(webView);

        // 设置样式
        root.setStyle("-fx-background-color: #f5f7fa;");

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Bootstrap 3 管理仪表板 - JavaFX WebView 演示");
        stage.setScene(scene);
        stage.show();
    }

    private ToolBar createToolBar() {
        Button backBtn = new Button("← 后退");
        backBtn.setOnAction(e -> webEngine.getHistory().go(-1));

        Button forwardBtn = new Button("前进 →");
        forwardBtn.setOnAction(e -> webEngine.getHistory().go(1));

        Button refreshBtn = new Button("刷新");
        refreshBtn.setOnAction(e -> webEngine.reload());

        Button homeBtn = new Button("首页");
        homeBtn.setOnAction(e -> {
            URL htmlUrl = getClass().getResource("/dino/index.html");
            if (htmlUrl != null) {
                webEngine.load(htmlUrl.toExternalForm());
            }
        });

        Separator separator = new Separator();

        Label statusLabel = new Label("就绪");
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                Platform.runLater(() -> statusLabel.setText("加载完成"));
            } else if (newState == Worker.State.RUNNING) {
                Platform.runLater(() -> statusLabel.setText("加载中..."));
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolBar = new ToolBar(backBtn, forwardBtn, refreshBtn, homeBtn, separator, statusLabel, spacer);
        toolBar.setPadding(new Insets(5));
        toolBar.setStyle("-fx-background-color: #2c3e50; -fx-padding: 5 10;");

        // 设置按钮样式
        String buttonStyle = "-fx-background-color: #34495e; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 4 10;";
        backBtn.setStyle(buttonStyle);
        forwardBtn.setStyle(buttonStyle);
        refreshBtn.setStyle(buttonStyle);
        homeBtn.setStyle(buttonStyle);

        statusLabel.setStyle("-fx-text-fill: #b8c7ce;");

        return toolBar;
    }

    // 所有辅助方法已删除，仅保留 WebView 加载逻辑

    public static void main(String[] args) {
        launch(args);
    }
}
