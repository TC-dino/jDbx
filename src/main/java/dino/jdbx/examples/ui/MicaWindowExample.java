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
import javafx.scene.effect.GaussianBlur;
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
 * 无边框自定义窗口示例 - 云母效果
 * <p>
 * 功能特性：
 * - 无边框窗口（StageStyle.UNDECORATED）
 * - 自定义标题栏（最小化、最大化、关闭按钮）
 * - 可拖拽移动窗口
 * - 双击标题栏最大化/还原
 * - 左侧折叠导航栏
 * - 右侧折叠详情栏
 * - 云母效果（Mica-like semi-transparent）
 * - 窗口边缘可调整大小
 *
 * @author jDbx
 * @version 1.0
 */
public class MicaWindowExample extends Application {

    // 颜色常量
    private static final Color MICA_BG = Color.rgb(243, 243, 243, 0.85);
    private static final Color MICA_BG_DARK = Color.rgb(32, 32, 32, 0.9);
    private static final Color SIDEBAR_BG = Color.rgb(245, 245, 245, 0.95);
    private static final Color SIDEBAR_HOVER = Color.rgb(0, 0, 0, 0.05);
    private static final Color SIDEBAR_ACTIVE = Color.rgb(0, 120, 212, 0.1);
    private static final Color ACCENT = Color.rgb(0, 120, 212);
    private static final Color TITLE_BAR_BG = Color.rgb(243, 243, 243, 0.8);
    private static final Color CLOSE_HOVER = Color.rgb(232, 17, 35);
    private static final Color BUTTON_HOVER = Color.rgb(0, 0, 0, 0.05);

    private Stage primaryStage;
    private double xOffset, yOffset;
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreW, restoreH;

    // 侧边栏状态
    private boolean leftSidebarExpanded = true;
    private boolean rightSidebarExpanded = true;
    private StackPane leftSidebar;
    private StackPane rightSidebar;
    private VBox leftContent;
    private VBox rightContent;
    private HBox titleBar;

    // 窗口调整大小相关
    private static final int RESIZE_MARGIN = 5;
    private double resizeX, resizeY, resizeW, resizeH;
    private boolean resizing = false;
    private Cursor resizeCursor = Cursor.DEFAULT;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 无边框 + 透明（云母效果需要）
        stage.initStyle(StageStyle.TRANSPARENT);

        // 根容器
        StackPane root = new StackPane();
        root.setPickOnBounds(false);

        // 云母效果背景层
        StackPane micaBackground = createMicaBackground();
        root.getChildren().add(micaBackground);

        // 主内容布局
        BorderPane mainLayout = new BorderPane();
        mainLayout.setPickOnBounds(false);

        // 标题栏
        titleBar = createTitleBar();
        mainLayout.setTop(titleBar);

        // 左侧边栏
        leftSidebar = createLeftSidebar();
        mainLayout.setLeft(leftSidebar);

        // 右侧边栏
        rightSidebar = createRightSidebar();
        mainLayout.setRight(rightSidebar);

        // 中央内容
        StackPane centerContent = createCenterContent();
        mainLayout.setCenter(centerContent);

        root.getChildren().add(mainLayout);

        // 场景
        Scene scene = new Scene(root, 1000, 700);
        scene.setFill(Color.TRANSPARENT);
        scene.getStylesheets().add(getClass().getResource("/dino/jdbx/mica-window.css").toExternalForm());

        // 窗口拖拽
        setupWindowDrag(scene, stage);

        // 窗口边缘调整大小
        setupWindowResize(scene, stage);

        stage.setTitle("云母效果窗口");
        stage.setScene(scene);
        stage.setMinWidth(600);
        stage.setMinHeight(400);
        stage.show();
    }

    /**
     * 创建云母效果背景
     */
    private StackPane createMicaBackground() {
        StackPane bg = new StackPane();

        // 半透明背景
        Rectangle bgRect = new Rectangle();
        bgRect.widthProperty().bind(bg.widthProperty());
        bgRect.heightProperty().bind(bg.heightProperty());
        bgRect.setFill(MICA_BG);
        bgRect.setArcWidth(8);
        bgRect.setArcHeight(8);

        // 内容层（无背景）
        StackPane contentLayer = new StackPane();
        contentLayer.setPickOnBounds(false);

        bg.getChildren().addAll(bgRect, contentLayer);

        // 将内容层绑定到外部，以便添加内容
        bg.getProperties().put("contentLayer", contentLayer);

        return bg;
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
        titleBar.setStyle(
            "-fx-background-color: rgba(243, 243, 243, 0.8);" +
            "-fx-border-color: transparent transparent rgba(0,0,0,0.1) transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );

        // 应用图标
        Label icon = new Label("◆");
        icon.setFont(Font.font("Segoe UI Symbol", 14));
        icon.setTextFill(ACCENT);
        icon.setPadding(new Insets(0, 8, 0, 0));

        // 应用标题
        Label title = new Label("云母效果窗口 - Mica Window");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 12));
        title.setTextFill(Color.rgb(51, 51, 51));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 左侧边栏切换按钮
        Button leftToggle = createTitleButton("☰", "切换左侧栏");
        leftToggle.setOnAction(e -> toggleLeftSidebar());

        // 右侧边栏切换按钮
        Button rightToggle = createTitleButton("☰", "切换右侧栏");
        rightToggle.setOnAction(e -> toggleRightSidebar());
        rightToggle.setRotate(180);

        // 窗口控制按钮
        Button minimizeBtn = createTitleButton("─", "最小化");
        minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));

        Button maximizeBtn = createTitleButton("□", "最大化");
        maximizeBtn.setOnAction(e -> toggleMaximize());

        Button closeBtn = createTitleButton("×", "关闭");
        closeBtn.setTextFill(Color.rgb(51, 51, 51));
        closeBtn.setStyle(closeBtn.getStyle() + "-fx-background-radius: 0 8 0 0;");
        closeBtn.setOnMouseEntered(e ->
            closeBtn.setStyle("-fx-background-color: rgba(232, 17, 35, 0.9); -fx-text-fill: white; -fx-background-radius: 0 8 0 0; -fx-cursor: hand;")
        );
        closeBtn.setOnMouseExited(e ->
            closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #333; -fx-background-radius: 0 8 0 0; -fx-cursor: hand;")
        );
        closeBtn.setOnAction(e -> primaryStage.close());

        titleBar.getChildren().addAll(
            icon, title, spacer,
            leftToggle, rightToggle,
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
                btn.setStyle(
                    "-fx-background-color: rgba(0, 0, 0, 0.05);" +
                    "-fx-text-fill: #333;" +
                    "-fx-font-size: 14;" +
                    "-fx-background-radius: 0;" +
                    "-fx-cursor: hand;"
                );
            }
        });
        btn.setOnMouseExited(e -> {
            if (!tooltip.equals("关闭")) {
                btn.setStyle(
                    "-fx-background-color: transparent;" +
                    "-fx-text-fill: #333;" +
                    "-fx-font-size: 14;" +
                    "-fx-background-radius: 0;" +
                    "-fx-cursor: hand;"
                );
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

        // 背景
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(sidebar.widthProperty());
        bg.heightProperty().bind(sidebar.heightProperty());
        bg.setFill(SIDEBAR_BG);

        // 内容
        leftContent = new VBox(2);
        leftContent.setPadding(new Insets(8, 4, 8, 4));
        leftContent.setAlignment(Pos.TOP_CENTER);

        // 折叠状态下的图标按钮
        VBox collapsedIcons = createCollapsedIcons(true);
        collapsedIcons.visibleProperty().bind(sidebar.widthProperty().lessThanOrEqualTo(50));
        collapsedIcons.managedProperty().bind(collapsedIcons.visibleProperty());

        // 展开状态的内容
        VBox expandedContent = createExpandedSidebarContent(true);
        expandedContent.visibleProperty().bind(sidebar.widthProperty().greaterThan(50));
        expandedContent.managedProperty().bind(expandedContent.visibleProperty());

        sidebar.getChildren().addAll(bg, leftContent);
        leftContent.getChildren().addAll(collapsedIcons, expandedContent);

        return sidebar;
    }

    /**
     * 创建右侧边栏
     */
    private StackPane createRightSidebar() {
        StackPane sidebar = new StackPane();
        sidebar.setPrefWidth(280);
        sidebar.setMinWidth(48);
        sidebar.setMaxWidth(400);

        // 背景
        Rectangle bg = new Rectangle();
        bg.widthProperty().bind(sidebar.widthProperty());
        bg.heightProperty().bind(sidebar.heightProperty());
        bg.setFill(SIDEBAR_BG);

        // 内容
        rightContent = new VBox(2);
        rightContent.setPadding(new Insets(8, 4, 8, 4));
        rightContent.setAlignment(Pos.TOP_CENTER);

        // 折叠状态下的图标按钮
        VBox collapsedIcons = createCollapsedIcons(false);
        collapsedIcons.visibleProperty().bind(sidebar.widthProperty().lessThanOrEqualTo(50));
        collapsedIcons.managedProperty().bind(collapsedIcons.visibleProperty());

        // 展开状态的内容
        VBox expandedContent = createExpandedSidebarContent(false);
        expandedContent.visibleProperty().bind(sidebar.widthProperty().greaterThan(50));
        expandedContent.managedProperty().bind(expandedContent.visibleProperty());

        sidebar.getChildren().addAll(bg, rightContent);
        rightContent.getChildren().addAll(collapsedIcons, expandedContent);

        return sidebar;
    }

    /**
     * 创建折叠状态下的图标导航
     */
    private VBox createCollapsedIcons(boolean isLeft) {
        VBox box = new VBox(4);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(8, 0, 8, 0));

        String[] icons = isLeft
            ? new String[]{"🏠", "📁", "⚙️", "👤", "❓"}
            : new String[]{"📊", "🔔", "📋", "💡", "🔧"};

        for (String icon : icons) {
            Button btn = new Button(icon);
            btn.setPrefSize(36, 36);
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-background-radius: 6;" +
                "-fx-font-size: 16;" +
                "-fx-cursor: hand;"
            );
            btn.setOnMouseEntered(e ->
                btn.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 6; -fx-font-size: 16; -fx-cursor: hand;")
            );
            btn.setOnMouseExited(e ->
                btn.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-font-size: 16; -fx-cursor: hand;")
            );
            box.getChildren().add(btn);
        }

        return box;
    }

    /**
     * 创建展开状态下的侧边栏内容
     */
    private VBox createExpandedSidebarContent(boolean isLeft) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.TOP_LEFT);
        box.setPadding(new Insets(4, 8, 4, 8));

        String title = isLeft ? "导航" : "详情";
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 13));
        titleLabel.setTextFill(Color.rgb(102, 102, 102));
        titleLabel.setPadding(new Insets(8, 4, 8, 4));

        box.getChildren().add(titleLabel);

        if (isLeft) {
            // 左侧导航菜单
            String[][] menuItems = {
                {"🏠", "主页"},
                {"📁", "文件"},
                {"📊", "数据"},
                {"⚙️", "设置"},
                {"👤", "用户"},
                {"❓", "帮助"}
            };

            for (String[] item : menuItems) {
                box.getChildren().add(createSidebarItem(item[0], item[1]));
            }
        } else {
            // 右侧详情内容
            String[][] detailItems = {
                {"📊", "统计"},
                {"🔔", "通知"},
                {"📋", "任务"},
                {"💡", "建议"},
                {"🔧", "工具"}
            };

            for (String[] item : detailItems) {
                box.getChildren().add(createSidebarItem(item[0], item[1]));
            }
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
        item.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Segoe UI Emoji", 14));

        Label textLabel = new Label(text);
        textLabel.setFont(Font.font("Microsoft YaHei", 13));
        textLabel.setTextFill(Color.rgb(51, 51, 51));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        item.getChildren().addAll(iconLabel, textLabel, spacer);

        // 悬停效果
        item.setOnMouseEntered(e ->
            item.setStyle("-fx-background-color: rgba(0,0,0,0.05); -fx-background-radius: 6; -fx-cursor: hand;")
        );
        item.setOnMouseExited(e ->
            item.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;")
        );
        item.setOnMouseClicked(e -> {
            // 清除其他项的选中状态
            item.getParent().getChildrenUnmodifiable().forEach(node -> {
                if (node instanceof HBox) {
                    node.setStyle("-fx-background-color: transparent; -fx-background-radius: 6; -fx-cursor: hand;");
                }
            });
            // 设置当前项为选中状态
            item.setStyle(
                "-fx-background-color: rgba(0, 120, 212, 0.1);" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;"
            );
        });

        return item;
    }

    /**
     * 创建中央内容区域
     */
    private StackPane createCenterContent() {
        StackPane center = new StackPane();
        center.setPadding(new Insets(0));

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(30, 40, 30, 40));

        // 欢迎标题
        Label welcomeLabel = new Label("云母效果窗口示例");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.rgb(32, 32, 32));

        // 说明文字
        Label descLabel = new Label(
            "这是一个使用 JavaFX 实现的无边框窗口示例，具有以下特性：\n" +
            "• 自定义标题栏（最小化、最大化、关闭按钮）\n" +
            "• 可拖拽移动窗口\n" +
            "• 双击标题栏最大化/还原\n" +
            "• 左侧折叠导航栏\n" +
            "• 右侧折叠详情栏\n" +
            "• 云母效果（Mica-like semi-transparent）\n" +
            "• 窗口边缘可调整大小"
        );
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.rgb(80, 80, 80));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);

        // 功能卡片
        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createFeatureCard("🎨", "云母效果", "半透明磨砂玻璃背景"),
            createFeatureCard("📐", "无边框窗口", "完全自定义的窗口外观"),
            createFeatureCard("↔️", "可调整大小", "支持边缘拖拽调整窗口尺寸"),
            createFeatureCard("📌", "折叠侧边栏", "左侧导航 + 右侧详情")
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
            "-fx-background-color: rgba(255, 255, 255, 0.7);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(0, 0, 0, 0.05);" +
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

        // 悬停效果
        card.setOnMouseEntered(e ->
            card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.9);" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(0, 120, 212, 0.3);" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,120,212,0.15), 12, 0, 0, 4);"
            )
        );
        card.setOnMouseExited(e ->
            card.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.7);" +
                "-fx-background-radius: 12;" +
                "-fx-border-color: rgba(0, 0, 0, 0.05);" +
                "-fx-border-radius: 12;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);"
            )
        );

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
     * 切换右侧边栏
     */
    private void toggleRightSidebar() {
        rightSidebarExpanded = !rightSidebarExpanded;
        double targetWidth = rightSidebarExpanded ? 280 : 48;
        animateWidth(rightSidebar, targetWidth);
    }

    /**
     * 动画调整宽度
     */
    private void animateWidth(StackPane node, double targetWidth) {
        javafx.animation.KeyValue kv = new javafx.animation.KeyValue(
            node.prefWidthProperty(),
            targetWidth,
            javafx.animation.Interpolator.EASE_BOTH
        );
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(
            javafx.util.Duration.millis(200),
            kv
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
            // 还原
            primaryStage.setX(restoreX);
            primaryStage.setY(restoreY);
            primaryStage.setWidth(restoreW);
            primaryStage.setHeight(restoreH);
        } else {
            // 保存当前状态
            restoreX = primaryStage.getX();
            restoreY = primaryStage.getY();
            restoreW = primaryStage.getWidth();
            restoreH = primaryStage.getHeight();

            // 最大化
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
            // 仅在标题栏区域允许拖拽
            if (e.getTarget() instanceof Node) {
                Node target = (Node) e.getTarget();
                boolean onTitleBar = false;
                Node parent = target;
                while (parent != null) {
                    if (parent == titleBar) {
                        onTitleBar = true;
                        break;
                    }
                    parent = parent.getParent();
                }

                if (onTitleBar) {
                    xOffset = e.getScreenX() - stage.getX();
                    yOffset = e.getScreenY() - stage.getY();
                }
            }
        });

        scene.setOnMouseDragged(e -> {
            if (e.getTarget() instanceof Node) {
                Node target = (Node) e.getTarget();
                boolean onTitleBar = false;
                Node parent = target;
                while (parent != null) {
                    if (parent == titleBar) {
                        onTitleBar = true;
                        break;
                    }
                    parent = parent.getParent();
                }

                if (onTitleBar && !isMaximized) {
                    stage.setX(e.getScreenX() - xOffset);
                    stage.setY(e.getScreenY() - yOffset);
                } else if (onTitleBar && isMaximized) {
                    // 从最大化状态拖拽时还原
                    isMaximized = false;
                    double mouseX = e.getScreenX();
                    double newWidth = restoreW;
                    stage.setWidth(newWidth);
                    stage.setHeight(restoreH);
                    stage.setX(mouseX - newWidth / 2);
                    stage.setY(e.getScreenY() - yOffset);
                    xOffset = newWidth / 2;
                }
            }
        });
    }

    /**
     * 设置窗口边缘调整大小
     */
    private void setupWindowResize(Scene scene, Stage stage) {
        scene.setOnMouseMoved(e -> {
            if (isMaximized) {
                scene.setCursor(Cursor.DEFAULT);
                return;
            }

            double x = e.getX();
            double y = e.getY();
            double w = scene.getWidth();
            double h = scene.getHeight();

            boolean left = x < RESIZE_MARGIN;
            boolean right = x > w - RESIZE_MARGIN;
            boolean top = y < RESIZE_MARGIN;
            boolean bottom = y > h - RESIZE_MARGIN;

            if (left && top) {
                scene.setCursor(Cursor.NW_RESIZE);
            } else if (right && top) {
                scene.setCursor(Cursor.NE_RESIZE);
            } else if (left && bottom) {
                scene.setCursor(Cursor.SW_RESIZE);
            } else if (right && bottom) {
                scene.setCursor(Cursor.SE_RESIZE);
            } else if (left) {
                scene.setCursor(Cursor.W_RESIZE);
            } else if (right) {
                scene.setCursor(Cursor.E_RESIZE);
            } else if (top) {
                scene.setCursor(Cursor.N_RESIZE);
            } else if (bottom) {
                scene.setCursor(Cursor.S_RESIZE);
            } else {
                scene.setCursor(Cursor.DEFAULT);
            }
        });

        scene.setOnMousePressed(e -> {
            if (isMaximized) return;

            Cursor cursor = scene.getCursor();
            if (cursor == Cursor.DEFAULT) return;

            resizing = true;
            resizeX = e.getScreenX();
            resizeY = e.getScreenY();
            resizeW = stage.getWidth();
            resizeH = stage.getHeight();
        });

        scene.setOnMouseDragged(e -> {
            if (!resizing || isMaximized) return;

            double dx = e.getScreenX() - resizeX;
            double dy = e.getScreenY() - resizeY;
            Cursor cursor = scene.getCursor();

            double newW = resizeW;
            double newH = resizeH;
            double newX = stage.getX();
            double newY = stage.getY();

            if (cursor == Cursor.E_RESIZE || cursor == Cursor.NE_RESIZE || cursor == Cursor.SE_RESIZE) {
                newW = Math.max(stage.getMinWidth(), resizeW + dx);
            }
            if (cursor == Cursor.W_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.SW_RESIZE) {
                newW = Math.max(stage.getMinWidth(), resizeW - dx);
                newX = stage.getX() + (resizeW - newW);
            }
            if (cursor == Cursor.S_RESIZE || cursor == Cursor.SE_RESIZE || cursor == Cursor.SW_RESIZE) {
                newH = Math.max(stage.getMinHeight(), resizeH + dy);
            }
            if (cursor == Cursor.N_RESIZE || cursor == Cursor.NW_RESIZE || cursor == Cursor.NE_RESIZE) {
                newH = Math.max(stage.getMinHeight(), resizeH - dy);
                newY = stage.getY() + (resizeH - newH);
            }

            stage.setX(newX);
            stage.setY(newY);
            stage.setWidth(newW);
            stage.setHeight(newH);
        });

        scene.setOnMouseReleased(e -> {
            resizing = false;
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
