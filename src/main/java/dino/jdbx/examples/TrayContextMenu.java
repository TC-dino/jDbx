package dino.jdbx.examples;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * 基于 JavaFX 的系统托盘右键菜单
 * 完美支持中文字符，绕过 AWT 原生菜单的字体渲染限制
 *
 * 使用方式：
 * 1. 创建 TrayContextMenu 实例
 * 2. 使用 addItem() 添加菜单项
 * 3. 使用 addSeparator() 添加分隔线
 * 4. 在托盘图标的右键点击事件中调用 show()
 */
public class TrayContextMenu {

    private final Popup popup;
    private final VBox menuBox;
    private final Window ownerWindow;

    /**
     * 创建右键菜单
     *
     * @param ownerWindow 所有者窗口（用于定位和生命周期管理）
     */
    public TrayContextMenu(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
        this.popup = new Popup();
        this.menuBox = new VBox();

        // 设置菜单容器样式
        menuBox.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-border-color: #d0d0d0;" +
            "-fx-border-radius: 4;" +
            "-fx-background-radius: 4;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);"
        );
        menuBox.setPadding(new Insets(4, 0, 4, 0));
        menuBox.setMinWidth(160);

        popup.getContent().add(menuBox);
        popup.setAutoHide(true);
    }

    /**
     * 添加菜单项
     *
     * @param text   菜单项文本（支持中文）
     * @param action 点击时执行的操作
     * @return this（支持链式调用）
     */
    public TrayContextMenu addItem(String text, Runnable action) {
        Label label = new Label(text);
        label.setFont(Font.font("Microsoft YaHei", 13));
        label.setTextFill(Color.BLACK);
        label.setPadding(new Insets(6, 20, 6, 20));
        label.setMaxWidth(Double.MAX_VALUE);

        HBox container = new HBox(label);
        container.setStyle("-fx-cursor: hand;");

        // 鼠标悬停效果
        container.setOnMouseEntered(e ->
            container.setStyle("-fx-cursor: hand; -fx-background-color: #e0e0e0;")
        );
        container.setOnMouseExited(e ->
            container.setStyle("-fx-cursor: hand;")
        );

        // 点击事件
        container.setOnMouseClicked(e -> {
            hide();
            if (action != null) {
                action.run();
            }
        });

        menuBox.getChildren().add(container);
        return this;
    }

    /**
     * 添加分隔线
     *
     * @return this（支持链式调用）
     */
    public TrayContextMenu addSeparator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(4, 10, 4, 10));
        menuBox.getChildren().add(separator);
        return this;
    }

    /**
     * 在指定位置显示菜单
     *
     * @param x 屏幕 X 坐标
     * @param y 屏幕 Y 坐标
     */
    public void show(double x, double y) {
        if (menuBox.getChildren().isEmpty()) {
            return;
        }

        // 计算菜单位置，确保不超出屏幕边界
        double menuWidth = menuBox.getWidth() > 0 ? menuBox.getWidth() : 160;
        double menuHeight = menuBox.getHeight() > 0 ? menuBox.getHeight() : 100;

        // 获取主屏幕边界
        Screen screen = Screen.getPrimary();
        javafx.geometry.Rectangle2D bounds = screen.getBounds();

        // 如果菜单超出右边界，向左偏移
        if (x + menuWidth > bounds.getMaxX()) {
            x = bounds.getMaxX() - menuWidth - 10;
        }

        // 如果菜单超出下边界，向上偏移
        if (y + menuHeight > bounds.getMaxY()) {
            y = bounds.getMaxY() - menuHeight - 10;
        }

        popup.show(ownerWindow, x, y);
    }

    /**
     * 隐藏菜单
     */
    public void hide() {
        popup.hide();
    }

    /**
     * 菜单是否正在显示
     */
    public boolean isShowing() {
        return popup.isShowing();
    }

    /**
     * 清空所有菜单项
     */
    public void clear() {
        menuBox.getChildren().clear();
    }
}
