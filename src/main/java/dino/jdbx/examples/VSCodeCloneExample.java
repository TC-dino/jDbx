package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * VS Code 界面克隆 - 完全还原 HTML 原型（含全部交互）
 */
public class VSCodeCloneExample extends Application {

    // VS Code 颜色定义
    private static final String TITLEBAR_BG = "#323233";
    private static final String ACTIVITYBAR_BG = "#333333";
    private static final String SIDEBAR_BG = "#252526";
    private static final String EDITOR_BG = "#1e1e1e";
    private static final String EDITOR_TAB_ACTIVE = "#1e1e1e";
    private static final String EDITOR_TAB_INACTIVE = "#2d2d2d";
    private static final String PANEL_BG = "#1e1e1e";
    private static final String PANEL_HEADER_BG = "#252526";
    private static final String STATUSBAR_BG = "#007acc";
    private static final String ACCENT = "#007acc";
    private static final String HOVER_BG = "#2a2d2e";
    private static final String SELECTED_BG = "#094771";
    private static final String BORDER = "#474747";

    // 状态变量
    private VBox sidebar;
    private VBox sidebarContent;
    private Label sidebarHeader;
    private VBox panelContainer;
    private boolean sidebarCollapsed = false;
    private boolean panelCollapsed = false;
    private String activeView = "explorer";
    private String activePanelTab = "terminal";
    private String activeEditorTab = "UserProfile";
    private StackPane[] activityBarItems = new StackPane[5];
    private StackPane[] panelTabs = new StackPane[4];
    private StackPane[] editorTabs = new StackPane[3];
    private VBox[] sidebarViews = new VBox[5];
    private VBox[] panelViews = new VBox[4];
    private StackPane editorPane;
    private VBox codeEditorView;
    private VBox welcomeView;
    private HBox breadcrumbs;
    private Label titlebarTitle;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + EDITOR_BG + ";");

        // 顶部标题栏
        root.setTop(createTitleBar());

        // 底部状态栏
        root.setBottom(createStatusBar());

        // 主内容区
        HBox mainContainer = new HBox();
        mainContainer.setStyle("-fx-background-color: " + EDITOR_BG + ";");

        // 活动栏
        mainContainer.getChildren().add(createActivityBar());

        // 侧边栏
        sidebar = createSidebar();
        mainContainer.getChildren().add(sidebar);

        // 编辑器 + 面板
        VBox editorPanelWrapper = new VBox();
        editorPanelWrapper.setStyle("-fx-background-color: " + EDITOR_BG + ";");
        HBox.setHgrow(editorPanelWrapper, Priority.ALWAYS);

        // 编辑器区域
        editorPane = createEditorArea();
        VBox.setVgrow(editorPane, Priority.ALWAYS);
        editorPanelWrapper.getChildren().add(editorPane);

        // 面板分隔条（可拖拽）
        StackPane panelResize = createResizeHandle(true);
        editorPanelWrapper.getChildren().add(panelResize);

        // 底部面板
        panelContainer = createPanelContainer();
        editorPanelWrapper.getChildren().add(panelContainer);

        mainContainer.getChildren().add(editorPanelWrapper);
        root.setCenter(mainContainer);

        Scene scene = new Scene(root, 1400, 900);

        // 键盘快捷键
        scene.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                if (e.getCode() == KeyCode.B) {
                    toggleSidebar();
                    e.consume();
                } else if (e.getCode() == KeyCode.BACK_QUOTE) {
                    togglePanel();
                    e.consume();
                } else if (e.isShiftDown()) {
                    switch (e.getCode()) {
                        case E -> { switchSidebarView("explorer"); e.consume(); }
                        case F -> { switchSidebarView("search"); e.consume(); }
                        case G -> { switchSidebarView("scm"); e.consume(); }
                        case D -> { switchSidebarView("debug"); e.consume(); }
                        case X -> { switchSidebarView("extensions"); e.consume(); }
                    }
                }
            }
        });

        stage.setTitle("Visual Studio Code");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 创建可拖拽的分隔条
     */
    private StackPane createResizeHandle(boolean isHorizontal) {
        Region handle = new Region();
        handle.setPrefSize(isHorizontal ? Double.MAX_VALUE : 4, isHorizontal ? 4 : Double.MAX_VALUE);
        handle.setStyle("-fx-background-color: transparent; -fx-cursor: " + (isHorizontal ? "row-resize" : "col-resize") + ";");

        StackPane container = new StackPane(handle);
        container.setStyle("-fx-background-color: transparent;");

        final double[] startPos = new double[1];
        final double[] startSize = new double[1];

        handle.setOnMousePressed(e -> {
            if (isHorizontal) {
                startPos[0] = e.getScreenY();
                startSize[0] = panelContainer.getHeight();
            } else {
                startPos[0] = e.getScreenX();
                startSize[0] = sidebar.getWidth();
            }
            handle.setStyle("-fx-background-color: " + ACCENT + "; -fx-opacity: 0.5; -fx-cursor: " + (isHorizontal ? "row-resize" : "col-resize") + ";");
        });

        handle.setOnMouseDragged(e -> {
            if (isHorizontal) {
                double delta = e.getScreenY() - startPos[0];
                double newHeight = Math.max(80, Math.min(500, startSize[0] - delta));
                panelContainer.setPrefHeight(newHeight);
                panelContainer.setMinHeight(80);
                if (panelCollapsed) {
                    panelCollapsed = false;
                    panelContainer.setVisible(true);
                    panelContainer.setManaged(true);
                }
            } else {
                double delta = e.getScreenX() - startPos[0];
                double newWidth = Math.max(170, Math.min(600, startSize[0] + delta));
                sidebar.setPrefWidth(newWidth);
                if (sidebarCollapsed) {
                    sidebarCollapsed = false;
                    sidebar.setVisible(true);
                    sidebar.setManaged(true);
                }
            }
        });

        handle.setOnMouseReleased(e -> {
            handle.setStyle("-fx-background-color: transparent; -fx-cursor: " + (isHorizontal ? "row-resize" : "col-resize") + ";");
        });

        return container;
    }

    // ==================== 切换功能 ====================

    private void toggleSidebar() {
        sidebarCollapsed = !sidebarCollapsed;
        sidebar.setVisible(!sidebarCollapsed);
        sidebar.setManaged(!sidebarCollapsed);
    }

    private void togglePanel() {
        panelCollapsed = !panelCollapsed;
        panelContainer.setVisible(!panelCollapsed);
        panelContainer.setManaged(!panelCollapsed);
    }

    private void switchSidebarView(String view) {
        // 如果点击当前活跃的视图，则切换侧边栏显示/隐藏
        if (view.equals(activeView) && !sidebarCollapsed) {
            toggleSidebar();
            return;
        }

        activeView = view;
        sidebarCollapsed = false;
        sidebar.setVisible(true);
        sidebar.setManaged(true);

        // 更新活动栏高亮
        for (int i = 0; i < activityBarItems.length; i++) {
            StackPane item = activityBarItems[i];
            if (item == null) continue;
            String itemView = (String) item.getUserData();
            boolean isActive = view.equals(itemView);
            item.setStyle("-fx-background-color: " + ACTIVITYBAR_BG + ";");

            // 更新图标颜色
            if (item.getChildren().get(0) instanceof Label icon) {
                icon.setTextFill(isActive ? Color.WHITE : Color.web("#858585"));
            }

            // 更新指示条
            boolean hasIndicator = item.getChildren().stream()
                    .anyMatch(n -> n instanceof Region && ((Region) n).getPrefWidth() == 2);
            if (isActive && !hasIndicator) {
                Region indicator = new Region();
                indicator.setPrefSize(2, 36);
                indicator.setStyle("-fx-background-color: white;");
                StackPane.setAlignment(indicator, Pos.CENTER_LEFT);
                item.getChildren().add(indicator);
            } else if (!isActive) {
                item.getChildren().removeIf(n -> n instanceof Region && ((Region) n).getPrefWidth() == 2);
            }
        }

        // 更新侧边栏头部
        String[] headers = {"EXPLORER", "SEARCH", "SOURCE CONTROL", "RUN AND DEBUG", "EXTENSIONS"};
        int viewIndex = switch (view) {
            case "explorer" -> 0;
            case "search" -> 1;
            case "scm" -> 2;
            case "debug" -> 3;
            case "extensions" -> 4;
            default -> 0;
        };
        sidebarHeader.setText(headers[viewIndex]);

        // 切换侧边栏视图
        for (int i = 0; i < sidebarViews.length; i++) {
            if (sidebarViews[i] != null) {
                sidebarViews[i].setVisible(i == viewIndex);
                sidebarViews[i].setManaged(i == viewIndex);
            }
        }
    }

    private void switchPanelTab(String tab) {
        activePanelTab = tab;

        // 更新标签高亮
        for (int i = 0; i < panelTabs.length; i++) {
            StackPane tabPane = panelTabs[i];
            if (tabPane == null) continue;
            String tabName = (String) tabPane.getUserData();
            boolean isActive = tab.equals(tabName);

            // 更新文字颜色和底部边框
            tabPane.getChildren().removeIf(n -> n instanceof Region && ((Region) n).getPrefHeight() == 1);
            if (tabPane.getChildren().get(0) instanceof HBox hbox) {
                if (hbox.getChildren().get(0) instanceof Label label) {
                    label.setTextFill(isActive ? Color.web("#cccccc") : Color.web("#969696"));
                }
            }
            if (isActive) {
                Region bottomBorder = new Region();
                bottomBorder.setPrefHeight(1);
                bottomBorder.setStyle("-fx-background-color: " + ACCENT + ";");
                StackPane.setAlignment(bottomBorder, Pos.BOTTOM_CENTER);
                tabPane.getChildren().add(bottomBorder);
            }
        }

        // 切换面板视图
        for (int i = 0; i < panelViews.length; i++) {
            if (panelViews[i] != null) {
                panelViews[i].setVisible(i == switch (tab) {
                    case "problems" -> 0;
                    case "output" -> 1;
                    case "debug-console" -> 2;
                    case "terminal" -> 3;
                    default -> 3;
                });
            }
        }

        // 确保面板可见
        if (panelCollapsed) {
            panelCollapsed = false;
            panelContainer.setVisible(true);
            panelContainer.setManaged(true);
        }
    }

    private void switchEditorTab(String tab) {
        activeEditorTab = tab;

        // 更新标签高亮
        for (int i = 0; i < editorTabs.length; i++) {
            StackPane tabPane = editorTabs[i];
            if (tabPane == null) continue;
            String tabName = (String) tabPane.getUserData();
            boolean isActive = tab.equals(tabName);

            // 更新背景和顶部边框
            if (tabPane.getChildren().get(0) instanceof HBox hbox) {
                hbox.setStyle("-fx-background-color: " + (isActive ? EDITOR_TAB_ACTIVE : EDITOR_TAB_INACTIVE) + ";");
                if (hbox.getChildren().size() > 1 && hbox.getChildren().get(1) instanceof Label label) {
                    label.setTextFill(isActive ? Color.WHITE : Color.web("#969696"));
                }
            }

            // 更新顶部指示条
            tabPane.getChildren().removeIf(n -> n instanceof Region && ((Region) n).getPrefHeight() == 1 && n.getBoundsInParent().getMinY() == 0);
            if (isActive) {
                Region topBorder = new Region();
                topBorder.setPrefHeight(1);
                topBorder.setStyle("-fx-background-color: " + ACCENT + ";");
                StackPane.setAlignment(topBorder, Pos.TOP_CENTER);
                tabPane.getChildren().add(topBorder);
            }
        }

        // 切换编辑器视图
        if ("Welcome".equals(tab)) {
            codeEditorView.setVisible(false);
            codeEditorView.setManaged(false);
            welcomeView.setVisible(true);
            welcomeView.setManaged(true);
            breadcrumbs.setVisible(false);
            breadcrumbs.setManaged(false);
            titlebarTitle.setText("Welcome - my-project - Visual Studio Code");
        } else {
            codeEditorView.setVisible(true);
            codeEditorView.setManaged(true);
            welcomeView.setVisible(false);
            welcomeView.setManaged(false);
            breadcrumbs.setVisible(true);
            breadcrumbs.setManaged(true);
            titlebarTitle.setText(tab + " - my-project - Visual Studio Code");
        }
    }

    // ==================== 标题栏 ====================

    private HBox createTitleBar() {
        HBox titlebar = new HBox();
        titlebar.setStyle("-fx-background-color: " + TITLEBAR_BG + "; -fx-border-color: #1a1a1a transparent transparent transparent; -fx-border-width: 0 0 1 0;");
        titlebar.setPrefHeight(30);
        titlebar.setAlignment(Pos.CENTER_LEFT);

        // 菜单项（带下拉）
        String[] menuNames = {"File", "Edit", "Selection", "View", "Go", "Run", "Terminal", "Help"};
        for (String menu : menuNames) {
            Label menuItem = new Label(menu);
            menuItem.setTextFill(Color.web("#cccccc"));
            menuItem.setFont(Font.font("Segoe UI", 12));
            menuItem.setPadding(new Insets(0, 8, 0, 8));
            menuItem.setStyle("-fx-cursor: hand;");

            menuItem.setOnMouseEntered(e -> {
                if (!menuItem.getStyle().contains("background-color")) {
                    menuItem.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.1);");
                }
            });
            menuItem.setOnMouseExited(e -> {
                menuItem.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
            });

            // 点击显示菜单
            if ("File".equals(menu) || "View".equals(menu)) {
                menuItem.setOnMouseClicked(e -> {
                    showTitleMenu(menuItem, menu);
                });
            }

            titlebar.getChildren().add(menuItem);
        }

        // 标题
        titlebarTitle = new Label("UserProfile.tsx - my-project - Visual Studio Code");
        titlebarTitle.setTextFill(Color.web("#cccccc"));
        titlebarTitle.setFont(Font.font("Segoe UI", 12));
        titlebarTitle.setPadding(new Insets(0, 20, 0, 20));
        HBox.setHgrow(titlebarTitle, Priority.ALWAYS);
        titlebarTitle.setAlignment(Pos.CENTER);
        titlebarTitle.setMaxWidth(Double.MAX_VALUE);

        // 窗口控制按钮
        HBox controls = new HBox();
        controls.setAlignment(Pos.CENTER);
        controls.getChildren().addAll(
                createWindowButton("—", false),
                createWindowButton("☐", false),
                createWindowButton("✕", true)
        );

        titlebar.getChildren().addAll(titlebarTitle, controls);
        return titlebar;
    }

    private void showTitleMenu(Label anchor, String menuName) {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.setStyle("-fx-background-color: #252526; -fx-border-color: #454545; -fx-border-radius: 4; -fx-background-radius: 4;");

        if ("File".equals(menuName)) {
            addMenuItem(contextMenu, "New File", "Ctrl+N");
            addMenuItem(contextMenu, "New Window", "Ctrl+Shift+N");
            contextMenu.getItems().add(new SeparatorMenuItem());
            addMenuItem(contextMenu, "Open File...", "Ctrl+O");
            addMenuItem(contextMenu, "Open Folder...", "Ctrl+K Ctrl+O");
            contextMenu.getItems().add(new SeparatorMenuItem());
            addMenuItem(contextMenu, "Save", "Ctrl+S");
            addMenuItem(contextMenu, "Save As...", "Ctrl+Shift+S");
            addMenuItem(contextMenu, "Save All", "Ctrl+K S");
            contextMenu.getItems().add(new SeparatorMenuItem());
            addMenuItem(contextMenu, "Auto Save", "");
            contextMenu.getItems().add(new SeparatorMenuItem());
            addMenuItem(contextMenu, "Exit", "");
        } else if ("View".equals(menuName)) {
            addMenuItem(contextMenu, "Explorer", "Ctrl+Shift+E", () -> switchSidebarView("explorer"));
            addMenuItem(contextMenu, "Search", "Ctrl+Shift+F", () -> switchSidebarView("search"));
            addMenuItem(contextMenu, "Source Control", "Ctrl+Shift+G", () -> switchSidebarView("scm"));
            addMenuItem(contextMenu, "Run", "Ctrl+Shift+D", () -> switchSidebarView("debug"));
            addMenuItem(contextMenu, "Extensions", "Ctrl+Shift+X", () -> switchSidebarView("extensions"));
            contextMenu.getItems().add(new SeparatorMenuItem());
            addMenuItem(contextMenu, "Toggle Sidebar", "Ctrl+B", this::toggleSidebar);
            addMenuItem(contextMenu, "Toggle Panel", "Ctrl+`", this::togglePanel);
        }

        contextMenu.show(anchor, anchor.localToScreen(0, anchor.getHeight()).getX(), anchor.localToScreen(0, anchor.getHeight()).getY());
    }

    private void addMenuItem(ContextMenu menu, String text, String shortcut) {
        addMenuItem(menu, text, shortcut, null);
    }

    private void addMenuItem(ContextMenu menu, String text, String shortcut, Runnable action) {
        Label item = new Label(text);
        item.setTextFill(Color.web("#cccccc"));
        item.setFont(Font.font("Segoe UI", 13));
        item.setPadding(new Insets(4, 24, 4, 12));

        if (!shortcut.isEmpty()) {
            Label key = new Label(shortcut);
            key.setTextFill(Color.web("#969696"));
            key.setFont(Font.font("Segoe UI", 12));
            key.setPadding(new Insets(0, 0, 0, 24));

            HBox container = new HBox(item, key);
            container.setAlignment(Pos.CENTER_LEFT);
            container.setPrefWidth(250);

            CustomMenuItem menuItem = new CustomMenuItem(container);
            menuItem.setHideOnClick(true);
            if (action != null) {
                container.setOnMouseClicked(e -> action.run());
            }
            menu.getItems().add(menuItem);
        } else {
            CustomMenuItem menuItem = new CustomMenuItem(item);
            menuItem.setHideOnClick(true);
            if (action != null) {
                item.setOnMouseClicked(e -> action.run());
            }
            menu.getItems().add(menuItem);
        }
    }

    private StackPane createWindowButton(String text, boolean isClose) {
        Label btn = new Label(text);
        btn.setTextFill(Color.web("#cccccc"));
        btn.setFont(Font.font("Segoe UI", 10));
        btn.setPrefSize(46, 30);
        btn.setAlignment(Pos.CENTER);
        if (isClose) {
            btn.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-cursor: hand; -fx-background-color: #e81123; -fx-text-fill: white;"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-text-fill: #cccccc;"));
        } else {
            btn.setStyle("-fx-cursor: hand; -fx-background-color: transparent;");
            btn.setOnMouseEntered(e -> btn.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.1);"));
            btn.setOnMouseExited(e -> btn.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));
        }
        return new StackPane(btn);
    }

    // ==================== 活动栏 ====================

    private VBox createActivityBar() {
        VBox activitybar = new VBox();
        activitybar.setStyle("-fx-background-color: " + ACTIVITYBAR_BG + "; -fx-border-color: transparent #1a1a1a transparent transparent; -fx-border-width: 0 1 0 0;");
        activitybar.setPrefWidth(48);
        activitybar.setAlignment(Pos.TOP_CENTER);

        VBox topItems = new VBox();
        topItems.setAlignment(Pos.TOP_CENTER);

        String[][] items = {
                {"\uD83D\uDCC1", "Explorer", "Ctrl+Shift+E", "explorer"},
                {"\uD83D\uDD0D", "Search", "Ctrl+Shift+F", "search"},
                {"\uD83D\uDD00", "Source Control", "Ctrl+Shift+G", "scm"},
                {"\uD83D\uDC1B", "Debug", "Ctrl+Shift+D", "debug"},
                {"\uD83D\uDCE6", "Extensions", "Ctrl+Shift+X", "extensions"}
        };

        boolean[] activeStates = {true, false, false, false, false};
        int[] badges = {0, 0, 3, 0, 0};

        for (int i = 0; i < items.length; i++) {
            activityBarItems[i] = createActivityBarItem(items[i][0], items[i][1], items[i][2], items[i][3], activeStates[i], badges[i]);
            topItems.getChildren().add(activityBarItems[i]);
        }

        VBox bottomItems = new VBox();
        bottomItems.setAlignment(Pos.BOTTOM_CENTER);
        bottomItems.getChildren().addAll(
                createActivityBarItem("\uD83D\uDC64", "Accounts", "", "", false, 0),
                createActivityBarItem("\u2699", "Manage", "Ctrl+,", "", false, 0)
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        activitybar.getChildren().addAll(topItems, spacer, bottomItems);
        return activitybar;
    }

    private StackPane createActivityBarItem(String icon, String tooltip, String shortcut, String view, boolean active, int badge) {
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(20));
        iconLabel.setTextFill(active ? Color.WHITE : Color.web("#858585"));
        iconLabel.setPrefSize(48, 48);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setStyle("-fx-cursor: hand;");

        StackPane container = new StackPane(iconLabel);
        container.setPrefSize(48, 48);
        container.setUserData(view);

        if (active) {
            container.setStyle("-fx-background-color: " + ACTIVITYBAR_BG + ";");
            Region indicator = new Region();
            indicator.setPrefSize(2, 36);
            indicator.setStyle("-fx-background-color: white;");
            StackPane.setAlignment(indicator, Pos.CENTER_LEFT);
            container.getChildren().add(indicator);
        }

        if (badge > 0) {
            Label badgeLabel = new Label(String.valueOf(badge));
            badgeLabel.setTextFill(Color.WHITE);
            badgeLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 9));
            badgeLabel.setStyle("-fx-background-color: " + ACCENT + "; -fx-padding: 0 4; -fx-background-radius: 8;");
            StackPane.setAlignment(badgeLabel, Pos.TOP_RIGHT);
            StackPane.setMargin(badgeLabel, new Insets(8, 6, 0, 0));
            container.getChildren().add(badgeLabel);
        }

        // 点击事件
        if (!view.isEmpty()) {
            container.setOnMouseClicked(e -> switchSidebarView(view));
        }

        // 悬停效果
        container.setOnMouseEntered(e -> {
            if (!view.equals(activeView)) {
                iconLabel.setTextFill(Color.WHITE);
            }
        });
        container.setOnMouseExited(e -> {
            if (!view.equals(activeView)) {
                iconLabel.setTextFill(Color.web("#858585"));
            }
        });

        return container;
    }

    // ==================== 侧边栏 ====================

    private VBox createSidebar() {
        sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");
        sidebar.setPrefWidth(260);
        sidebar.setMinWidth(170);
        sidebar.setMaxWidth(600);

        // 侧边栏头部
        sidebarHeader = new Label("EXPLORER");
        sidebarHeader.setTextFill(Color.web("#6e6e6e"));
        sidebarHeader.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        sidebarHeader.setPadding(new Insets(0, 20, 0, 20));
        sidebarHeader.setPrefHeight(35);
        sidebarHeader.setAlignment(Pos.CENTER_LEFT);
        sidebarHeader.setStyle("-fx-letter-spacing: 0.5;");

        // 侧边栏内容
        sidebarContent = new VBox();
        sidebarContent.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");

        // 创建5个视图
        sidebarViews[0] = createExplorerView();
        sidebarViews[1] = createSearchView();
        sidebarViews[2] = createSCMView();
        sidebarViews[3] = createDebugView();
        sidebarViews[4] = createExtensionsView();

        for (int i = 0; i < sidebarViews.length; i++) {
            sidebarViews[i].setVisible(i == 0);
            sidebarViews[i].setManaged(i == 0);
            sidebarContent.getChildren().add(sidebarViews[i]);
        }

        // 侧边栏拖拽调整大小
        StackPane sidebarResize = createResizeHandle(false);

        sidebar.getChildren().addAll(sidebarHeader, sidebarContent);

        // 添加调整大小手柄
        Region resizeHandle = new Region();
        resizeHandle.setPrefSize(6, Double.MAX_VALUE);
        resizeHandle.setStyle("-fx-cursor: col-resize; -fx-background-color: transparent;");
        StackPane.setAlignment(resizeHandle, Pos.CENTER_RIGHT);

        StackPane sidebarWrapper = new StackPane(sidebar, resizeHandle);
        sidebarWrapper.setPrefWidth(260);

        // 拖拽调整
        final double[] startDragX = new double[1];
        final double[] startWidth = new double[1];

        resizeHandle.setOnMousePressed(e -> {
            startDragX[0] = e.getScreenX();
            startWidth[0] = sidebar.getPrefWidth();
            resizeHandle.setStyle("-fx-cursor: col-resize; -fx-background-color: " + ACCENT + "; -fx-opacity: 0.5;");
        });

        resizeHandle.setOnMouseDragged(e -> {
            double delta = e.getScreenX() - startDragX[0];
            double newWidth = Math.max(170, Math.min(600, startWidth[0] + delta));
            sidebar.setPrefWidth(newWidth);
            sidebarWrapper.setPrefWidth(newWidth);
            if (sidebarCollapsed) {
                sidebarCollapsed = false;
                sidebar.setVisible(true);
                sidebar.setManaged(true);
            }
        });

        resizeHandle.setOnMouseReleased(e -> {
            resizeHandle.setStyle("-fx-cursor: col-resize; -fx-background-color: transparent;");
        });

        return sidebar;
    }

    private VBox createExplorerView() {
        VBox view = new VBox();
        view.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: " + SIDEBAR_BG + "; -fx-border-color: transparent;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        VBox content = new VBox();

        // Open Editors
        content.getChildren().add(createSectionHeader("Open Editors"));
        VBox openEditors = createOpenEditorsSection();
        content.getChildren().add(openEditors);

        // MY-PROJECT
        content.getChildren().add(createSectionHeader("MY-PROJECT"));
        content.getChildren().add(createProjectTreeSection());

        // Outline
        content.getChildren().add(createSectionHeader("Outline"));
        VBox outlineContent = new VBox();
        outlineContent.setPadding(new Insets(4, 12, 4, 12));
        Label outlineLabel = new Label("No symbols found in document.");
        outlineLabel.setTextFill(Color.web("#6e6e6e"));
        outlineLabel.setFont(Font.font("Segoe UI", 12));
        outlineContent.getChildren().add(outlineLabel);
        content.getChildren().add(outlineContent);

        // Timeline
        content.getChildren().add(createSectionHeader("Timeline"));
        VBox timelineContent = new VBox();
        timelineContent.setPadding(new Insets(6, 12, 6, 12));
        Label commit1 = new Label("  abc1234 - Update user profile");
        commit1.setTextFill(Color.web("#6e6e6e"));
        commit1.setFont(Font.font("Segoe UI", 12));
        Label commit2 = new Label("  def5678 - Initial commit");
        commit2.setTextFill(Color.web("#6e6e6e"));
        commit2.setFont(Font.font("Segoe UI", 12));
        timelineContent.getChildren().addAll(commit1, commit2);
        content.getChildren().add(timelineContent);

        scrollPane.setContent(content);
        view.getChildren().add(scrollPane);

        return view;
    }

    private VBox createSearchView() {
        VBox view = new VBox(8);
        view.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");
        view.setPadding(new Insets(0, 12, 0, 12));

        // 搜索框
        HBox searchBox = new HBox(4);
        searchBox.setStyle("-fx-background-color: #3c3c3c; -fx-padding: 0 4; -fx-border-color: #3c3c3c; -fx-border-radius: 0;");
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPrefHeight(24);

        Label arrow1 = new Label("▼");
        arrow1.setTextFill(Color.web("#969696"));
        arrow1.setFont(Font.font(10));
        Label arrow2 = new Label("▶");
        arrow2.setTextFill(Color.web("#969696"));
        arrow2.setFont(Font.font(10));

        TextField searchInput = new TextField();
        searchInput.setPromptText("Search");
        searchInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #cccccc; -fx-prompt-text-fill: #6e6e6e; -fx-border-color: transparent;");
        HBox.setHgrow(searchInput, Priority.ALWAYS);

        Label moreBtn = new Label("⋯");
        moreBtn.setTextFill(Color.web("#969696"));
        moreBtn.setFont(Font.font(14));

        searchBox.getChildren().addAll(arrow1, arrow2, searchInput, moreBtn);

        // 搜索选项
        HBox searchOpts = new HBox(2);
        searchOpts.setPadding(new Insets(4, 0, 0, 0));
        String[] optIcons = {"Aa", "ab", ".*"};
        for (String opt : optIcons) {
            Label optBtn = new Label(opt);
            optBtn.setTextFill(Color.web("#969696"));
            optBtn.setFont(Font.font("Segoe UI", 11));
            optBtn.setPrefSize(24, 22);
            optBtn.setAlignment(Pos.CENTER);
            optBtn.setStyle("-fx-cursor: hand; -fx-border-color: transparent; -fx-border-radius: 3; -fx-background-radius: 3;");
            optBtn.setOnMouseClicked(e -> {
                if (optBtn.getStyle().contains("background-color: rgba(255,255,255,0.15)")) {
                    optBtn.setStyle("-fx-cursor: hand; -fx-border-color: transparent; -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: #969696;");
                } else {
                    optBtn.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.15); -fx-border-radius: 3; -fx-background-radius: 3; -fx-text-fill: #cccccc;");
                }
            });
            searchOpts.getChildren().add(optBtn);
        }

        // 替换框
        HBox replaceBox = new HBox(4);
        replaceBox.setStyle("-fx-background-color: #3c3c3c; -fx-padding: 0 4; -fx-border-color: #3c3c3c;");
        replaceBox.setAlignment(Pos.CENTER_LEFT);
        replaceBox.setPrefHeight(24);

        Label arrow3 = new Label("▶");
        arrow3.setTextFill(Color.web("#969696"));
        arrow3.setFont(Font.font(10));

        TextField replaceInput = new TextField();
        replaceInput.setPromptText("Replace");
        replaceInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #cccccc; -fx-prompt-text-fill: #6e6e6e; -fx-border-color: transparent;");
        HBox.setHgrow(replaceInput, Priority.ALWAYS);

        replaceBox.getChildren().addAll(arrow3, replaceInput);

        // 提示文本
        Label hint = new Label("Type to search across files");
        hint.setTextFill(Color.web("#6e6e6e"));
        hint.setFont(Font.font("Segoe UI", 12));
        hint.setPadding(new Insets(20, 0, 0, 0));
        hint.setAlignment(Pos.CENTER);
        hint.setMaxWidth(Double.MAX_VALUE);

        view.getChildren().addAll(searchBox, searchOpts, replaceBox, hint);

        return view;
    }

    private VBox createSCMView() {
        VBox view = new VBox();
        view.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");

        // 提交消息输入
        TextArea commitInput = new TextArea();
        commitInput.setPromptText("Message (Ctrl+Enter to commit)");
        commitInput.setPrefHeight(26);
        commitInput.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: #cccccc; -fx-prompt-text-fill: #6e6e6e; -fx-border-color: #3c3c3c; -fx-font-size: 13;");
        commitInput.setPadding(new Insets(4, 8, 4, 8));
        view.getChildren().add(commitInput);

        // Changes 部分
        HBox changesHeader = createSectionHeader("Changes");
        view.getChildren().add(changesHeader);

        // 变更列表
        VBox changesList = new VBox();
        String[][] changes = {
                {"\uD83D\uDCC4", "src/components/UserProfile.tsx", "M"},
                {"\uD83D\uDCC4", "src/styles/main.css", "M"},
                {"\u2795", "src/utils/helpers.ts", "A"}
        };
        for (String[] change : changes) {
            HBox item = new HBox(8);
            item.setPrefHeight(22);
            item.setAlignment(Pos.CENTER_LEFT);
            item.setPadding(new Insets(0, 8, 0, 22));
            item.setStyle("-fx-cursor: hand;");
            item.setOnMouseEntered(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: " + HOVER_BG + ";"));
            item.setOnMouseExited(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));

            Label icon = new Label(change[0]);
            icon.setFont(Font.font(14));

            Label name = new Label(change[1]);
            name.setTextFill(Color.web("#cccccc"));
            name.setFont(Font.font("Segoe UI", 13));
            HBox.setHgrow(name, Priority.ALWAYS);

            Label status = new Label(change[2]);
            status.setTextFill(Color.web("#73c991"));
            status.setFont(Font.font("Segoe UI", 11));

            item.getChildren().addAll(icon, name, status);
            changesList.getChildren().add(item);
        }
        view.getChildren().add(changesList);

        return view;
    }

    private VBox createDebugView() {
        VBox view = new VBox();
        view.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");
        view.setPadding(new Insets(16, 20, 16, 20));

        Label desc = new Label("To customize Run and Debug");
        desc.setTextFill(Color.web("#cccccc"));
        desc.setFont(Font.font("Segoe UI", 13));

        Label link = new Label("create a launch.json file");
        link.setTextFill(Color.web("#3794ff"));
        link.setFont(Font.font("Segoe UI", 13));
        link.setStyle("-fx-cursor: hand;");
        link.setOnMouseEntered(e -> link.setUnderline(true));
        link.setOnMouseExited(e -> link.setUnderline(false));

        Label hint = new Label("Show all automatic debug configurations.");
        hint.setTextFill(Color.web("#969696"));
        hint.setFont(Font.font("Segoe UI", 12));

        ComboBox<String> configSelect = new ComboBox<>();
        configSelect.getItems().addAll("Node.js", "Chrome", "Edge");
        configSelect.setValue("Node.js");
        configSelect.setStyle("-fx-background-color: #3c3c3c; -fx-border-color: #3c3c3c; -fx-text-fill: #cccccc;");
        configSelect.setPrefWidth(Double.MAX_VALUE);

        Button startBtn = new Button("Start Debugging");
        startBtn.setStyle("-fx-background-color: " + ACCENT + "; -fx-text-fill: white; -fx-background-radius: 2;");
        startBtn.setPadding(new Insets(4, 14, 4, 14));

        VBox container = new VBox(12, desc, link, hint, configSelect, startBtn);
        view.getChildren().add(container);

        return view;
    }

    private VBox createExtensionsView() {
        VBox view = new VBox();
        view.setStyle("-fx-background-color: " + SIDEBAR_BG + ";");

        // 搜索框
        TextField searchInput = new TextField();
        searchInput.setPromptText("Search Extensions in Marketplace");
        searchInput.setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: #cccccc; -fx-prompt-text-fill: #6e6e6e; -fx-border-color: #3c3c3c; -fx-padding: 4 8;");
        searchInput.setPrefHeight(24);
        view.getChildren().add(searchInput);

        // Installed 部分
        HBox installedHeader = createSectionHeader("Installed");
        view.getChildren().add(installedHeader);

        // 扩展列表
        VBox extList = new VBox();
        String[][] extensions = {
                {"\uD83C\uDFA8", "One Dark Pro", "Theme based on Atom's One Dark", "4.8", "#3794ff"},
                {"\uD83D\uDCDD", "ESLint", "Integrates ESLint JavaScript into VS Code", "4.5", "#4ec9b0"},
                {"\uD83E\uDDE9", "Prettier", "Code formatter using prettier", "4.7", "#ce9178"},
                {"\uD83C\uDF10", "Auto Rename Tag", "Auto rename paired HTML/XML tag", "4.3", "#569cd6"},
                {"\uD83D\uDCDC", "GitLens", "Supercharge Git within VS Code", "4.9", "#dcdcaa"},
                {"\u2699", "TypeScript Hero", "TypeScript tooling for VS Code", "4.1", "#b5cea8"}
        };

        for (String[] ext : extensions) {
            HBox item = new HBox(8);
            item.setPadding(new Insets(8, 12, 8, 12));
            item.setStyle("-fx-cursor: hand;");
            item.setOnMouseEntered(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: " + HOVER_BG + ";"));
            item.setOnMouseExited(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));

            // 图标
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(36, 36);
            iconBox.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 4;");
            Label icon = new Label(ext[0]);
            icon.setFont(Font.font(18));
            icon.setTextFill(Color.web(ext[4]));
            iconBox.getChildren().add(icon);

            // 信息
            VBox info = new VBox(2);
            Label name = new Label(ext[1]);
            name.setTextFill(Color.web("#cccccc"));
            name.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

            Label desc = new Label(ext[2]);
            desc.setTextFill(Color.web("#969696"));
            desc.setFont(Font.font("Segoe UI", 12));

            HBox meta = new HBox(4);
            Label star = new Label("★");
            star.setTextFill(Color.web("#e8c547"));
            star.setFont(Font.font(11));
            Label rating = new Label(ext[3]);
            rating.setTextFill(Color.web("#6e6e6e"));
            rating.setFont(Font.font("Segoe UI", 11));
            meta.getChildren().addAll(star, rating);

            info.getChildren().addAll(name, desc, meta);
            item.getChildren().addAll(iconBox, info);
            extList.getChildren().add(item);
        }
        view.getChildren().add(extList);

        return view;
    }

    private HBox createSectionHeader(String title) {
        HBox header = new HBox(2);
        header.setStyle("-fx-background-color: " + SIDEBAR_BG + "; -fx-border-color: " + BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        header.setPrefHeight(22);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 8, 0, 8));

        Label chevron = new Label("▼");
        chevron.setTextFill(Color.web("#969696"));
        chevron.setFont(Font.font("Segoe UI", 10));
        chevron.setPrefWidth(16);
        chevron.setAlignment(Pos.CENTER);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web("#6e6e6e"));
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 11));
        titleLabel.setStyle("-fx-letter-spacing: 0.5;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(chevron, titleLabel, spacer);

        // 点击折叠/展开
        header.setOnMouseClicked(e -> {
            VBox parent = (VBox) header.getParent();
            int index = parent.getChildren().indexOf(header);
            if (index + 1 < parent.getChildren().size()) {
                Node nextNode = parent.getChildren().get(index + 1);
                if (nextNode instanceof VBox body) {
                    boolean collapsed = body.isVisible();
                    body.setVisible(!collapsed);
                    body.setManaged(!collapsed);
                    chevron.setRotate(collapsed ? -90 : 0);
                }
            }
        });
        header.setStyle(header.getStyle() + " -fx-cursor: hand;");

        return header;
    }

    private VBox createOpenEditorsSection() {
        VBox section = new VBox();

        HBox welcomeItem = createOpenEditorItem("\uD83D\uDCC4", "Welcome", "1", false);
        welcomeItem.setOnMouseClicked(e -> switchEditorTab("Welcome"));

        HBox profileItem = createOpenEditorItem("\uD83D\uDCDD", "UserProfile.tsx", "1", true);
        profileItem.setOnMouseClicked(e -> switchEditorTab("UserProfile"));

        section.getChildren().addAll(welcomeItem, profileItem);
        return section;
    }

    private HBox createOpenEditorItem(String icon, String label, String group, boolean active) {
        HBox item = new HBox(4);
        item.setStyle("-fx-background-color: " + (active ? SELECTED_BG : "transparent") + ";");
        item.setPrefHeight(22);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(0, 8, 0, 22));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(14));
        iconLabel.setPrefWidth(16);
        iconLabel.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.web("#cccccc"));
        nameLabel.setFont(Font.font("Segoe UI", 13));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label groupLabel = new Label(group);
        groupLabel.setTextFill(Color.web("#6e6e6e"));
        groupLabel.setFont(Font.font("Segoe UI", 10));

        item.getChildren().addAll(iconLabel, nameLabel, groupLabel);
        item.setOnMouseEntered(e -> { if (!active) item.setStyle("-fx-background-color: " + HOVER_BG + ";"); });
        item.setOnMouseExited(e -> { if (!active) item.setStyle("-fx-background-color: transparent;"); });

        return item;
    }

    private VBox createProjectTreeSection() {
        VBox section = new VBox();

        section.getChildren().add(createTreeItem("\uD83D\uDCC2", ".vscode", 0, false, true));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "launch.json", 1, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "settings.json", 1, false, false));

        section.getChildren().add(createTreeItem("\uD83D\uDCC2", "src", 0, false, true));
        section.getChildren().add(createTreeItem("\uD83D\uDCC2", "components", 1, false, true));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "App.tsx", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "Header.tsx", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "UserProfile.tsx", 2, true, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCC2", "styles", 1, false, true));
        section.getChildren().add(createTreeItem("\uD83C\uDFA8", "main.css", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83C\uDFA8", "variables.css", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCC2", "utils", 1, false, true));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "api.ts", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "helpers.ts", 2, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "index.tsx", 1, false, false));

        section.getChildren().add(createTreeItem("\uD83D\uDCC4", ".gitignore", 0, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "package.json", 0, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCDD", "tsconfig.json", 0, false, false));
        section.getChildren().add(createTreeItem("\uD83D\uDCC4", "README.md", 0, false, false));

        return section;
    }

    private HBox createTreeItem(String icon, String label, int indent, boolean active, boolean isFolder) {
        HBox item = new HBox(4);
        item.setStyle("-fx-background-color: " + (active ? SELECTED_BG : "transparent") + ";");
        item.setPrefHeight(22);
        item.setAlignment(Pos.CENTER_LEFT);

        int leftPadding = 8 + indent * 8;
        item.setPadding(new Insets(0, 8, 0, leftPadding));

        Label twistie = new Label(isFolder ? "▶" : "");
        twistie.setTextFill(Color.web("#969696"));
        twistie.setFont(Font.font("Segoe UI", 10));
        twistie.setPrefWidth(16);
        twistie.setAlignment(Pos.CENTER);
        if (isFolder) twistie.setRotate(90);

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(14));
        iconLabel.setPrefWidth(16);
        iconLabel.setAlignment(Pos.CENTER);

        Label nameLabel = new Label(label);
        nameLabel.setTextFill(Color.web("#cccccc"));
        nameLabel.setFont(Font.font("Segoe UI", 13));
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        item.getChildren().addAll(twistie, iconLabel, nameLabel);
        item.setOnMouseEntered(e -> { if (!active) item.setStyle("-fx-background-color: " + HOVER_BG + ";"); });
        item.setOnMouseExited(e -> { if (!active) item.setStyle("-fx-background-color: transparent;"); });

        // 文件夹点击展开/折叠
        if (isFolder) {
            item.setOnMouseClicked(e -> {
                boolean isOpen = twistie.getRotate() == 90;
                twistie.setRotate(isOpen ? 0 : 90);
                iconLabel.setText(isOpen ? "\uD83D\uDCC1" : "\uD83D\uDCC2");

                // 折叠/展开子项
                VBox parent = (VBox) item.getParent();
                int index = parent.getChildren().indexOf(item);
                int currentIndent = indent;

                for (int i = index + 1; i < parent.getChildren().size(); i++) {
                    if (parent.getChildren().get(i) instanceof HBox child) {
                        // 检查子项的缩进
                        int childIndent = (int) ((child.getPadding().getLeft() - 8) / 8);
                        if (childIndent <= currentIndent) break;
                        child.setVisible(!isOpen);
                        child.setManaged(!isOpen);
                    }
                }
            });
        }

        return item;
    }

    // ==================== 编辑器 ====================

    private StackPane createEditorArea() {
        StackPane editorPane = new StackPane();
        editorPane.setStyle("-fx-background-color: " + EDITOR_BG + ";");

        // 代码编辑器视图
        VBox editorContainer = new VBox();

        editorContainer.getChildren().add(createEditorTabs());
        breadcrumbs = createBreadcrumbs();
        editorContainer.getChildren().add(breadcrumbs);

        StackPane editorViewport = new StackPane();
        HBox codeContainer = new HBox();

        codeContainer.getChildren().add(createLineNumbers());

        ScrollPane codeScroll = new ScrollPane();
        codeScroll.setStyle("-fx-background-color: " + EDITOR_BG + "; -fx-border-color: transparent;");
        codeScroll.setFitToWidth(true);
        codeScroll.setFitToHeight(true);
        codeScroll.setContent(createCodeContent());
        codeContainer.getChildren().add(codeScroll);
        HBox.setHgrow(codeScroll, Priority.ALWAYS);

        codeContainer.getChildren().add(createMinimap());

        editorViewport.getChildren().add(codeContainer);
        VBox.setVgrow(editorViewport, Priority.ALWAYS);
        editorContainer.getChildren().add(editorViewport);

        codeEditorView = editorContainer;

        // Welcome 视图
        welcomeView = createWelcomeContent();
        welcomeView.setVisible(false);
        welcomeView.setManaged(false);

        editorPane.getChildren().addAll(codeEditorView, welcomeView);

        return editorPane;
    }

    private HBox createEditorTabs() {
        HBox tabs = new HBox();
        tabs.setStyle("-fx-background-color: " + EDITOR_TAB_INACTIVE + "; -fx-border-color: transparent transparent #252526 transparent; -fx-border-width: 0 0 1 0;");
        tabs.setPrefHeight(35);
        tabs.setAlignment(Pos.BOTTOM_LEFT);

        editorTabs[0] = createEditorTab("\uD83D\uDCC4", "Welcome", false, false);
        editorTabs[1] = createEditorTab("\uD83D\uDCDD", "UserProfile.tsx", true, false);
        editorTabs[2] = createEditorTab("\uD83C\uDFA8", "main.css", false, true);

        editorTabs[0].setOnMouseClicked(e -> switchEditorTab("Welcome"));
        editorTabs[1].setOnMouseClicked(e -> switchEditorTab("UserProfile"));
        editorTabs[2].setOnMouseClicked(e -> switchEditorTab("main.css"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(2);
        actions.setPadding(new Insets(0, 8, 0, 8));
        actions.setAlignment(Pos.CENTER);
        Label splitBtn = new Label("⊞");
        splitBtn.setTextFill(Color.web("#969696"));
        splitBtn.setFont(Font.font(14));
        Label moreBtn = new Label("⋯");
        moreBtn.setTextFill(Color.web("#969696"));
        moreBtn.setFont(Font.font(14));
        actions.getChildren().addAll(splitBtn, moreBtn);

        tabs.getChildren().addAll(editorTabs[0], editorTabs[1], editorTabs[2], spacer, actions);
        return tabs;
    }

    private StackPane createEditorTab(String icon, String name, boolean active, boolean modified) {
        HBox tab = new HBox(6);
        tab.setStyle("-fx-background-color: " + (active ? EDITOR_TAB_ACTIVE : EDITOR_TAB_INACTIVE) + ";");
        tab.setPrefHeight(35);
        tab.setAlignment(Pos.CENTER_LEFT);
        tab.setPadding(new Insets(0, 10, 0, 10));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(14));

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(active ? Color.WHITE : Color.web("#969696"));
        nameLabel.setFont(Font.font("Segoe UI", 13));

        tab.getChildren().addAll(iconLabel, nameLabel);

        if (modified) {
            Label dot = new Label("●");
            dot.setTextFill(Color.web("#c4c4c4"));
            dot.setFont(Font.font(8));
            tab.getChildren().add(dot);
        }

        Label closeBtn = new Label("✕");
        closeBtn.setTextFill(Color.web("#969696"));
        closeBtn.setFont(Font.font(14));
        closeBtn.setPadding(new Insets(0, 0, 0, 4));
        closeBtn.setStyle("-fx-cursor: hand;");
        tab.getChildren().add(closeBtn);

        StackPane container = new StackPane(tab);
        container.setPrefWidth(150);
        container.setUserData(name);

        if (active) {
            Region topBorder = new Region();
            topBorder.setPrefHeight(1);
            topBorder.setStyle("-fx-background-color: " + ACCENT + ";");
            StackPane.setAlignment(topBorder, Pos.TOP_CENTER);
            container.getChildren().add(topBorder);
        }

        // 关闭标签
        closeBtn.setOnMouseClicked(e -> {
            e.consume();
            // 简单实现：切换到下一个标签
            if (name.equals(activeEditorTab)) {
                if ("UserProfile".equals(name)) {
                    switchEditorTab("Welcome");
                } else {
                    switchEditorTab("UserProfile");
                }
            }
        });

        return container;
    }

    private HBox createBreadcrumbs() {
        HBox bc = new HBox(2);
        bc.setStyle("-fx-background-color: " + EDITOR_BG + "; -fx-border-color: transparent transparent #1a1a1a transparent; -fx-border-width: 0 0 1 0;");
        bc.setPrefHeight(22);
        bc.setAlignment(Pos.CENTER_LEFT);
        bc.setPadding(new Insets(0, 12, 0, 12));

        Label src = new Label("  src");
        src.setTextFill(Color.web("#969696"));
        src.setFont(Font.font("Segoe UI", 12));
        src.setOnMouseEntered(e -> src.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 3;"));
        src.setOnMouseExited(e -> src.setStyle("-fx-background-color: transparent;"));

        Label sep1 = new Label(" ›");
        sep1.setTextFill(Color.web("#6e6e6e"));
        sep1.setFont(Font.font("Segoe UI", 14));

        Label components = new Label("  components");
        components.setTextFill(Color.web("#969696"));
        components.setFont(Font.font("Segoe UI", 12));

        Label sep2 = new Label(" ›");
        sep2.setTextFill(Color.web("#6e6e6e"));
        sep2.setFont(Font.font("Segoe UI", 14));

        Label file = new Label("  UserProfile.tsx");
        file.setTextFill(Color.web("#969696"));
        file.setFont(Font.font("Segoe UI", 12));

        bc.getChildren().addAll(src, sep1, components, sep2, file);
        return bc;
    }

    private VBox createLineNumbers() {
        VBox lineNums = new VBox();
        lineNums.setStyle("-fx-background-color: " + EDITOR_BG + ";");
        lineNums.setPadding(new Insets(0, 16, 0, 16));
        lineNums.setAlignment(Pos.TOP_RIGHT);
        lineNums.setMinWidth(60);

        for (int i = 1; i <= 58; i++) {
            Label lineNum = new Label(String.valueOf(i));
            lineNum.setTextFill(i == 28 ? Color.WHITE : Color.web("#858585"));
            lineNum.setFont(Font.font("Consolas", 13));
            lineNum.setPrefHeight(20);
            lineNum.setAlignment(Pos.CENTER_RIGHT);
            lineNums.getChildren().add(lineNum);
        }

        return lineNums;
    }

    private VBox createCodeContent() {
        VBox codeContent = new VBox();
        codeContent.setStyle("-fx-background-color: " + EDITOR_BG + ";");
        codeContent.setPadding(new Insets(0, 30, 0, 4));

        String[][] codeLines = {
                {"K", "import "}, {"V", "React"}, {"K", ", { "}, {"V", "useState"}, {"K", ", "}, {"V", "useEffect"}, {"K", " } "}, {"K", "from "}, {"S", "'react'"}, {"P", ";"},
                {"K", "import "}, {"P", "{ "}, {"T", "User"}, {"P", " } "}, {"K", "from "}, {"S", "'../types'"}, {"P", ";"},
                {"K", "import "}, {"P", "{ "}, {"F", "fetchUserData"}, {"P", " } "}, {"K", "from "}, {"S", "'../api'"}, {"P", ";"},
                {},
                {"K", "interface "}, {"T", "UserProfileProps"}, {"P", " {"},
                {"P", "  "}, {"V", "userId"}, {"P", ":"}, {"T", " string"}, {"P", ";"},
                {"P", "  "}, {"V", "onUpdate"}, {"P", "?:"}, {"P", " ("}, {"V", "user"}, {"P", ":"}, {"T", " User"}, {"P", ") "}, {"K", "=>"}, {"T", " void"}, {"P", ";"},
                {"P", "}"},
                {},
                {"K", "export const "}, {"F", "UserProfile"}, {"P", ":"}, {"T", " React.FC"}, {"P", "<"}, {"T", "UserProfileProps"}, {"P", "> = ({ "}, {"V", "userId"}, {"P", ", "}, {"V", "onUpdate"}, {"P", " }) => {"},
                {"P", "  "}, {"K", "const "}, {"P", "["}, {"V", "user"}, {"P", ", "}, {"F", "setUser"}, {"P", "] = "}, {"F", "useState"}, {"P", "<"}, {"T", "User"}, {"P", " | "}, {"T", "null"}, {"P", ">("}, {"K", "null"}, {"P", ");"},
                {"P", "  "}, {"K", "const "}, {"P", "["}, {"V", "loading"}, {"P", ", "}, {"F", "setLoading"}, {"P", "] = "}, {"F", "useState"}, {"P", "("}, {"K", "true"}, {"P", ");"},
                {"P", "  "}, {"K", "const "}, {"P", "["}, {"V", "error"}, {"P", ", "}, {"F", "setError"}, {"P", "] = "}, {"F", "useState"}, {"P", "<"}, {"T", "string"}, {"P", " | "}, {"T", "null"}, {"P", ">("}, {"K", "null"}, {"P", ");"},
                {},
                {"P", "  "}, {"F", "useEffect"}, {"P", "(() => {"},
                {"P", "    "}, {"K", "const "}, {"F", "loadUser"}, {"P", " = "}, {"K", "async"}, {"P", " () => {"},
                {"P", "      "}, {"K", "try"}, {"P", " {"},
                {"P", "        "}, {"F", "setLoading"}, {"P", "("}, {"K", "true"}, {"P", ");"},
                {"P", "        "}, {"K", "const "}, {"V", "data"}, {"P", " = "}, {"K", "await"}, {"P", " "}, {"F", "fetchUserData"}, {"P", "("}, {"V", "userId"}, {"P", ");"},
                {"P", "        "}, {"F", "setUser"}, {"P", "("}, {"V", "data"}, {"P", ");"},
                {"P", "        "}, {"V", "onUpdate"}, {"P", "?."}, {"P", "("}, {"V", "data"}, {"P", ");"},
                {"P", "      } "}, {"K", "catch"}, {"P", " ("}, {"V", "err"}, {"P", ") {"},
                {"P", "        "}, {"F", "setError"}, {"P", "("}, {"S", "'Failed to load user data'"}, {"P", ");"},
                {"P", "        "}, {"V", "console"}, {"P", "."}, {"F", "error"}, {"P", "("}, {"V", "err"}, {"P", ");"},
                {"P", "      } "}, {"K", "finally"}, {"P", " {"},
                {"P", "        "}, {"F", "setLoading"}, {"P", "("}, {"K", "false"}, {"P", ");"},
                {"P", "      }"},
                {"P", "    };"},
                {},
                {"P", "    "}, {"F", "loadUser"}, {"P", "();"},
                {"P", "  }, ["}, {"V", "userId"}, {"P", ", "}, {"V", "onUpdate"}, {"P", "]);"},
                {},
                {"P", "  "}, {"K", "if"}, {"P", " ("}, {"V", "loading"}, {"P", ") "}, {"K", "return"}, {"P", " <"}, {"K", "div"}, {"P", " "}, {"V", "className"}, {"P", "="}, {"S", "\"spinner\""}, {"P", ">Loading..."}, {"P", "</"}, {"K", "div"}, {"P", ">;"},
                {"P", "  "}, {"K", "if"}, {"P", " ("}, {"V", "error"}, {"P", ") "}, {"K", "return"}, {"P", " <"}, {"K", "div"}, {"P", " "}, {"V", "className"}, {"P", "="}, {"S", "\"error\""}, {"P", ">{"}, {"V", "error"}, {"P", "}"}, {"P", "</"}, {"K", "div"}, {"P", ">;"},
                {"P", "  "}, {"K", "if"}, {"P", " (!"}, {"V", "user"}, {"P", ") "}, {"K", "return"}, {"P", " "}, {"K", "null"}, {"P", ";"},
                {},
                {"P", "  "}, {"K", "return"}, {"P", " ("},
                {"P", "    <"}, {"K", "div"}, {"P", " "}, {"V", "className"}, {"P", "="}, {"S", "\"user-profile\""}, {"P", ">"},
                {"P", "      <"}, {"K", "h2"}, {"P", ">{"}, {"V", "user"}, {"P", "."}, {"V", "name"}, {"P", "}"}, {"P", "</"}, {"K", "h2"}, {"P", ">"},
                {"P", "      <"}, {"K", "p"}, {"P", " "}, {"V", "className"}, {"P", "="}, {"S", "\"email\""}, {"P", ">{"}, {"V", "user"}, {"P", "."}, {"V", "email"}, {"P", "}"}, {"P", "</"}, {"K", "p"}, {"P", ">"},
                {"P", "      <"}, {"K", "div"}, {"P", " "}, {"V", "className"}, {"P", "="}, {"S", "\"stats\""}, {"P", ">"},
                {"P", "        <"}, {"K", "span"}, {"P", ">Posts: {"}, {"V", "user"}, {"P", "."}, {"V", "postCount"}, {"P", "}"}, {"P", "</"}, {"K", "span"}, {"P", ">"},
                {"P", "        <"}, {"K", "span"}, {"P", ">Followers: {"}, {"V", "user"}, {"P", "."}, {"V", "followerCount"}, {"P", "}"}, {"P", "</"}, {"K", "span"}, {"P", ">"},
                {"P", "      </"}, {"K", "div"}, {"P", ">"},
                {"P", "    </"}, {"K", "div"}, {"P", ">"},
                {"P", "  );"},
                {"P", "};"},
                {},
                {"K", "export default "}, {"V", "UserProfile"}, {"P", ";"},
        };

        int lineNum = 0;
        for (String[] line : codeLines) {
            lineNum++;
            HBox codeLine = new HBox();
            codeLine.setPrefHeight(20);
            codeLine.setAlignment(Pos.CENTER_LEFT);
            if (lineNum == 28) {
                codeLine.setStyle("-fx-background-color: #2a2d2e;");
            }

            if (line.length == 0) {
                Label space = new Label(" ");
                space.setFont(Font.font("Consolas", 13));
                codeLine.getChildren().add(space);
            } else {
                for (int i = 0; i < line.length; i += 2) {
                    String type = line[i];
                    String text = line[i + 1];
                    Label token = new Label(text);
                    token.setFont(Font.font("Consolas", 13));
                    token.setTextFill(getSyntaxColor(type));
                    codeLine.getChildren().add(token);
                }
            }
            codeContent.getChildren().add(codeLine);
        }

        return codeContent;
    }

    private Color getSyntaxColor(String type) {
        return switch (type) {
            case "K" -> Color.web("#569cd6");
            case "S" -> Color.web("#ce9178");
            case "C" -> Color.web("#6a9955");
            case "F" -> Color.web("#dcdcaa");
            case "V" -> Color.web("#9cdcfe");
            case "T" -> Color.web("#4ec9b0");
            case "N" -> Color.web("#b5cea8");
            case "P" -> Color.web("#808080");
            default -> Color.web("#d4d4d4");
        };
    }

    private VBox createMinimap() {
        VBox minimap = new VBox();
        minimap.setStyle("-fx-background-color: " + EDITOR_BG + ";");
        minimap.setPrefWidth(57);
        minimap.setMinWidth(57);

        String[] colors = {"#569cd6", "#ce9178", "#6a9955", "#dcdcaa", "#9cdcfe", "#4ec9b0", "#b5cea8", "#d4d4d4", "#808080"};

        for (int i = 0; i < 58; i++) {
            HBox line = new HBox();
            line.setPrefHeight(4);
            int width = (int) (Math.random() * 60 + 10);
            Region bar = new Region();
            bar.setPrefSize(width, 3);
            bar.setStyle("-fx-background-color: " + colors[i % colors.length] + "; -fx-opacity: 0.7;");
            line.getChildren().add(bar);
            minimap.getChildren().add(line);
        }

        Region viewport = new Region();
        viewport.setPrefHeight(120);
        viewport.setStyle("-fx-background-color: rgba(100,100,100,0.2); -fx-border-color: rgba(100,100,100,0.1);");

        return minimap;
    }

    private VBox createWelcomeContent() {
        VBox welcome = new VBox(30);
        welcome.setStyle("-fx-background-color: " + EDITOR_BG + ";");
        welcome.setPadding(new Insets(40, 40, 40, 40));

        Label title = new Label("Visual Studio Code");
        title.setTextFill(Color.web("#cccccc"));
        title.setFont(Font.font("Segoe UI", FontWeight.THIN, 26));

        HBox columns = new HBox(60);

        VBox startCol = createWelcomeColumn("Start", new String[]{
                "New File  Ctrl+N", "Open File...  Ctrl+O", "Open Folder...  Ctrl+K Ctrl+O",
                "Open Recent  →", "Clone Git Repository..."
        });

        VBox recentCol = createWelcomeColumn("Recent", new String[]{
                "my-project", "backend-api", "portfolio-site"
        });

        VBox helpCol = createWelcomeColumn("Help", new String[]{
                "Welcome", "Documentation", "Release Notes", "Keyboard Shortcuts Reference", "Interactive Playground"
        });

        columns.getChildren().addAll(startCol, recentCol, helpCol);
        welcome.getChildren().addAll(title, columns);

        return welcome;
    }

    private VBox createWelcomeColumn(String title, String[] links) {
        VBox column = new VBox(5);

        Label header = new Label(title);
        header.setTextFill(Color.web("#cccccc"));
        header.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        column.getChildren().add(header);

        for (String link : links) {
            Label linkLabel = new Label(link);
            linkLabel.setTextFill(Color.web("#3794ff"));
            linkLabel.setFont(Font.font("Segoe UI", 13));
            linkLabel.setStyle("-fx-cursor: hand;");
            linkLabel.setOnMouseEntered(e -> linkLabel.setUnderline(true));
            linkLabel.setOnMouseExited(e -> linkLabel.setUnderline(false));
            column.getChildren().add(linkLabel);
        }

        return column;
    }

    // ==================== 底部面板 ====================

    private VBox createPanelContainer() {
        VBox panel = new VBox();
        panel.setStyle("-fx-background-color: " + PANEL_BG + "; -fx-border-color: " + BORDER + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");
        panel.setPrefHeight(200);
        panel.setMinHeight(80);

        HBox header = new HBox();
        header.setStyle("-fx-background-color: " + PANEL_HEADER_BG + ";");
        header.setPrefHeight(35);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 8, 0, 8));

        HBox panelTabsBox = new HBox();
        panelTabsBox.setAlignment(Pos.CENTER_LEFT);

        String[] tabNames = {"Problems", "Output", "Debug Console", "Terminal"};
        int[] counts = {2, 0, 0, 0};
        String[] tabIds = {"problems", "output", "debug-console", "terminal"};

        for (int i = 0; i < tabNames.length; i++) {
            panelTabs[i] = createPanelTab(tabNames[i], tabIds[i], counts[i], i == 3);
            panelTabsBox.getChildren().add(panelTabs[i]);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox actions = new HBox(2);
        actions.setAlignment(Pos.CENTER);
        String[] actionIcons = {"+", "🗑", "⤢", "✕"};
        for (String icon : actionIcons) {
            Label actionBtn = new Label(icon);
            actionBtn.setTextFill(Color.web("#969696"));
            actionBtn.setFont(Font.font(14));
            actionBtn.setPrefSize(24, 24);
            actionBtn.setAlignment(Pos.CENTER);
            actionBtn.setStyle("-fx-cursor: hand; -fx-background-radius: 3;");
            actionBtn.setOnMouseEntered(e -> actionBtn.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 3;"));
            actionBtn.setOnMouseExited(e -> actionBtn.setStyle("-fx-cursor: hand; -fx-background-color: transparent; -fx-background-radius: 3;"));

            if ("✕".equals(icon)) {
                actionBtn.setOnMouseClicked(e -> togglePanel());
            }

            actions.getChildren().add(actionBtn);
        }

        header.getChildren().addAll(panelTabsBox, spacer, actions);

        StackPane panelBody = new StackPane();

        panelViews[0] = createProblemsView();
        panelViews[1] = createOutputView();
        panelViews[2] = createDebugConsoleView();
        panelViews[3] = createTerminalView();

        for (int i = 0; i < panelViews.length; i++) {
            panelViews[i].setVisible(i == 3);
            panelBody.getChildren().add(panelViews[i]);
        }

        panel.getChildren().addAll(header, panelBody);
        VBox.setVgrow(panelBody, Priority.ALWAYS);

        return panel;
    }

    private StackPane createPanelTab(String name, String tabId, int count, boolean active) {
        HBox tab = new HBox(4);
        tab.setAlignment(Pos.CENTER);
        tab.setPadding(new Insets(0, 12, 0, 12));
        tab.setPrefHeight(35);

        Label nameLabel = new Label(name);
        nameLabel.setTextFill(active ? Color.web("#cccccc") : Color.web("#969696"));
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 11));
        nameLabel.setStyle("-fx-letter-spacing: 0.5;");

        tab.getChildren().add(nameLabel);

        if (count > 0) {
            Label countLabel = new Label(String.valueOf(count));
            countLabel.setTextFill(Color.WHITE);
            countLabel.setFont(Font.font("Segoe UI", 10));
            countLabel.setStyle("-fx-background-color: #f44747; -fx-padding: 0 4; -fx-background-radius: 7;");
            tab.getChildren().add(countLabel);
        }

        StackPane container = new StackPane(tab);
        container.setUserData(tabId);
        container.setOnMouseClicked(e -> switchPanelTab(tabId));
        container.setOnMouseEntered(e -> nameLabel.setTextFill(Color.web("#cccccc")));
        container.setOnMouseExited(e -> {
            if (!tabId.equals(activePanelTab)) {
                nameLabel.setTextFill(Color.web("#969696"));
            }
        });

        if (active) {
            Region bottomBorder = new Region();
            bottomBorder.setPrefHeight(1);
            bottomBorder.setStyle("-fx-background-color: " + ACCENT + ";");
            StackPane.setAlignment(bottomBorder, Pos.BOTTOM_CENTER);
            container.getChildren().add(bottomBorder);
        }

        return container;
    }

    private VBox createProblemsView() {
        VBox problems = new VBox();
        problems.setStyle("-fx-background-color: " + PANEL_BG + ";");

        HBox item1 = createProblemItem("✕", "#f44747", "Property 'postCount' does not exist on type 'User'.", "UserProfile.tsx:38:34");
        HBox item2 = createProblemItem("⚠", "#cca700", "'fetchUserData' is defined but never used.", "api.ts:12:14");

        problems.getChildren().addAll(item1, item2);
        return problems;
    }

    private HBox createProblemItem(String icon, String color, String message, String location) {
        HBox item = new HBox(6);
        item.setPadding(new Insets(2, 12, 2, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-cursor: hand;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: " + HOVER_BG + ";"));
        item.setOnMouseExited(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));

        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(Color.web(color));
        iconLabel.setFont(Font.font(14));

        Label msgLabel = new Label(message);
        msgLabel.setTextFill(Color.web("#cccccc"));
        msgLabel.setFont(Font.font("Segoe UI", 12));
        HBox.setHgrow(msgLabel, Priority.ALWAYS);

        Label locLabel = new Label(location);
        locLabel.setTextFill(Color.web("#6e6e6e"));
        locLabel.setFont(Font.font("Segoe UI", 11));

        item.getChildren().addAll(iconLabel, msgLabel, locLabel);
        return item;
    }

    private VBox createOutputView() {
        VBox output = new VBox();
        output.setStyle("-fx-background-color: " + PANEL_BG + ";");
        output.setPadding(new Insets(4, 12, 4, 12));

        String[] lines = {
                "[Info  - 10:23:45] TypeScript server started.",
                "[Info  - 10:23:46] Loading project: /home/user/my-project/tsconfig.json",
                "[Info  - 10:23:46] Project loaded. 47 files.",
                "[Info  - 10:24:01] File watcher: src/components/UserProfile.tsx changed."
        };

        for (String line : lines) {
            Label lineLabel = new Label(line);
            lineLabel.setTextFill(Color.web("#969696"));
            lineLabel.setFont(Font.font("Consolas", 12));
            lineLabel.setPrefHeight(18);
            output.getChildren().add(lineLabel);
        }

        return output;
    }

    private VBox createDebugConsoleView() {
        VBox debug = new VBox();
        debug.setStyle("-fx-background-color: " + PANEL_BG + ";");
        debug.setPadding(new Insets(4, 12, 4, 12));

        Label message = new Label("Debug console is not active. Start a debug session to use the debug console.");
        message.setTextFill(Color.web("#969696"));
        message.setFont(Font.font("Consolas", 12));
        debug.getChildren().add(message);

        return debug;
    }

    private VBox createTerminalView() {
        VBox terminal = new VBox();
        terminal.setStyle("-fx-background-color: " + PANEL_BG + ";");
        terminal.setPadding(new Insets(4, 8, 4, 8));

        String[][] terminalLines = {
                {"#6a9955", "user@machine"}, {"#cccccc", ":"}, {"#569cd6", "~/my-project"}, {"#cccccc", "$ "}, {"#d4d4d4", "npm run dev"},
                {},
                {"#cccccc", "> my-project@1.0.0 dev"},
                {"#cccccc", "> vite"},
                {},
                {"#4ec9b0", "  VITE v5.1.4  ready in 342 ms"},
                {},
                {"#cccccc", "  ➜  Local:   "}, {"#3794ff", "http://localhost:5173/"},
                {"#cccccc", "  ➜  Network: "}, {"#3794ff", "http://192.168.1.100:5173/"},
                {"#cccccc", "  ➜  press "}, {"#cccccc", "h + enter"}, {"#cccccc", " to show help"},
                {},
                {"#6a9955", "user@machine"}, {"#cccccc", ":"}, {"#569cd6", "~/my-project"}, {"#cccccc", "$ "}
        };

        for (String[] line : terminalLines) {
            HBox lineBox = new HBox();
            lineBox.setPrefHeight(20);
            lineBox.setAlignment(Pos.CENTER_LEFT);

            if (line.length == 0) {
                Label space = new Label(" ");
                space.setFont(Font.font("Consolas", 13));
                lineBox.getChildren().add(space);
            } else {
                for (int i = 0; i < line.length; i += 2) {
                    String color = line[i];
                    String text = line[i + 1];
                    Label token = new Label(text);
                    token.setFont(Font.font("Consolas", 13));
                    token.setTextFill(Color.web(color));
                    lineBox.getChildren().add(token);
                }
            }
            terminal.getChildren().add(lineBox);
        }

        // 闪烁光标
        Label cursor = new Label("█");
        cursor.setTextFill(Color.web("#cccccc"));
        cursor.setFont(Font.font("Consolas", 13));

        HBox lastLine = new HBox();
        lastLine.setPrefHeight(20);
        lastLine.setAlignment(Pos.CENTER_LEFT);
        lastLine.getChildren().add(cursor);
        terminal.getChildren().add(lastLine);

        // 光标闪烁动画
        javafx.animation.FadeTransition blink = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), cursor);
        blink.setFromValue(1.0);
        blink.setToValue(0.0);
        blink.setCycleCount(javafx.animation.Animation.INDEFINITE);
        blink.setAutoReverse(true);
        blink.play();

        return terminal;
    }

    // ==================== 状态栏 ====================

    private HBox createStatusBar() {
        HBox statusbar = new HBox();
        statusbar.setStyle("-fx-background-color: " + STATUSBAR_BG + ";");
        statusbar.setPrefHeight(22);
        statusbar.setAlignment(Pos.CENTER);

        HBox leftItems = new HBox();
        leftItems.setAlignment(Pos.CENTER_LEFT);
        leftItems.getChildren().addAll(
                createStatusItem("🔀 main"),
                createStatusItem("↻ 0↓ 1↑"),
                createStatusItem("✕ 1"),
                createStatusItem("⚠ 1")
        );

        HBox rightItems = new HBox();
        rightItems.setAlignment(Pos.CENTER_RIGHT);
        rightItems.getChildren().addAll(
                createStatusItem("Ln 28, Col 45"),
                createStatusItem("Spaces: 2"),
                createStatusItem("UTF-8"),
                createStatusItem("LF"),
                createStatusItem("{ } TypeScript JSX"),
                createStatusItem("🔔")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        statusbar.getChildren().addAll(leftItems, spacer, rightItems);
        return statusbar;
    }

    private Label createStatusItem(String text) {
        Label item = new Label(text);
        item.setTextFill(Color.WHITE);
        item.setFont(Font.font("Segoe UI", 12));
        item.setPadding(new Insets(0, 8, 0, 8));
        item.setPrefHeight(22);
        item.setAlignment(Pos.CENTER);
        item.setStyle("-fx-cursor: hand;");
        item.setOnMouseEntered(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: rgba(255,255,255,0.12);"));
        item.setOnMouseExited(e -> item.setStyle("-fx-cursor: hand; -fx-background-color: transparent;"));
        return item;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
