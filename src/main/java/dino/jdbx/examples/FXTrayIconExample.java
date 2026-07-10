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

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * FXTrayIcon Example - Comprehensive demo of FXTrayIcon features
 *
 * Features demonstrated:
 * - Basic tray icon creation with Builder pattern
 * - JavaFX MenuItem in context menu (full Unicode/Chinese support)
 * - CheckMenuItem for toggle options
 * - Sub-menus
 * - Separator lines
 * - Multiple notification types (info, warn, error)
 * - Custom exit handler
 * - Tooltip
 * - Dynamic menu item updates
 * - Left-click action (onAction)
 */
public class FXTrayIconExample extends Application {

    private FXTrayIcon trayIcon;
    private Stage primaryStage;
    private boolean darkMode = false;
    private boolean notificationsEnabled = true;
    private Timer statusTimer;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        BorderPane root = new BorderPane();
        root.setPadding(new javafx.geometry.Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        javafx.scene.control.Label title = new javafx.scene.control.Label("FXTrayIcon Features Demo");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #333;");
        BorderPane.setAlignment(title, javafx.geometry.Pos.CENTER);
        root.setTop(title);

        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        HBox bottomBox = createBottomButtons();
        BorderPane.setAlignment(bottomBox, javafx.geometry.Pos.CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 650, 500);
        stage.setTitle("FXTrayIcon Features Demo");
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

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label("FXTrayIcon Features:");
        descLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        descLabel.setStyle("-fx-text-fill: #333;");

        javafx.scene.control.TextArea infoArea = new javafx.scene.control.TextArea();
        infoArea.setEditable(false);
        infoArea.setWrapText(true);
        infoArea.setPrefHeight(280);
        infoArea.setFont(Font.font("Consolas", 13));
        infoArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");
        infoArea.setText(
            "Context Menu Features:\n" +
            "  - Show Window / Hide to Tray\n" +
            "  - Notification types: Default, Info, Warn, Error\n" +
            "  - CheckMenuItems: Dark Mode, Notifications toggle\n" +
            "  - Sub-menu: Settings (with nested items)\n" +
            "  - Separator lines\n" +
            "  - Custom Exit with confirmation\n\n" +
            "Interaction:\n" +
            "  - Left-click tray icon: Show/Hide window\n" +
            "  - Right-click tray icon: Context menu\n" +
            "  - Close window: Minimize to tray\n\n" +
            "Other Features:\n" +
            "  - Dynamic tooltip\n" +
            "  - Periodic status updates\n" +
            "  - Auto-generated icon (blue circle with 'J')"
        );

        box.getChildren().addAll(descLabel, infoArea);
        return box;
    }

    private HBox createBottomButtons() {
        HBox box = new HBox(10);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        javafx.scene.control.Button initBtn = new javafx.scene.control.Button("Init Tray");
        initBtn.setStyle(getButtonStyle("#337ab7", "white"));
        initBtn.setOnAction(e -> initTrayIcon());

        javafx.scene.control.Button minimizeBtn = new javafx.scene.control.Button("Minimize");
        minimizeBtn.setStyle(getButtonStyle("#f0ad4e", "white"));
        minimizeBtn.setOnAction(e -> minimizeToTray());

        javafx.scene.control.Button notifyBtn = new javafx.scene.control.Button("Notify");
        notifyBtn.setStyle(getButtonStyle("#5cb85c", "white"));
        notifyBtn.setOnAction(e -> sendNotification("default"));

        javafx.scene.control.Button statusBtn = new javafx.scene.control.Button("Toggle Status Timer");
        statusBtn.setStyle(getButtonStyle("#9b59b6", "white"));
        statusBtn.setOnAction(e -> toggleStatusTimer());

        javafx.scene.control.Button exitBtn = new javafx.scene.control.Button("Exit");
        exitBtn.setStyle(getButtonStyle("#d9534f", "white"));
        exitBtn.setOnAction(e -> exitApp());

        box.getChildren().addAll(initBtn, minimizeBtn, notifyBtn, statusBtn, exitBtn);
        return box;
    }

    private void initTrayIcon() {
        if (trayIcon != null && trayIcon.isShowing()) {
            showAlert("Notice", "Tray already initialized");
            return;
        }

        BufferedImage trayImage = createDefaultImage();

        // Build context menu with full JavaFX MenuItem support
        trayIcon = new FXTrayIcon.Builder(primaryStage, trayImage)
            // Basic window control
            .menuItem("Show Window", e -> Platform.runLater(() -> {
                primaryStage.show();
                primaryStage.toFront();
            }))
            .menuItem("Hide to Tray", e -> Platform.runLater(this::minimizeToTray))
            .separator()

            // Notification sub-menu
            .menu("Notifications", createNotificationMenuItems())
            .separator()

            // CheckMenuItems for toggle options
            .checkMenuItem("Dark Mode", e -> {
                darkMode = !darkMode;
                updateTheme();
                if (notificationsEnabled) {
                    trayIcon.showMessage("Theme", "Dark mode: " + (darkMode ? "ON" : "OFF"));
                }
            })
            .checkMenuItem("Enable Notifications", e -> {
                notificationsEnabled = !notificationsEnabled;
                if (notificationsEnabled) {
                    trayIcon.showMessage("Notifications", "Notifications enabled");
                }
            })
            .separator()

            // Settings sub-menu
            .menu("Settings", createSettingsMenuItems())
            .separator()

            // Custom exit with confirmation
            .addExitMenuItem("Exit", e -> {
                if (notificationsEnabled) {
                    trayIcon.showMessage("Goodbye", "Application is closing...");
                }
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                Platform.exit();
                System.exit(0);
            })
            .show()
            .build();

        // Set tooltip
        trayIcon.setTooltip("jDbx FXTrayIcon Demo");

        showAlert("Notice", "Tray icon initialized!\nLeft-click: Show/Hide\nRight-click: Context menu");
    }

    private Menu createNotificationMenuItems() {
        Menu notifMenu = new Menu("Notification Types");

        MenuItem defaultItem = new MenuItem("Default Notification");
        defaultItem.setOnAction(e -> sendNotification("default"));

        MenuItem infoItem = new MenuItem("Info Notification");
        infoItem.setOnAction(e -> sendNotification("info"));

        MenuItem warnItem = new MenuItem("Warning Notification");
        warnItem.setOnAction(e -> sendNotification("warn"));

        MenuItem errorItem = new MenuItem("Error Notification");
        errorItem.setOnAction(e -> sendNotification("error"));

        notifMenu.getItems().addAll(defaultItem, infoItem, warnItem, errorItem);
        return notifMenu;
    }

    private Menu createSettingsMenuItems() {
        Menu settingsMenu = new Menu("Settings");

        MenuItem langItem = new MenuItem("Language: English");
        langItem.setOnAction(e -> {
            langItem.setText("Language: " + (langItem.getText().contains("English") ? "Chinese" : "English"));
            if (notificationsEnabled) {
                trayIcon.showMessage("Language", langItem.getText());
            }
        });

        MenuItem themeItem = new MenuItem("Theme: Light");
        themeItem.setOnAction(e -> {
            darkMode = !darkMode;
            themeItem.setText("Theme: " + (darkMode ? "Dark" : "Light"));
            updateTheme();
        });

        MenuItem resetItem = new MenuItem("Reset to Defaults");
        resetItem.setOnAction(e -> {
            darkMode = false;
            notificationsEnabled = true;
            updateTheme();
            if (notificationsEnabled) {
                trayIcon.showMessage("Settings", "Reset to defaults");
            }
        });

        settingsMenu.getItems().addAll(langItem, themeItem, new MenuItem("-"), resetItem);
        return settingsMenu;
    }

    private void sendNotification(String type) {
        if (trayIcon == null || !notificationsEnabled) return;

        switch (type) {
            case "info":
                trayIcon.showInfoMessage("Info", "This is an informational message");
                break;
            case "warn":
                trayIcon.showMessage("Warning", "This is a warning message");
                break;
            case "error":
                trayIcon.showErrorMessage("Error", "This is an error message");
                break;
            default:
                trayIcon.showMessage("Notification", "This is a default notification");
                break;
        }
    }

    private void updateTheme() {
        Platform.runLater(() -> {
            Scene scene = primaryStage.getScene();
            if (scene != null) {
                String bg = darkMode ? "#2c2c2c" : "#f5f5f5";
                String text = darkMode ? "#ffffff" : "#333333";
                scene.getRoot().setStyle("-fx-background-color: " + bg + ";");
            }
        });
    }

    private void toggleStatusTimer() {
        if (statusTimer != null) {
            statusTimer.cancel();
            statusTimer = null;
            showAlert("Timer", "Status timer stopped");
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
                        trayIcon.setTooltip("jDbx | Running: " + count + "s")
                    );
                }
            }
        }, 1000, 1000);

        showAlert("Timer", "Status timer started (updates tooltip every second)");
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
            showAlert("Notice", "Please initialize the tray first");
            return;
        }
        Platform.runLater(() -> {
            primaryStage.hide();
            if (notificationsEnabled) {
                trayIcon.showMessage("jDbx", "Window minimized to system tray");
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
