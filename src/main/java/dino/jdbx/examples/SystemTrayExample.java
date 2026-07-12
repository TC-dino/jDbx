package dino.jdbx.examples;

import dino.jdbx.TrayContextMenu;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * System Tray Example - 演示系统托盘图标、右键菜单、通知和最小化到托盘
 *
 * 特性：
 * - 使用 AWT SystemTray 显示托盘图标
 * - 使用 JavaFX 弹出菜单替代 AWT 原生菜单，完美支持中文
 * - 支持右键点击弹出菜单
 * - 支持双击恢复窗口
 * - 支持最小化到托盘
 * - 支持系统通知
 */
public class SystemTrayExample extends Application {

    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private Stage primaryStage;
    private TrayContextMenu contextMenu;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("系统托盘示例");
        title.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #333;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        HBox bottomBox = createBottomButtons();
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 600, 450);
        stage.setTitle("系统托盘示例");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            event.consume();
            minimizeToTray();
        });

        stage.show();
        initSystemTray();
    }

    private VBox createCenterContent() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20, 0, 0, 0));

        Label descLabel = new Label("系统托盘功能：");
        descLabel.setFont(Font.font("Microsoft YaHei", FontWeight.BOLD, 16));
        descLabel.setStyle("-fx-text-fill: #333;");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(200);
        infoArea.setFont(Font.font("Microsoft YaHei", 14));
        infoArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");
        infoArea.setText(
            "1. 点击「初始化托盘」创建系统托盘图标\n" +
            "2. 点击「最小化到托盘」隐藏窗口\n" +
            "3. 双击托盘图标恢复窗口\n" +
            "4. 右键点击托盘图标弹出菜单\n" +
            "5. 关闭窗口时自动最小化到托盘\n\n" +
            "技术说明：\n" +
            "使用 JavaFX 弹出菜单替代 AWT 原生菜单，\n" +
            "完美支持中文等 Unicode 字符显示。"
        );

        box.getChildren().addAll(descLabel, infoArea);
        return box;
    }

    private HBox createBottomButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));

        Button initBtn = new Button("初始化托盘");
        initBtn.setStyle(getButtonStyle("#337ab7", "white"));
        initBtn.setOnAction(e -> initSystemTray());

        Button minimizeBtn = new Button("最小化到托盘");
        minimizeBtn.setStyle(getButtonStyle("#f0ad4e", "white"));
        minimizeBtn.setOnAction(e -> minimizeToTray());

        Button notifyBtn = new Button("发送通知");
        notifyBtn.setStyle(getButtonStyle("#5cb85c", "white"));
        notifyBtn.setOnAction(e -> showNotification("你好！", "这是来自 jDbx 的通知"));

        Button exitBtn = new Button("退出");
        exitBtn.setStyle(getButtonStyle("#d9534f", "white"));
        exitBtn.setOnAction(e -> exitApp());

        box.getChildren().addAll(initBtn, minimizeBtn, notifyBtn, exitBtn);
        return box;
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported()) {
            showAlert("提示", "当前平台不支持系统托盘");
            return;
        }

        if (systemTray != null && trayIcon != null) {
            showAlert("提示", "托盘已初始化");
            return;
        }

        systemTray = SystemTray.getSystemTray();

        trayIcon = new TrayIcon(createDefaultImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("jDbx 系统托盘");

        // 创建 JavaFX 右键菜单
        contextMenu = TrayContextMenu.create(primaryStage)
            .item("显示窗口", () -> {
                primaryStage.show();
                primaryStage.toFront();
            })
            .item("最小化到托盘", this::minimizeToTray)
            .separator()
            .item("发送通知", () -> showNotification("测试", "来自右键菜单的通知"))
            .separator()
            .item("退出", this::exitApp);

        // 添加鼠标监听器
        trayIcon.addMouseListener(new MouseAdapter() {
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

        try {
            systemTray.add(trayIcon);
            showNotification("jDbx", "系统托盘初始化成功");
        } catch (AWTException e) {
            showAlert("错误", "添加托盘图标失败: " + e.getMessage());
        }
    }

    private Image createDefaultImage() {
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
        if (systemTray == null || trayIcon == null) {
            showAlert("提示", "请先初始化托盘");
            return;
        }
        Platform.runLater(() -> {
            primaryStage.hide();
            showNotification("jDbx", "窗口已最小化到系统托盘");
        });
    }

    private void showNotification(String title, String message) {
        if (trayIcon != null) {
            trayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO);
        }
    }

    private void exitApp() {
        if (systemTray != null && trayIcon != null) {
            systemTray.remove(trayIcon);
            trayIcon = null;
            systemTray = null;
        }
        Platform.exit();
        // Force exit in a separate thread to bypass AWT non-daemon threads
        new Thread(() -> System.exit(0)).start();
    }

    private String getButtonStyle(String bgColor, String textColor) {
        return "-fx-background-color: " + bgColor + "; " +
               "-fx-text-fill: " + textColor + "; " +
               "-fx-background-radius: 4; " +
               "-fx-padding: 8 16; " +
               "-fx-font-size: 14;";
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
