package dino.jdbx.examples.ui;

import javafx.application.Application;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
import javafx.util.Duration;

/**
 * 无边框自定义窗口示例 - 渐变效果 (Gradient)
 * <p>
 * 功能特性：
 * - 多种渐变类型：线性、径向、扫描
 * - 动态渐变动画
 * - 多彩渐变背景
 * - 现代渐变配色
 * - 自定义标题栏
 * - 可拖拽移动窗口
 *
 * @author jDbx
 * @version 1.0
 */
public class GradientWindowExample extends Application {

    private Stage primaryStage;
    private Scene scene;
    private double xOffset, yOffset;
    private boolean isMaximized = false;
    private double restoreX, restoreY, restoreW, restoreH;

    // 背景矩形
    private Rectangle backgroundRect;
    private int currentGradientIndex = 0;
    private Timeline gradientAnimation;

    // 预设渐变
    private final LinearGradient[] gradients = {
        // 紫蓝渐变
        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(102, 126, 234)),
            new Stop(1, Color.rgb(118, 75, 162))
        ),
        // 粉红渐变
        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 154, 139)),
            new Stop(1, Color.rgb(254, 202, 87))
        ),
        // 青绿渐变
        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(0, 210, 211)),
            new Stop(1, Color.rgb(21, 101, 192))
        ),
        // 暖橙渐变
        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 123, 114)),
            new Stop(1, Color.rgb(255, 174, 108))
        ),
        // 深紫渐变
        new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(65, 88, 208)),
            new Stop(0.5, Color.rgb(200, 80, 192)),
            new Stop(1, Color.rgb(255, 204, 112))
        ),
        // 森林渐变
        new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(17, 153, 142)),
            new Stop(1, Color.rgb(56, 178, 172))
        )
    };

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        // 无边框 + 透明
        stage.initStyle(StageStyle.TRANSPARENT);

        // 根容器
        StackPane root = new StackPane();
        root.setPickOnBounds(false);

        // 渐变背景
        backgroundRect = new Rectangle();
        backgroundRect.widthProperty().bind(root.widthProperty());
        backgroundRect.heightProperty().bind(root.heightProperty());
        backgroundRect.setFill(gradients[0]);

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

        stage.setTitle("渐变效果窗口 - Gradient");
        stage.setScene(scene);
        stage.setMinWidth(600);
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

        // 应用图标
        Label icon = new Label("🌈");
        icon.setFont(Font.font("Segoe UI Emoji", 16));
        icon.setPadding(new Insets(0, 8, 0, 0));

        // 应用标题
        Label title = new Label("渐变效果窗口");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.NORMAL, 13));
        title.setTextFill(Color.WHITE);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 渐变切换按钮
        Button prevBtn = createTitleButton("◀", "上一个渐变");
        prevBtn.setOnAction(e -> switchGradient(-1));

        Label gradientName = new Label(getGradientName(0));
        gradientName.setFont(Font.font("Microsoft YaHei", 12));
        gradientName.setTextFill(Color.WHITE);
        gradientName.setPadding(new Insets(0, 8, 0, 8));

        Button nextBtn = createTitleButton("▶", "下一个渐变");
        nextBtn.setOnAction(e -> switchGradient(1));

        // 动画按钮
        Button animBtn = createTitleButton("▶", "播放动画");
        animBtn.setOnAction(e -> toggleAnimation());

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

        HBox gradientControls = new HBox(0, prevBtn, gradientName, nextBtn);
        gradientControls.setAlignment(Pos.CENTER);

        titleBar.getChildren().addAll(
            icon, title, spacer,
            gradientControls, animBtn,
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
     * 获取渐变名称
     */
    private String getGradientName(int index) {
        String[] names = {"紫蓝", "粉红", "青绿", "暖橙", "深紫", "森林"};
        return names[index % names.length];
    }

    /**
     * 切换渐变
     */
    private void switchGradient(int direction) {
        currentGradientIndex = (currentGradientIndex + direction + gradients.length) % gradients.length;
        backgroundRect.setFill(gradients[currentGradientIndex]);
    }

    /**
     * 切换动画
     */
    private void toggleAnimation() {
        if (gradientAnimation != null && gradientAnimation.getStatus() == Timeline.Status.RUNNING) {
            gradientAnimation.stop();
            gradientAnimation = null;
        } else {
            gradientAnimation = new Timeline();
            gradientAnimation.setCycleCount(Timeline.INDEFINITE);

            for (int i = 0; i < gradients.length; i++) {
                final int index = i;
                KeyFrame keyFrame = new KeyFrame(
                    Duration.seconds(2 * i),
                    new KeyValue(backgroundRect.fillProperty(), gradients[index])
                );
                gradientAnimation.getKeyFrames().add(keyFrame);
            }

            gradientAnimation.play();
        }
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
        Label welcomeLabel = new Label("渐变效果窗口");
        welcomeLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 28));
        welcomeLabel.setTextFill(Color.WHITE);

        // 说明文字
        Label descLabel = new Label(
            "渐变是现代 UI 设计中最常用的视觉效果之一，\n" +
            "可以创造出丰富的色彩层次和视觉深度。\n\n" +
            "• 线性渐变（Linear Gradient）\n" +
            "• 径向渐变（Radial Gradient）\n" +
            "• 多色渐变\n" +
            "• 动态渐变动画"
        );
        descLabel.setFont(Font.font("Microsoft YaHei", 14));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // 渐变类型卡片
        FlowPane cards = new FlowPane(16, 16);
        cards.setAlignment(Pos.CENTER);
        cards.getChildren().addAll(
            createGradientCard("线性渐变", "从一点到另一点的平滑过渡", createLinearPreview()),
            createGradientCard("径向渐变", "从中心向外扩散的渐变", createRadialPreview()),
            createGradientCard("多色渐变", "多种颜色的渐变组合", createMultiColorPreview()),
            createGradientCard("动态渐变", "自动切换的渐变动画", createAnimatedPreview())
        );

        content.getChildren().addAll(welcomeLabel, descLabel, cards);
        center.getChildren().add(content);

        return center;
    }

    /**
     * 创建线性渐变预览
     */
    private StackPane createLinearPreview() {
        Rectangle rect = new Rectangle(60, 40);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(102, 126, 234)),
            new Stop(1, Color.rgb(118, 75, 162))
        ));
        return new StackPane(rect);
    }

    /**
     * 创建径向渐变预览
     */
    private StackPane createRadialPreview() {
        Rectangle rect = new Rectangle(60, 40);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(255, 154, 139)),
            new Stop(1, Color.rgb(254, 202, 87))
        ));
        return new StackPane(rect);
    }

    /**
     * 创建多色渐变预览
     */
    private StackPane createMultiColorPreview() {
        Rectangle rect = new Rectangle(60, 40);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(65, 88, 208)),
            new Stop(0.5, Color.rgb(200, 80, 192)),
            new Stop(1, Color.rgb(255, 204, 112))
        ));
        return new StackPane(rect);
    }

    /**
     * 创建动态渐变预览
     */
    private StackPane createAnimatedPreview() {
        Circle circle = new Circle(20);
        circle.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.rgb(0, 210, 211)),
            new Stop(1, Color.rgb(21, 101, 192))
        ));

        // 旋转动画
        javafx.animation.RotateTransition rotate = new javafx.animation.RotateTransition(Duration.seconds(3), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(javafx.animation.Animation.INDEFINITE);
        rotate.play();

        return new StackPane(circle);
    }

    /**
     * 创建渐变类型卡片
     */
    private VBox createGradientCard(String name, String desc, StackPane preview) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(20, 24, 20, 24));
        card.setPrefWidth(200);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 14));
        nameLabel.setTextFill(Color.WHITE);

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Microsoft YaHei", 12));
        descLabel.setTextFill(Color.WHITE);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        card.getChildren().addAll(preview, nameLabel, descLabel);

        // 卡片样式
        String cardStyle =
            "-fx-background-color: rgba(255, 255, 255, 0.15);" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: rgba(255, 255, 255, 0.25);" +
            "-fx-border-radius: 12;" +
            "-fx-cursor: hand;";
        card.setStyle(cardStyle);

        card.setOnMouseEntered(e -> card.setStyle(
            cardStyle.replace("rgba(255, 255, 255, 0.15)", "rgba(255, 255, 255, 0.25)")
                     .replace("rgba(255, 255, 255, 0.25)", "rgba(255, 255, 255, 0.4)")
        ));
        card.setOnMouseExited(e -> card.setStyle(cardStyle));

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
