package dino.jdbx.examples;

import com.dustinredmond.fxtrayicon.FXTrayIcon;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * FXTrayIcon 示例 - 演示 FXTrayIcon 功能
 *
 * 特性：
 * - 使用 FXTrayIcon 显示托盘图标
 * - 使用 JavaFX 弹出菜单替代 AWT 原生菜单，完美支持中文
 * - 支持右键点击弹出菜单
 * - 支持双击恢复窗口
 * - 支持最小化到托盘
 * - 支持系统通知（信息、警告、错误）
 * - 支持深色模式切换
 * - 支持动态提示文本
 *
 * 技术说明：
 * FXTrayIcon 内部将 JavaFX MenuItem 转换为 AWT MenuItem，
 * 导致中文字符在 Windows 上显示为方块。
 * 本示例使用 TrayContextMenu（基于 JavaFX Popup）替代，
 * 完美解决中文显示问题。
 */
public class FXTrayIconExample extends Application {

    private FXTrayIcon trayIcon;
    private Stage primaryStage;
    private boolean darkMode = false;
    private boolean notificationsEnabled = true;
    private Timer statusTimer;
    private TrayContextMenu contextMenu;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        javafx.scene.control.Label title = new javafx.scene.control.Label("FXTrayIcon 功能演示");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #333;");
        BorderPane.setAlignment(title, javafx.geometry.Pos.CENTER);
        root.setTop(title);

        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        HBox bottomBox = createBottomButtons();
        BorderPane.setAlignment(bottomBox, javafx.geometry.Pos.CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 650, 500);
        stage.setTitle("FXTrayIcon 功能演示");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            event.consume();
            minimizeToTray();
        });

        stage.show();
        initTrayIcon();
    }

    private VBox createCenterContent() {
        VBox box = new VBox(10);
        box.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label("FXTrayIcon 功能：");
        descLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        descLabel.setStyle("-fx-text-fill: #333;");

        javafx.scene.control.TextArea infoArea = new javafx.scene.control.TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(280);
        infoArea.setFont(Font.font("Microsoft YaHei", 13));
        infoArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");
        infoArea.setText(
            "右键菜单功能：\n" +
            "  - 显示窗口 / 最小化到托盘\n" +
            "  - 通知类型：默认、信息、警告、错误\n" +
            "  - 复选菜单项：深色模式、启用通知\n" +
            "  - 子菜单：设置（含嵌套项）\n" +
            "  - 分隔线\n" +
            "  - 自定义退出（带确认）\n\n" +
            "交互方式：\n" +
            "  - 左键点击托盘图标：显示/隐藏窗口\n" +
            "  - 右键点击托盘图标：弹出菜单\n" +
            "  - 关闭窗口：最小化到托盘\n\n" +
            "其他功能：\n" +
            "  - 动态提示文本\n" +
            "  - 定时状态更新\n" +
            "  - 自动生成图标（蓝色圆圈 + 'J'）\n\n" +
            "技术说明：\n" +
            "使用 JavaFX 弹出菜单替代 AWT 原生菜单，\n" +
            "完美支持中文等 Unicode 字符显示。"
        );

        box.getChildren().addAll(descLabel, infoArea);
        return box;
    }

    private HBox createBottomButtons() {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        javafx.scene.control.Button initBtn = new javafx.scene.control.Button("初始化托盘");
        initBtn.setStyle(getButtonStyle("#337ab7", "white"));
        initBtn.setOnAction(e -> initTrayIcon());

        javafx.scene.control.Button minimizeBtn = new javafx.scene.control.Button("最小化");
        minimizeBtn.setStyle(getButtonStyle("#f0ad4e", "white"));
        minimizeBtn.setOnAction(e -> minimizeToTray());

        javafx.scene.control.Button notifyBtn = new javafx.scene.control.Button("通知");
        notifyBtn.setStyle(getButtonStyle("#5cb85c", "white"));
        notifyBtn.setOnAction(e -> sendNotification("default"));

        javafx.scene.control.Button statusBtn = new javafx.scene.control.Button("切换状态定时器");
        statusBtn.setStyle(getButtonStyle("#9b59b6", "white"));
        statusBtn.setOnAction(e -> toggleStatusTimer());

        javafx.scene.control.Button exitBtn = new javafx.scene.control.Button("退出");
        exitBtn.setStyle(getButtonStyle("#d9534f", "white"));
        exitBtn.setOnAction(e -> exitApp());

        box.getChildren().addAll(initBtn, minimizeBtn, notifyBtn, statusBtn, exitBtn);
        return box;
    }

    private void initTrayIcon() {
        if (trayIcon != null && trayIcon.isShowing()) {
            showAlert("提示", "托盘已初始化");
            return;
        }

        BufferedImage trayImage = createDefaultImage();

        // 使用 FXTrayIcon 创建托盘图标（不使用其菜单功能）
        trayIcon = new FXTrayIcon(primaryStage, trayImage);

        // 创建 JavaFX 右键菜单
        contextMenu = new TrayContextMenu(primaryStage);
        buildContextMenu();

        // 获取底层的 AWT TrayIcon 并添加鼠标监听器
        java.awt.TrayIcon awtTrayIcon = trayIcon.getRestricted().getTrayIcon();
        awtTrayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    // 双击：恢复窗口
                    Platform.runLater(() -> {
                        primaryStage.show();
                        primaryStage.toFront();
                    });
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    // 右键：显示 JavaFX 菜单
                    Platform.runLater(() -> {
                        contextMenu.show(e.getXOnScreen(), e.getYOnScreen());
                    });
                }
            }
        });

        // 显示托盘图标
        trayIcon.show();

        // 设置提示文本
        trayIcon.setTooltip("jDbx FXTrayIcon 演示");

        showAlert("提示", "托盘图标已初始化！\n左键点击：显示/隐藏窗口\n右键点击：弹出菜单");
    }

    /**
     * 构建右键菜单
     */
    private void buildContextMenu() {
        contextMenu.clear();

        // 窗口控制
        contextMenu
            .addItem("显示窗口", () -> {
                primaryStage.show();
                primaryStage.toFront();
            })
            .addItem("最小化到托盘", this::minimizeToTray)
            .addSeparator();

        // 通知子菜单
        contextMenu.addItem("发送默认通知", () -> sendNotification("default"));
        contextMenu.addItem("发送信息通知", () -> sendNotification("info"));
        contextMenu.addItem("发送警告通知", () -> sendNotification("warn"));
        contextMenu.addItem("发送错误通知", () -> sendNotification("error"));
        contextMenu.addSeparator();

        // 设置选项
        contextMenu.addItem("深色模式: " + (darkMode ? "开" : "关"), () -> {
            darkMode = !darkMode;
            updateTheme();
            if (notificationsEnabled) {
                trayIcon.showMessage("主题", "深色模式: " + (darkMode ? "开" : "关"));
            }
        });

        contextMenu.addItem("启用通知: " + (notificationsEnabled ? "开" : "关"), () -> {
            notificationsEnabled = !notificationsEnabled;
            if (notificationsEnabled) {
                trayIcon.showMessage("通知", "通知已启用");
            }
        });

        contextMenu.addSeparator();

        // 语言切换
        contextMenu.addItem("语言: 中文", () -> {
            // 这里可以添加语言切换逻辑
            if (notificationsEnabled) {
                trayIcon.showMessage("语言", "当前语言: 中文");
            }
        });

        // 重置设置
        contextMenu.addItem("重置为默认设置", () -> {
            darkMode = false;
            notificationsEnabled = true;
            updateTheme();
            buildContextMenu(); // 重建菜单以更新显示
            if (notificationsEnabled) {
                trayIcon.showMessage("设置", "已重置为默认设置");
            }
        });

        contextMenu.addSeparator();

        // 退出
        contextMenu.addItem("退出", this::exitApp);
    }

    private void sendNotification(String type) {
        if (trayIcon == null || !notificationsEnabled) return;

        switch (type) {
            case "info":
                trayIcon.showInfoMessage("信息", "这是一条信息通知");
                break;
            case "warn":
                trayIcon.showMessage("警告", "这是一条警告通知");
                break;
            case "error":
                trayIcon.showErrorMessage("错误", "这是一条错误通知");
                break;
            default:
                trayIcon.showMessage("通知", "这是一条默认通知");
                break;
        }
    }

    private void updateTheme() {
        Platform.runLater(() -> {
            Scene scene = primaryStage.getScene();
            if (scene != null) {
                String bg = darkMode ? "#2c2c2c" : "#f5f5f5";
                scene.getRoot().setStyle("-fx-background-color: " + bg + ";");
            }
        });
    }

    private void toggleStatusTimer() {
        if (statusTimer != null) {
            statusTimer.cancel();
            statusTimer = null;
            showAlert("定时器", "状态定时器已停止");
            return;
        }

        statusTimer = new Timer(true);
        statusTimer.scheduleAtFixedRate(new TimerTask() {
            int count = 0;
            @Override
            public void run() {
                count++;
                if (trayIcon != null && notificationsEnabled) {
                    Platform.runLater(() ->
                        trayIcon.setTooltip("jDbx | 运行时间: " + count + "秒")
                    );
                }
            }
        }, 1000, 1000);

        showAlert("定时器", "状态定时器已启动（每秒更新提示文本）");
    }

    private BufferedImage createDefaultImage() {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(java.awt.Color.BLUE);
        g2d.fillOval(2, 2, 12, 12);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 10));
        g2d.drawString("J", 5, 12);
        g2d.dispose();
        return image;
    }

    private void minimizeToTray() {
        if (trayIcon == null || !trayIcon.isShowing()) {
            showAlert("提示", "请先初始化托盘");
            return;
        }
        Platform.runLater(() -> {
            primaryStage.hide();
            if (notificationsEnabled) {
                trayIcon.showMessage("jDbx", "窗口已最小化到系统托盘");
            }
        });
    }

    private void exitApp() {
        if (statusTimer != null) {
            statusTimer.cancel();
        }
        if (trayIcon != null) {
            trayIcon.hide();
        }
        Platform.exit();
        System.exit(0);
    }

    private String getButtonStyle(String bgColor, String textColor) {
        return "-fx-background-color: " + bgColor + "; " +
               "-fx-text-fill: " + textColor + "; " +
               "-fx-background-radius: 4; " +
               "-fx-padding: 8 12; " +
               "-fx-font-size: 12;";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
