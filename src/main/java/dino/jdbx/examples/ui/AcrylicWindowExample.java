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
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * 无边框自定义窗口示例 - 亚克力效果 (Acrylic)
 * <p>
 * 功能特性：
 * - 亚克力效果（模糊透明 + 噪点纹理）
 * - 类似 Windows 10/11 Acrylic 效果
 * - 支持亮色/暗色主题切换
 * - 自定义标题栏
 * - 可拖拽移动窗口
 * - 左侧折叠导航栏
 *
 * @author jDbx
 * @version 1.0
 */
public class AcrylicWindowExample extends Application {

    // 颜色常量
    private static final Color ACCENT = Color.rgb(0, 120, 212);
    private static final Color LIGHT_BG = Color.rgb(249, 249, 249, 0.85);
    private static final Color DARK_BG = Color.rgb(32, 32, 32, 0.85);
    private static final Color LIGHT_CARD = Color.rgb(255, 255, 255, 0.7);
    private static final Color DARK_CARD = Color.rgb(45, 45, 45, 0.7);

    private Stage primaryStage;
    private double xOffset, yOffset;
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreW, restoreH;
    private boolean isDarkTheme = false;
    private boolean leftSidebarExpanded = true;

    // UI 组件
    private StackPane root;
    private StackPane acrylicBackground;
    private Canvas noiseCanvas;
    private BorderPane mainLayout;
    private HBox titleBar;
    private StackPane leftSidebar;
    private VBox leftContent;

    // 背景装饰元素
    private Circle circle1, circle2, circle3;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 无边框 + 透明
        stage.initStyle(StageStyle.TRANSPARENT);

        // 根容器
        root = new StackPane();
        root.setPickOnBounds(false);

        // 亚克力效果背景
        acrylicBackground = createAcrylicBackground();
        root.getChildren().add(acrylicBackground);

        // 主内容布局
        mainLayout = new BorderPane();
        mainLayout.setPickOnBounds(false);

        // 标题栏
        titleBar = createTitleBar();
        mainLayout.setTop(titleBar);

        // 左侧边栏
        leftSidebar = createLeftSidebar();
        mainLayout.setLeft(leftSidebar);

        // 中央内容
        StackPane centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        root.getChildren().add(mainLayout);

        // 场景
        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);

        // 窗口拖拽
        setupWindowDrag(scene, stage);

        stage.setTitle("亚克力效果窗口 - Acrylic");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    /**
     * 创建亚克力效果背景
     */
    private StackPane createAcrylicBackground() {
        StackPane bg = new StackPane();

        // 底层：彩色渐变背景（模拟桌面背景）
        Rectangle gradientBg = new Rectangle();
        gradientBg.widthProperty().bind(bg.widthProperty());
        gradientBg.heightProperty().bind(bg.heightProperty());
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(102, 126, 234)),
            new Stop(0.5, Color.rgb(118, 75, 162)),
            new Stop(1, Color.rgb(255, 154, 139))
        );
        gradientBg.setFill(gradient);

        // 装饰圆形元素
        circle1 = createDecorativeCircle(150, Color.rgb(255, 255, 255, 0.2), 100);
        circle2 = createDecorativeCircle(200, Color.rgb(255, 255, 255, 0.15), 200);
        circle3 = createDecorativeCircle(120, Color.rgb(255, 255, 255, 0.1), 350);

        StackPane decorLayer = new StackPane(circle1, circle2, circle3);
        decorLayer.setMouseTransparent(true);

        // 中层：亚克力效果（半透明 + 模糊）
        StackPane acrylicLayer = new StackPane();
        acrylicLayer.setStyle(
            "-fx-background-color: rgba(249, 249, 249, 0.7);" +
            "-fx-effect: gaussian-blur(20);"
        );

        // 噪点纹理层
        noiseCanvas = new Canvas();
        noiseCanvas.widthProperty().bind(bg.widthProperty());
        noiseCanvas.heightProperty().bind(bg.heightProperty());
        noiseCanvas.setMouseTransparent(true);
        drawNoiseTexture();

        // 顶层：内容层
        StackPane contentLayer = new StackPane();
        contentLayer.setPickOnBounds(false);

        bg.getChildren().addAll(gradientBg, decorLayer, acrylicLayer, noiseCanvas, contentLayer);

        // 当宽度/高度变化时重绘噪点
        bg.widthProperty().addListener((obs, old, val) -> drawNoiseTexture());
        bg.heightProperty().addListener((obs, old, val) -> drawNoiseTexture());

        return bg;
    }

    /**
     * 创建装饰圆形
     */
    private Circle createDecorativeCircle(double radius, Color fill, double layoutX) {
        Circle circle = new Circle(radius, fill);
        circle.setCenterX(layoutX);
        circle.setCenterY(100);
        circle.setEffect(new GaussianBlur(40));
        return circle;
    }

    /**
     * 绘制噪点纹理（亚克力效果的关键）
     */
    private void drawNoiseTexture() {
        if (noiseCanvas.getWidth() <= 0 || noiseCanvas.getHeight() <= 0) return;

        GraphicsContext gc = noiseCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, noiseCanvas.getWidth(), noiseCanvas.getHeight());

        int width = (int) noiseCanvas.getWidth();
        int height = (int) noiseCanvas.getHeight();

        // 绘制随机噪点
        for (int i = 0; i < width * height * 0.1; i++) {
            double x = Math.random() * width;
            double y = Math.random() * height;
            double alpha = Math.random() * 0.08; // 非常低的透明度
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
        titleBar.setPadding(new Insets(0, 8, 0, 12));
        titleBar.setPrefHeight(36);
        titleBar.setMinHeight(36);
        titleBar.setMaxHeight(36);

        // 应用图标
        Label icon = new Label("◈");
        icon.setFont(Font.font("Segoe UI Symbol", 16));
        icon.setTextFill(ACCENT);
        icon.setPadding(new Insets(0, 8, 0, 0));

        // 应用标题
        Label title = new Label("亚克力效果窗口");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 12));
        title.setTextFill(Color.rgb(51, 51, 51));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 左侧边栏切换按钮
        Button leftToggle = createTitleButton("☰", "切换左侧栏");
        leftToggle.setOnAction(e -> toggleLeftSidebar());

        // 主题切换按钮
        Button themeBtn = createTitleButton("☀", "切换主题");
        themeBtn.setOnAction(e -> toggleTheme());

        // 窗口控制按钮
        Button minimizeBtn = createTitleButton("─", "最小化");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));

        Button maximizeBtn = createTitleButton("□", "最大化");
        maximizeBtn.setOnAction(e -> toggleMaximize());

        Button closeBtn = createTitleButton("×", "关闭");
        closeBtn.setOnMouseEntered(e ->
            closeBtn.setStyle("-fx-background-color: rgba(232, 17, 35, 0.9); -fx-text-fill: white; -fx-background-radius: 0; -fx-cursor: hand;")
        );
        closeBtn.setOnMouseExited(e ->
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-background-radius: 0; -fx-cursor: hand;")
        );
        closeBtn.setOnAction(e -> primaryStage.close());

        titleBar.getChildren().addAll(
            icon, title, spacer,
            leftToggle, themeBtn,
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
            "-fx-text-fill: #333;" +
            "-fx-font-size: 14;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;"
        );
        btn.setOnMouseEntered(e -> {
            if (!tooltip.equals("关闭")) {
                btn.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 0; -fx-cursor: hand;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!tooltip.equals("关闭")) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-font-size: 14; -fx-background-radius: 0; -fx-cursor: hand;");
            }
        });
        return btn;
    }

    /**
     * 创建左侧边栏
     */
    private StackPane createLeftSidebar() {
        StackPane sidebar = new StackPane();
        sidebar.setPrefWidth(240);
        sidebar.setMinWidth(48);
        sidebar.setMaxWidth(300);

        // 内容
        leftContent = new VBox(2);
        leftContent.setPadding(new Insets(8, 4, 8, 4));
        leftContent.setAlignment(Pos.TOP_CENTER);

        // 折叠状态下的图标按钮
        VBox collapsedIcons = createCollapsedIcons();
        collapsedIcons.visibleProperty().bind(sidebar.widthProperty().lessThanOrEqualTo(50));
        collapsedIcons.managedProperty().bind(collapsedIcons.visibleProperty());

        // 展开状态的内容
        VBox expandedContent = createExpandedSidebarContent();
        expandedContent.visibleProperty().bind(sidebar.widthProperty().greaterThan(50));
        expandedContent.managedProperty().bind(expandedContent.visibleProperty());

        sidebar.getChildren().add(leftContent);
        leftContent.getChildren().addAll(collapsedIcons, expandedContent);

        return sidebar;
    }

    /**
     * 创建折叠状态下的图标导航
     */
    private VBox createCollapsedIcons() {
        VBox box = new VBox(4);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(8, 0, 8, 0));

        String[] icons = {"🏠", "📁", "📊", "⚙️", "👤"};

        for (String icon : icons) {
            Button btn = new Button(icon);
            btn.setPrefSize(36, 36);
            btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-font-size: 16; -fx-cursor: hand;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 6; -fx-font-size: 16; -fx-cursor: hand;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-font-size: 16; -fx-cursor: hand;"));
            box.getChildren().add(btn);
        }

        return box;
    }

    /**
     * 创建展开状态下的侧边栏内容
     */
    private VBox createExpandedSidebarContent() {
        VBox box = new VBox(2);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(4, 8, 4, 8));

        Label titleLabel = new Label("导航");
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.rgb(102, 102, 102));
        titleLabel.setPadding(new Insets(8, 4, 8, 4));

        box.getChildren().add(titleLabel);

        String[][] menuItems = {
            {"🏠", "主页"},
            {"📁", "文件"},
            {"📊", "数据"},
            {"⚙️", "设置"},
            {"👤", "用户"}
        };

        for (String[] item : menuItems) {
            box.getChildren().add(createSidebarItem(item[0], item[1]));
        }

        return box;
    }

    /**
     * 创建侧边栏菜单项
     */
    private HBox createSidebarItem(String icon, String text) {
        HBox item = new HBox(10);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(8, 12, 8, 12));
        item.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;");

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 14));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Microsoft YaHei", 13));
        textLabel.setTextFill(Color.rgb(51, 51, 51));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        item.getChildren().addAll(iconLabel, textLabel, spacer);

        item.setOnMouseEntered(e -> item.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 6; -fx-cursor: hand;"));
        item.setOnMouseExited(e -> item.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;"));
        item.setOnMouseClicked(e -> {
            item.getParent().getChildrenUnmodifiable().forEach(node -> {
                if (node instanceof HBox) {
                    node.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;");
                }
            });
            item.setStyle("-fx-background-color: rgba(0, 120, 212, 0.1); -fx-background-radius: 6; -fx-cursor: hand;");
        });

        return item;
    }

    /**
     * 创建中央内容区域
     */
    private StackPane createCenterContent() {
        StackPane center = new StackPane();

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30, 40, 30, 40));

        // 欢迎标题
        Label welcomeLabel = new Label("亚克力效果窗口");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.rgb(32, 32, 32));

        // 说明文字
        Label descLabel = new Label(
            "亚克力效果是 Windows 10/11 的标志性设计语言，\n" +
            "通过模糊透明背景 + 噪点纹理实现。\n\n" +
            "• 半透明背景色\n" +
            "• 背景模糊效果\n" +
            "• 细微噪点纹理\n" +
            "• 彩色渐变底层"
        );
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.rgb(80, 80, 80));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // 效果演示卡片
        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createFeatureCard("◈", "亚克力效果", "模糊 + 噪点纹理"),
            createFeatureCard("🎨", "彩色背景", "渐变色底层装饰"),
            createFeatureCard("☀️", "主题切换", "支持亮色/暗色主题"),
            createFeatureCard("↔️", "可折叠侧边栏", "图标/文字自适应")
        );

        content.getChildren().addAll(welcomeLabel, descLabel, cards);
        center.getChildren().add(content);

        return center;
    }

    /**
     * 创建功能卡片
     */
    private VBox createFeatureCard(String icon, String title, String desc) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setPrefWidth(200);
        card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.6);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255, 255, 255, 0.4);" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 28));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        titleLabel.setTextFill(Color.rgb(32, 32, 32));

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.rgb(102, 102, 102));
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel, descLabel);

        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.8);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(0, 120, 212, 0.3);" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,120,212,0.15), 12, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color: rgba(255, 255, 255, 0.6);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255, 255, 255, 0.4);" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
        ));

        return card;
    }

    /**
     * 切换左侧边栏
     */
    private void toggleLeftSidebar() {
        leftSidebarExpanded = !leftSidebarExpanded;
        double targetWidth = leftSidebarExpanded ? 240 : 48;
        animateWidth(leftSidebar, targetWidth);
    }

    /**
     * 切换主题
     */
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        // 更新背景颜色
        acrylicBackground.getChildren().get(2).setStyle(
            isDarkTheme ?
            "-fx-background-color: rgba(32, 32, 32, 0.7); -fx-effect: gaussian-blur(20);" :
            "-fx-background-color: rgba(249, 249, 249, 0.7); -fx-effect: gaussian-blur(20);"
        );
    }

    /**
     * 动画调整宽度
     */
    private void animateWidth(StackPane node, double targetWidth) {
        javafx.animation.KeyValue kv = new javafx.animation.KeyValue(
            node.prefWidthProperty(), targetWidth, javafx.animation.Interpolator.EASE_BOTH
        );
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(200), kv);
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
                if (parent == titleBar) return true;
                parent = parent.getParent();
            }
        }
        return false;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
