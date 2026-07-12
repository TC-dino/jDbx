package dino.jdbx.examples.ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 无边框自定义窗口示例 - 玻璃拟态 (Glassmorphism)
 * <p>
 * 功能特性：
 * - 毛玻璃效果（模糊背景 + 半透明前景）
 * - 彩色渐变背景
 * - 半透明卡片
 * - 现代 UI 设计趋势
 * - 自定义标题栏
 * - 可拖拽移动窗口
 *
 * @author jDbx
 * @version 1.0
 */
public class GlassMorphismExample extends Application {

    private Stage primaryStage;
    private Scene scene;
    private double xOffset, yOffset;
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreW, restoreH;

    // 背景装饰
    private StackPane glassBackground;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 无边框 + 透明
        stage.initStyle(StageStyle.TRANSPARENT);

        // 根容器
        StackPane root = new StackPane();
        root.setPickOnBounds(false);

        // 玻璃拟态背景
        glassBackground = createGlassBackground();
        root.getChildren().add(glassBackground);

        // 主内容布局
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPickOnBounds(false);

        // 标题栏
        HBox titleBar = createTitleBar();
        mainLayout.setTop(titleBar);

        // 中央内容
        StackPane centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        root.getChildren().add(mainLayout);

        // 场景
        this.scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);

        // 窗口拖拽
        setupWindowDrag(scene, stage);

        stage.setTitle("玻璃拟态窗口 - Glassmorphism");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    /**
     * 创建玻璃拟态背景
     */
    private StackPane createGlassBackground() {
        StackPane bg = new StackPane();

        // 底层：彩色渐变背景
        Rectangle gradientBg = new Rectangle();
        gradientBg.widthProperty().bind(bg.widthProperty());
        gradientBg.heightProperty().bind(bg.heightProperty());
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(99, 102, 241)),      // 靛蓝
            new Stop(0.3, Color.rgb(168, 85, 247)),    // 紫色
            new Stop(0.6, Color.rgb(236, 72, 153)),    // 粉红
            new Stop(1, Color.rgb(251, 146, 60))       // 橙色
        );
        gradientBg.setFill(gradient);

        // 装饰元素层
        StackPane decorLayer = createDecorativeLayer();

        // 毛玻璃层（半透明 + 模糊）
        StackPane blurLayer = new StackPane();
        blurLayer.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.45);" +
            "-fx-effect: gaussian-blur(30);"
        );

        // 噪点纹理层
        Canvas noiseCanvas = new Canvas();
        noiseCanvas.widthProperty().bind(bg.widthProperty());
        noiseCanvas.heightProperty().bind(bg.heightProperty());
        noiseCanvas.setMouseTransparent(true);
        drawNoiseTexture(noiseCanvas);

        // 内容层
        StackPane contentLayer = new StackPane();
        contentLayer.setPickOnBounds(false);

        bg.getChildren().addAll(gradientBg, decorLayer, blurLayer, noiseCanvas, contentLayer);

        // 当宽度/高度变化时重绘噪点
        bg.widthProperty().addListener((obs, old, val) -> drawNoiseTexture(noiseCanvas));
        bg.heightProperty().addListener((obs, old, val) -> drawNoiseTexture(noiseCanvas));

        return bg;
    }

    /**
     * 创建装饰元素层
     */
    private StackPane createDecorativeLayer() {
        StackPane decor = new StackPane();
        decor.setMouseTransparent(true);

        // 多个装饰圆形
        Circle circle1 = createDecorCircle(180, Color.rgb(255, 255, 255, 0.2), 150, 150);
        Circle circle2 = createDecorCircle(250, Color.rgb(255, 255, 255, 0.15), 600, 200);
        Circle circle3 = createDecorCircle(150, Color.rgb(255, 255, 255, 0.1), 300, 400);
        Circle circle4 = createDecorCircle(200, Color.rgb(255, 255, 255, 0.12), 800, 350);

        decor.getChildren().addAll(circle1, circle2, circle3, circle4);
        return decor;
    }

    /**
     * 创建装饰圆形
     */
    private Circle createDecorCircle(double radius, Color fill, double centerX, double centerY) {
        Circle circle = new Circle(radius, fill);
        circle.setCenterX(centerX);
        circle.setCenterY(centerY);
        circle.setEffect(new GaussianBlur(50));
        return circle;
    }

    /**
     * 绘制噪点纹理
     */
    private void drawNoiseTexture(Canvas canvas) {
        if (canvas.getWidth() <= 0 || canvas.getHeight() <= 0) return;

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int width = (int) canvas.getWidth();
        int height = (int) canvas.getHeight();

        for (int i = 0; i < width * height * 0.08; i++) {
            double x = Math.random() * width;
            double y = Math.random() * height;
            double alpha = Math.random() * 0.06;
            gc.setFill(Color.rgb(0, 0, 0, alpha));
            gc.fillRect(x, y, 1, 1);
        }
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

        // 应用图标
        Label icon = new Label("💎");
        icon.setFont(Font.font("Segoe UI Emoji", 16));
        icon.setPadding(new Insets(0, 8, 0, 0));

        // 应用标题
        Label title = new Label("玻璃拟态窗口");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 13));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 窗口控制按钮
        Button minimizeBtn = createTitleButton("─", "最小化");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));

        Button maximizeBtn = createTitleButton("□", "最大化");
        maximizeBtn.setOnAction(e -> toggleMaximize());

        Button closeBtn = createTitleButton("×", "关闭");
        closeBtn.setOnMouseEntered(e ->
            closeBtn.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-text-fill: white; -fx-background-radius: 0; -fx-cursor: hand;")
        );
        closeBtn.setOnMouseExited(e ->
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-background-radius: 0; -fx-cursor: hand;")
        );
        closeBtn.setOnAction(e -> primaryStage.close());

        titleBar.getChildren().addAll(
            icon, title, spacer,
            minimizeBtn, maximizeBtn, closeBtn
        );

        // 双击标题栏最大化/还原
        titleBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                toggleMaximize();
            }
        });

        return titleBar;
    }

    /**
     * 创建标题栏按钮
     */
    private Button createTitleButton(String text, String tooltip) {
        Button btn = new Button(text);
        btn.setPrefSize(46, 30);
        btn.setMinSize(46, 30);
        btn.setMaxSize(46, 30);
        btn.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            if (!tooltip.equals("关闭")) {
                btn.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 0; -fx-cursor: hand;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!tooltip.equals("关闭")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 14; -fx-background-radius: 0; -fx-cursor: hand;");
            }
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
        Label welcomeLabel = new Label("玻璃拟态窗口");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.WHITE);

        // 说明文字
        Label descLabel = new Label(
            "玻璃拟态（Glassmorphism）是现代 UI 设计的趋势，\n" +
            "通过毛玻璃效果创造出层次感和深度感。\n\n" +
            "• 半透明背景\n" +
            "• 背景模糊效果\n" +
            "• 细微噪点纹理\n" +
            "• 半透明卡片"
        );
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // 特性卡片
        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createFeatureCard("💎", "毛玻璃效果", "模糊 + 半透明"),
            createFeatureCard("🎨", "彩色背景", "渐变色底层"),
            createFeatureCard("✨", "噪点纹理", "增加质感细节"),
            createFeatureCard("🃏", "半透明卡片", "层次感设计")
        );

        content.getChildren().addAll(welcomeLabel, descLabel, cards);
        center.getChildren().add(content);

        return center;
    }

    /**
     * 创建特性卡片
     */
    private VBox createFeatureCard(String icon, String title, String desc) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24, 28, 24, 28));
        card.setPrefWidth(200);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 32));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 15));
        titleLabel.setTextFill(Color.WHITE);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.rgb(255, 255, 255, 0.8));
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        // 半透明卡片样式
        String cardStyle =
            "-fx-background-color: rgba(255, 255, 255, 0.15);" +
            "-fx-background-radius: 16;" +
            "-fx-border-color: rgba(255, 255, 255, 0.25);" +
            "-fx-border-radius: 16;" +
            "-fx-cursor: hand;";
        card.setStyle(cardStyle);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                cardStyle.replace("rgba(255, 255, 255, 0.15)", "rgba(255, 255, 255, 0.25)")
                         .replace("rgba(255, 255, 255, 0.25)", "rgba(255, 255, 255, 0.4)")
            );
            card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.2)));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(cardStyle);
            card.setEffect(null);
        });

        return card;
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

    public static void main(String[] args) {
        launch(args);
    }
}
