package dino.jdbx.examples;

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
 * System Tray Example - Demonstrates system tray icon, context menu, notifications, and minimize-to-tray
 */
public class SystemTrayExample extends Application {

    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        Label title = new Label("System Tray Example");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #333;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        HBox bottomBox = createBottomButtons();
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 600, 450);
        stage.setTitle("System Tray Example");
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

        Label descLabel = new Label("System Tray Features:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        descLabel.setStyle("-fx-text-fill: #333;");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(200);
        infoArea.setFont(Font.font("Arial", 14));
        infoArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");
        infoArea.setText(
            "1. Click 'Init Tray' to create a system tray icon\n" +
            "2. Click 'Minimize to Tray' to hide the window\n" +
            "3. Double-click the tray icon to restore the window\n" +
            "4. Right-click the tray icon for context menu\n" +
            "5. Closing the window minimizes to tray instead of exiting\n" +
            "\n" +
            "Note: Uses AWT SystemTray with English menu items\n" +
            "to avoid Unicode rendering issues on Windows."
        );

        box.getChildren().addAll(descLabel, infoArea);
        return box;
    }

    private HBox createBottomButtons() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));

        Button initBtn = new Button("Init Tray");
        initBtn.setStyle(getButtonStyle("#337ab7", "white"));
        initBtn.setOnAction(e -> initSystemTray());

        Button minimizeBtn = new Button("Minimize to Tray");
        minimizeBtn.setStyle(getButtonStyle("#f0ad4e", "white"));
        minimizeBtn.setOnAction(e -> minimizeToTray());

        Button notifyBtn = new Button("Send Notification");
        notifyBtn.setStyle(getButtonStyle("#5cb85c", "white"));
        notifyBtn.setOnAction(e -> showNotification("Hello!", "This is a notification from jDbx"));

        Button exitBtn = new Button("Exit");
        exitBtn.setStyle(getButtonStyle("#d9534f", "white"));
        exitBtn.setOnAction(e -> exitApp());

        box.getChildren().addAll(initBtn, minimizeBtn, notifyBtn, exitBtn);
        return box;
    }

    private void initSystemTray() {
        if (!SystemTray.isSupported()) {
            showAlert("Notice", "System tray is not supported on this platform");
            return;
        }

        if (systemTray != null && trayIcon != null) {
            showAlert("Notice", "Tray already initialized");
            return;
        }

        systemTray = SystemTray.getSystemTray();

        trayIcon = new TrayIcon(createDefaultImage());
        trayIcon.setImageAutoSize(true);
        trayIcon.setToolTip("jDbx System Tray");

        PopupMenu popup = createContextMenu();
        trayIcon.setPopupMenu(popup);

        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Platform.runLater(() -> {
                        primaryStage.show();
                        primaryStage.toFront();
                    });
                }
            }
        });

        try {
            systemTray.add(trayIcon);
            showNotification("jDbx", "System tray initialized successfully");
        } catch (AWTException e) {
            showAlert("Error", "Failed to add tray icon: " + e.getMessage());
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

    private PopupMenu createContextMenu() {
        PopupMenu popup = new PopupMenu();

        MenuItem showItem = new MenuItem("Show Window");
        showItem.addActionListener(e -> Platform.runLater(() -> {
            primaryStage.show();
            primaryStage.toFront();
        }));

        MenuItem hideItem = new MenuItem("Hide to Tray");
        hideItem.addActionListener(e -> Platform.runLater(this::minimizeToTray));

        MenuItem notifyItem = new MenuItem("Send Notification");
        notifyItem.addActionListener(e -> showNotification("Test", "Notification from context menu"));

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> Platform.runLater(this::exitApp));

        popup.add(showItem);
        popup.add(hideItem);
        popup.add(notifyItem);
        popup.addSeparator();
        popup.add(exitItem);

        return popup;
    }

    private void minimizeToTray() {
        if (systemTray == null || trayIcon == null) {
            showAlert("Notice", "Please initialize the tray first");
            return;
        }
        Platform.runLater(() -> {
            primaryStage.hide();
            showNotification("jDbx", "Window minimized to system tray");
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
