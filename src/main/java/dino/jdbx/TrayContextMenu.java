package dino.jdbx;

import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * 基于 JavaFX 的系统托盘右键菜单组件
 * <p>
 * 完美支持中文字符，绕过 AWT 原生菜单的字体渲染限制。
 * <p>
 * 使用示例：
 * <pre>
 * TrayContextMenu menu = TrayContextMenu.create(primaryStage)
 *     .item("显示窗口", () -> primaryStage.show())
 *     .item("最小化", this::minimizeToTray)
 *     .separator()
 *     .item("退出", this::exitApp);
 *
 * // 在右键点击时显示
 * menu.show(screenX, screenY);
 * </pre>
 *
 * @author jDbx
 * @version 1.0
 */
public class TrayContextMenu {

    /**
     * 菜单样式
     */
    public enum Style {
        /** 浅色样式（默认） */
        LIGHT,
        /** 深色样式 */
        DARK
    }

    private final Popup popup;
    private final VBox menuBox;
    private final Window ownerWindow;

    // 配置项
    private Font menuFont = Font.font("Microsoft YaHei", 13);
    private Style menuStyle = Style.LIGHT;
    private double menuMinWidth = 160;

    // 样式常量
    private static final String STYLE_LIGHT =
        "-fx-background-color: #ffffff;" +
        "-fx-border-color: #d0d0d0;" +
        "-fx-border-radius: 4;" +
        "-fx-background-radius: 4;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 8, 0, 0, 2);";

    private static final String STYLE_DARK =
        "-fx-background-color: #2d2d2d;" +
        "-fx-border-color: #404040;" +
        "-fx-border-radius: 4;" +
        "-fx-background-radius: 4;" +
        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 2);";

    private static final String ITEM_HOVER_LIGHT = "-fx-cursor: hand; -fx-background-color: #e0e0e0;";
    private static final String ITEM_HOVER_DARK = "-fx-cursor: hand; -fx-background-color: #404040;";
    private static final String ITEM_STYLE = "-fx-cursor: hand;";

    /**
     * 私有构造方法，使用 {@link #create(Window)} 创建实例
     */
    private TrayContextMenu(Window ownerWindow) {
        this.ownerWindow = ownerWindow;
        this.popup = new Popup();
        this.menuBox = new VBox();
        applyStyle();
        popup.getContent().add(menuBox);
        popup.setAutoHide(true);
    }

    /**
     * 创建 TrayContextMenu 实例
     *
     * @param ownerWindow 所有者窗口（用于定位和生命周期管理）
     * @return 新的 TrayContextMenu 实例
     */
    public static TrayContextMenu create(Window ownerWindow) {
        return new TrayContextMenu(ownerWindow);
    }

    /**
     * 添加菜单项
     *
     * @param text   菜单项文本（支持中文）
     * @param action 点击时执行的操作
     * @return this（支持链式调用）
     */
    public TrayContextMenu item(String text, Runnable action) {
        return item(text, action, false);
    }

    /**
     * 添加菜单项
     *
     * @param text     菜单项文本（支持中文）
     * @param action   点击时执行的操作
     * @param disabled 是否禁用
     * @return this（支持链式调用）
     */
    public TrayContextMenu item(String text, Runnable action, boolean disabled) {
        Label label = new Label(text);
        label.setFont(menuFont);
        label.setTextFill(disabled ? Color.GRAY : getItemTextColor());
        label.setPadding(new Insets(6, 20, 6, 20));
        label.setMaxWidth(Double.MAX_VALUE);

        HBox container = new HBox(label);
        HBox.setHgrow(label, Priority.ALWAYS);
        container.setStyle(ITEM_STYLE);
        container.setPadding(new Insets(0));

        if (!disabled) {
            // 鼠标悬停效果
            container.setOnMouseEntered(e ->
                container.setStyle(getItemHoverStyle())
            );
            container.setOnMouseExited(e ->
                container.setStyle(ITEM_STYLE)
            );

            // 点击事件
            container.setOnMouseClicked(e -> {
                hide();
                if (action != null) {
                    action.run();
                }
            });
        } else {
            container.setOpacity(0.5);
            container.setStyle("-fx-cursor: default;");
        }

        menuBox.getChildren().add(container);
        return this;
    }

    /**
     * 添加带图标的菜单项
     *
     * @param icon   图标
     * @param text   菜单项文本（支持中文）
     * @param action 点击时执行的操作
     * @return this（支持链式调用）
     */
    public TrayContextMenu item(Image icon, String text, Runnable action) {
        ImageView imageView = new ImageView(icon);
        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        Label label = new Label(text);
        label.setFont(menuFont);
        label.setTextFill(getItemTextColor());
        label.setPadding(new Insets(6, 20, 6, 10));

        HBox container = new HBox(8, imageView, label);
        container.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        container.setStyle(ITEM_STYLE);
        container.setPadding(new Insets(0, 20, 0, 10));

        // 鼠标悬停效果
        container.setOnMouseEntered(e ->
            container.setStyle(getItemHoverStyle())
        );
        container.setOnMouseExited(e ->
            container.setStyle(ITEM_STYLE)
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
    public TrayContextMenu separator() {
        Separator separator = new Separator();
        separator.setPadding(new Insets(4, 10, 4, 10));
        separator.setStyle("-fx-background-color: " + getSeparatorColor() + ";");
        menuBox.getChildren().add(separator);
        return this;
    }

    /**
     * 设置菜单字体
     *
     * @param font 字体
     * @return this（支持链式调用）
     */
    public TrayContextMenu font(Font font) {
        this.menuFont = font;
        return this;
    }

    /**
     * 设置菜单样式
     *
     * @param style 样式
     * @return this（支持链式调用）
     */
    public TrayContextMenu style(Style style) {
        this.menuStyle = style;
        applyStyle();
        return this;
    }

    /**
     * 设置菜单最小宽度
     *
     * @param width 最小宽度
     * @return this（支持链式调用）
     */
    public TrayContextMenu minWidth(double width) {
        this.menuMinWidth = width;
        menuBox.setMinWidth(width);
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
        double menuWidth = menuBox.getWidth() > 0 ? menuBox.getWidth() : menuMinWidth;
        double menuHeight = menuBox.getHeight() > 0 ? menuBox.getHeight() : 100;

        // 获取主屏幕边界
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getBounds();

        // 如果菜单超出右边界，向左偏移
        if (x + menuWidth > bounds.getMaxX()) {
            x = bounds.getMaxX() - menuWidth - 10;
        }

        // 如果菜单超出下边界，向上偏移
        if (y + menuHeight > bounds.getMaxY()) {
            y = bounds.getMaxY() - menuHeight - 10;
        }

        // 确保不超出左边界和上边界
        x = Math.max(bounds.getMinX() + 10, x);
        y = Math.max(bounds.getMinY() + 10, y);

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
     *
     * @return true 如果菜单正在显示
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

    /**
     * 获取当前菜单项数量
     *
     * @return 菜单项数量
     */
    public int getItemCount() {
        return menuBox.getChildren().size();
    }

    /**
     * 应用当前样式
     */
    private void applyStyle() {
        String style = menuStyle == Style.DARK ? STYLE_DARK : STYLE_LIGHT;
        menuBox.setStyle(style);
        menuBox.setPadding(new Insets(4, 0, 4, 0));
        menuBox.setMinWidth(menuMinWidth);
    }

    /**
     * 获取菜单项文本颜色
     */
    private Color getItemTextColor() {
        return menuStyle == Style.DARK ? Color.WHITE : Color.BLACK;
    }

    /**
     * 获取菜单项悬停样式
     */
    private String getItemHoverStyle() {
        return menuStyle == Style.DARK ? ITEM_HOVER_DARK : ITEM_HOVER_LIGHT;
    }

    /**
     * 获取分隔线颜色
     */
    private String getSeparatorColor() {
        return menuStyle == Style.DARK ? "#505050" : "#d0d0d0";
    }
}
