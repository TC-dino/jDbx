package dino.jdbx.examples.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 无边框自定义窗口示例 - 圆角窗口 (Rounded)
 * <p>
 * 功能特性：
 * - 大圆角窗口设计（类似 macOS 风格）
 * - 阴影效果
 * - 无边框设计
 * - 自定义标题栏
 * - 可拖拽移动窗口
 * - 多种圆角样式选择
 *
 * @author jDbx
 * @version 1.0
 */
public class RoundedWindowExample extends Application {

    // 颜色常量
    private static final Color ACCENT = Color.rgb(0, 120, 212);
    private static final Color SURFACE = Color.rgb(255, 255, 255);
    private static final Color SURFACE_VARIANT = Color.rgb(245, 245, 245);

    private Stage primaryStage;
    private Scene scene;
    private double xOffset, yOffset;
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreW, restoreH;

    // 当前圆角半径
    private double currentCornerRadius = 20;
    private Rectangle backgroundRect;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 无边框 + 透明（圆角需要）
        stage.initStyle(StageStyle.TRANSPARENT);

        // 根容器
        StackPane root = new StackPane();
        root.setPickOnBounds(false);
        root.setPadding(new Insets(10)); // 为阴影留出空间

        // 圆角背景
        backgroundRect = new Rectangle();
        backgroundRect.widthProperty().bind(root.widthProperty().subtract(20));
        backgroundRect.heightProperty().bind(root.heightProperty().subtract(20));
        backgroundRect.setArcWidth(currentCornerRadius * 2);
        backgroundRect.setArcHeight(currentCornerRadius * 2);
        backgroundRect.setFill(SURFACE);

        // 阴影效果
        DropShadow shadow = new DropShadow();
        shadow.setRadius(30);
        shadow.setSpread(0);
        shadow.setColor(Color.rgb(0, 0, 0, 0.15));
        shadow.setOffsetY(5);
        backgroundRect.setEffect(shadow);

        // 主内容布局
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPickOnBounds(false);

        // 标题栏
        HBox titleBar = createTitleBar();
        mainLayout.setTop(titleBar);

        // 中央内容
        StackPane centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        root.getChildren().addAll(backgroundRect, mainLayout);

        // 场景
        this.scene = new Scene(root, 900, 600);
        scene.setFill(Color.TRANSPARENT);

        // 窗口拖拽
        setupWindowDrag(scene, stage);

        stage.setTitle("圆角窗口 - Rounded Window");
        stage.setScene(scene);
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.show();
    }

    /**
     * 创建自定义标题栏
     */
    private HBox createTitleBar() {
        HBox titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(0, 8, 0, 16));
        titleBar.setPrefHeight(44);
        titleBar.setMinHeight(44);
        titleBar.setMaxHeight(44);

        // macOS 风格的红绿灯按钮
        Button closeBtn = createTrafficLightButton(Color.rgb(255, 95, 86), "关闭");
        closeBtn.setOnAction(e -> primaryStage.close());

        Button minimizeBtn = createTrafficLightButton(Color.rgb(255, 189, 46), "最小化");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));

        Button maximizeBtn = createTrafficLightButton(Color.rgb(39, 201, 63), "最大化");
        maximizeBtn.setOnAction(e -> toggleMaximize());

        HBox trafficLights = new HBox(8, closeBtn, minimizeBtn, maximizeBtn);
        trafficLights.setAlignment(Pos.CENTER_LEFT);
        trafficLights.setPadding(new Insets(0, 16, 0, 0));

        // 应用标题
        Label title = new Label("圆角窗口示例");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 13));
        title.setTextFill(Color.rgb(102, 102, 102));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 圆角半径调节标签
        Label radiusLabel = new Label("圆角半径: " + (int) currentCornerRadius + "px");
        radiusLabel.setFont(Font.font("Microsoft YaHei", 12));
        radiusLabel.setTextFill(Color.rgb(153, 153, 153));

        titleBar.getChildren().addAll(trafficLights, title, spacer, radiusLabel);

        // 双击标题栏最大化/还原
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleMaximize();
            }
        });

        return titleBar;
    }

    /**
     * 创建 macOS 风格的红绿灯按钮
     */
    private Button createTrafficLightButton(Color color, String tooltip) {
        Button btn = new Button();
        btn.setPrefSize(14, 14);
        btn.setMinSize(14, 14);
        btn.setMaxSize(14, 14);
        btn.setTooltip(new javafx.scene.control.Tooltip(tooltip));

        // 圆形背景
        String style = String.format(
            "-fx-background-color: rgb(%d, %d, %d);" +
            "-fx-background-radius: 50%;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 0;",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
        btn.setStyle(style);

        // 悬停效果：显示图标
        btn.setOnMouseEntered(e -> {
            String hoverStyle = String.format(
                "-fx-background-color: rgb(%d, %d, %d);" +
                "-fx-background-radius: 50%;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0;" +
                "-fx-font-size: 8;" +
                "-fx-text-fill: rgba(0,0,0,0.6);",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
            );
            btn.setStyle(hoverStyle);
            if (tooltip.equals("关闭")) btn.setText("×");
            else if (tooltip.equals("最小化")) btn.setText("−");
            else btn.setText("+");
        });

        btn.setOnMouseExited(e -> {
            btn.setStyle(style);
            btn.setText("");
        });

        return btn;
    }

    /**
     * 创建中央内容区域
     */
    private StackPane createCenterContent() {
        StackPane center = new StackPane();

        VBox content = new VBox(24);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(20, 40, 30, 40));

        // 欢迎标题
        Label welcomeLabel = new Label("圆角窗口示例");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.rgb(32, 32, 32));

        // 说明文字
        Label descLabel = new Label(
            "圆角窗口是现代 UI 设计的标志性特征，\n" +
            "类似 macOS 窗口的圆润外观。\n\n" +
            "• 大圆角设计（20px+）\n" +
            "• 柔和阴影效果\n" +
            "• macOS 风格红绿灯按钮\n" +
            "• 多种圆角样式可选"
        );
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.rgb(80, 80, 80));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // 圆角样式选择卡片
        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createRadiusCard("小圆角", "8px", 8),
            createRadiusCard("中圆角", "16px", 16),
            createRadiusCard("大圆角", "24px", 24),
            createRadiusCard("超大圆角", "32px", 32)
        );

        content.getChildren().addAll(welcomeLabel, descLabel, cards);
        center.getChildren().add(content);

        return center;
    }

    /**
     * 创建圆角样式卡片
     */
    private VBox createRadiusCard(String name, String radiusText, double radius) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setPrefWidth(180);

        // 圆角预览矩形
        Rectangle preview = new Rectangle(80, 60);
        preview.setArcWidth(radius * 2);
        preview.setArcHeight(radius * 2);
        preview.setFill(new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(102, 126, 234)),
            new Stop(1, Color.rgb(118, 75, 162))
        ));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.rgb(32, 32, 32));

        Label radiusLabel = new Label(radiusText);
        radiusLabel.setFont(Font.font("Consolas", 12));
        radiusLabel.setTextFill(Color.rgb(153, 153, 153));

        Button applyBtn = new Button("应用");
        applyBtn.setStyle(
            "-fx-background-color: " + toHex(ACCENT) + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-padding: 6 16;" +
            "-fx-cursor: hand;"
        );
        applyBtn.setOnAction(e -> applyCornerRadius(radius));

        card.getChildren().addAll(preview, nameLabel, radiusLabel, applyBtn);

        // 卡片样式
        String cardStyle =
            "-fx-background-color: white;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(0,0,0,0.05);" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);";
        card.setStyle(cardStyle);

        card.setOnMouseEntered(e -> card.setStyle(
            cardStyle.replace("rgba(0,0,0,0.06)", "rgba(0,120,212,0.15)")
                     .replace("rgba(0,0,0,0.05)", "rgba(0,120,212,0.3)")
        ));
        card.setOnMouseExited(e -> card.setStyle(cardStyle));

        return card;
    }

    /**
     * 应用圆角半径
     */
    private void applyCornerRadius(double radius) {
        currentCornerRadius = radius;
        backgroundRect.setArcWidth(radius * 2);
        backgroundRect.setArcHeight(radius * 2);

        // 动画效果
        javafx.animation.KeyValue kv = new javafx.animation.KeyValue(
            backgroundRect.arcWidthProperty(), radius * 2, javafx.animation.Interpolator.EASE_BOTH
        );
        javafx.animation.KeyValue kv2 = new javafx.animation.KeyValue(
            backgroundRect.arcHeightProperty(), radius * 2, javafx.animation.Interpolator.EASE_BOTH
        );
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(300), kv, kv2
        );
        new javafx.animation.Timeline(kf).play();
    }

    /**
     * 切换最大化/还原
     */
    private void toggleMaximize() {
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        if (isMaximized) {
            primaryStage.setX(restoreX);
            primaryStage.setY(restoreY);
            primaryStage.setWidth(restoreW);
            primaryStage.setHeight(restoreH);
        } else {
            restoreX = primaryStage.getX();
            restoreY = primaryStage.getY();
            restoreW = primaryStage.getWidth();
            restoreH = primaryStage.getHeight();
            primaryStage.setX(bounds.getMinX());
            primaryStage.setY(bounds.getMinY());
            primaryStage.setWidth(bounds.getWidth());
            primaryStage.setHeight(bounds.getHeight());
        }
        isMaximized = !isMaximized;
    }

    /**
     * 设置窗口拖拽
     */
    private void setupWindowDrag(Scene scene, Stage stage) {
        scene.setOnMousePressed(e -> {
            if (isOnTitleBar(e)) {
                xOffset = e.getScreenX() - stage.getX();
                yOffset = e.getScreenY() - stage.getY();
            }
        });

        scene.setOnMouseDragged(e -> {
            if (isOnTitleBar(e) && !isMaximized) {
                stage.setX(e.getScreenX() - xOffset);
                stage.setY(e.getScreenY() - yOffset);
            }
        });
    }

    /**
     * 检查鼠标是否在标题栏区域
     */
    private boolean isOnTitleBar(javafx.scene.input.MouseEvent e) {
        if (e.getTarget() instanceof Node) {
            Node target = (Node) e.getTarget();
            Node parent = target;
            while (parent != null) {
                if (parent instanceof HBox && parent.getParent() == ((StackPane) ((StackPane) scene.getRoot()).getChildren().get(1))) {
                    return true;
                }
                parent = parent.getParent();
            }
        }
        return false;
    }

    /**
     * Color 转 hex 字符串
     */
    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
