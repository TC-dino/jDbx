package dino.jdbx.app;

import dino.jdbx.app.web.JdbxJsBridge;
import dino.jdbx.app.web.WebJson;
import dino.jdbx.app.web.WebPanel;
import dino.jdbx.app.web.WebUiHost;
import dino.jdbx.core.api.*;
import dino.jdbx.core.connection.DefaultConnectionManager;
import dino.jdbx.core.config.DefaultConfigManager;
import dino.jdbx.core.history.QueryHistoryManager;
import dino.jdbx.core.plugin.DefaultPluginContext;
import dino.jdbx.core.plugin.DefaultPluginManager;
import dino.jdbx.core.theme.DefaultThemeManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.*;

/**
 * 主窗口控制器 — Beekeeper 风格侧边栏 + 工作区布局
 */
public class MainController implements WebUiHost {

    @FXML private SplitPane mainSplitPane;
    @FXML private VBox sidebarPane;
    @FXML private VBox activityBar;
    @FXML private Button activityConnectionsBtn;
    @FXML private Button activityHistoryBtn;
    @FXML private Button activitySettingsBtn;
    @FXML private Button toggleSidebarButton;
    @FXML private StackPane sidebarWebHost;
    @FXML private StackPane workspaceStack;
    @FXML private StackPane welcomeHost;
    @FXML private TabPane tabPane;
    @FXML private HBox statusBar;
    @FXML private Label statusLabel;
    @FXML private Label connectionLabel;
    @FXML private Label databaseLabel;
    @FXML private Label queryTimeLabel;
    @FXML private Button themeToggleButton;

    private PluginManager pluginManager;
    private ConnectionManager connectionManager;
    private ConfigManager configManager;
    private ThemeManager themeManager;
    private QueryHistoryManager historyManager;

    private WebPanel sidebarWeb;
    private WebPanel welcomeWeb;
    private WebPanel historyWeb;
    private WebPanel connectionWeb;
    private Stage historyStage;
    private Stage connectionStage;
    private ConnectionConfig editingConnection;
    private final Map<String, List<WebJson.EntityGroup>> entitiesByConnection = new HashMap<>();
    private String selectedConnectionId;

    private final Map<String, Connection> activeConnections = new HashMap<>();
    private final Set<String> failedConnectionIds = new HashSet<>();
    private final Set<String> connectingIds = new HashSet<>();

    private Connection currentConnection;
    private Stage stage;
    private boolean sidebarCollapsed;
    private double lastSidebarDivider = 0.22;

    @FXML
    public void initialize() {
        configManager = new DefaultConfigManager();
        themeManager = new DefaultThemeManager();
        pluginManager = new DefaultPluginManager();
        connectionManager = new DefaultConnectionManager(pluginManager);
        historyManager = new QueryHistoryManager();

        PluginContext context = new DefaultPluginContext(connectionManager, configManager, themeManager);
        ((DefaultPluginManager) pluginManager).setContext(context);
        pluginManager.loadPlugins();

        sidebarWeb = new WebPanel();
        if (sidebarWebHost != null) {
            sidebarWebHost.getChildren().setAll(sidebarWeb);
            sidebarWeb.loadPage("/dino/jdbx/app/web/sidebar.html", new JdbxJsBridge(this));
        }

        welcomeWeb = new WebPanel();
        if (welcomeHost != null) {
            welcomeHost.getChildren().setAll(welcomeWeb);
            welcomeWeb.loadPage("/dino/jdbx/app/web/welcome.html", new JdbxJsBridge(this));
        }

        if (tabPane != null) {
            tabPane.getTabs().addListener((javafx.collections.ListChangeListener<Tab>) change -> updateWelcomeVisibility());
        }

        statusLabel.setText("就绪");
        connectionLabel.setText("未连接");
        updateThemeButtonLabel();
        setStatusBarConnected(false, null);
        updateWelcomeVisibility();
    }

    private void updateWelcomeVisibility() {
        boolean empty = tabPane == null || tabPane.getTabs().isEmpty();
        if (welcomeHost != null) {
            welcomeHost.setVisible(empty);
            welcomeHost.setManaged(empty);
        }
        if (tabPane != null) {
            tabPane.setVisible(!empty);
            tabPane.setMouseTransparent(empty);
        }
    }

    public void onStageReady(Stage stage) {
        this.stage = stage;
        String themeKey = configManager.getTheme();
        if (themeKey != null && !themeKey.isBlank()) {
            try {
                themeManager.setTheme(ThemeManager.Theme.valueOf(themeKey.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
                // keep default theme
            }
        }
        applyTheme();
        updateThemeButtonLabel();
    }

    // ── WebUiHost ────────────────────────────────────────────────────────────

    @Override
    public void webNewConnection() {
        onNewConnection();
    }

    @Override
    public void webConnect(String connectionId) {
        ConnectionConfig config = connectionManager.getConnection(connectionId);
        if (config != null) {
            openConnection(config);
        }
    }

    @Override
    public void webConnectionContext(String connectionId) {
        ConnectionConfig config = connectionManager.getConnection(connectionId);
        if (config == null) {
            return;
        }
        selectedConnectionId = connectionId;
        ContextMenu menu = createConnectionContextMenu(config);
        if (sidebarWebHost != null) {
            menu.show(sidebarWebHost, Side.RIGHT, 0, 0);
        }
    }

    @Override
    public void webOpenTable(String connectionId, String tableName, String kind) {
        if ("view".equalsIgnoreCase(kind)) {
            showViewStructure(new ViewInfo(connectionId, null, tableName));
        } else {
            openTableData(new TableInfo(connectionId, null, tableName));
        }
    }

    @Override
    public void webRequestConnections() {
        pushSidebarData();
    }

    @Override
    public void webRequestHistory() {
        if (historyWeb == null) {
            return;
        }
        historyWeb.callJs("renderHistory(" + WebJson.historyJson(historyManager.getAllHistory()) + ")");
    }

    @Override
    public void webClearHistory() {
        historyManager.clearHistory();
        if (historyWeb != null) {
            historyWeb.callJs("renderHistory(" + WebJson.historyJson(List.of()) + ")");
        }
    }

    @Override
    public void webReplaySql(String sql) {
        QueryTabController controller = openQueryTab(currentConnection);
        if (controller != null && sql != null) {
            controller.setSql(sql);
        }
        webCloseHistory();
        statusLabel.setText("已从历史回放查询");
    }

    @Override
    public void webCloseHistory() {
        if (historyStage != null) {
            historyStage.hide();
        }
    }

    @Override
    public void webRequestPlugins() {
        if (connectionWeb != null) {
            connectionWeb.callJs("setPlugins(" + WebJson.pluginsJson(pluginManager.getDatabasePlugins()) + ")");
            connectionWeb.callJs("setConnectionForm(" + WebJson.connectionFormJson(editingConnection) + ")");
        }
    }

    @Override
    public void webSaveConnection(String json) {
        try {
            ConnectionConfig config = WebJson.parseConnection(json);
            if (editingConnection != null) {
                config.setId(editingConnection.getId());
                config.setCreatedAt(editingConnection.getCreatedAt());
            }
            connectionManager.saveConnection(config);
            pushSidebarData();
            webCloseConnectionDialog();
            statusLabel.setText("连接已保存: " + config.getName());
        } catch (Exception e) {
            if (connectionWeb != null) {
                connectionWeb.callJs("setTestResult(false, " + WebJson.jsString(e.getMessage()) + ")");
            }
        }
    }

    @Override
    public void webTestConnection(String json) {
        try {
            ConnectionConfig config = WebJson.parseConnection(json);
            boolean ok = connectionManager.testConnection(config);
            if (connectionWeb != null) {
                connectionWeb.callJs("setTestResult(" + ok + ", " + WebJson.jsString(ok ? "Connection OK" : "Connection failed") + ")");
            }
        } catch (Exception e) {
            if (connectionWeb != null) {
                connectionWeb.callJs("setTestResult(false, " + WebJson.jsString("Test error") + ")");
            }
        }
    }

    @Override
    public void webCloseConnectionDialog() {
        if (connectionStage != null) {
            connectionStage.hide();
        }
        editingConnection = null;
    }

    @Override
    public void webNewQuery() {
        onNewQuery();
    }

    private void pushSidebarData() {
        if (sidebarWeb == null) {
            return;
        }
        String json = WebJson.connectionsJson(
                connectionManager.getAllConnections(),
                activeConnections,
                failedConnectionIds,
                entitiesByConnection);
        sidebarWeb.callJs("renderConnections(" + json + ")");
    }

    private void loadEntities(ConnectionConfig config, Connection conn) {
        List<WebJson.EntityGroup> groups = new ArrayList<>();
        addEntityGroups(groups, conn);
        entitiesByConnection.put(config.getId(), groups);
    }

    private void addEntityGroups(List<WebJson.EntityGroup> groups, Connection conn) {
        try {
            List<String> tables = conn.getTables();
            if (tables != null && !tables.isEmpty()) {
                List<WebJson.EntityItem> items = new ArrayList<>();
                for (String tableName : tables) {
                    items.add(new WebJson.EntityItem(tableName, "table"));
                }
                groups.add(new WebJson.EntityGroup("表 (" + tables.size() + ")", items));
            }
        } catch (Exception ignored) {
            // skip tables on failure
        }

        try {
            List<String> views = conn.getViews();
            if (views != null && !views.isEmpty()) {
                List<WebJson.EntityItem> items = new ArrayList<>();
                for (String viewName : views) {
                    items.add(new WebJson.EntityItem(viewName, "view"));
                }
                groups.add(new WebJson.EntityGroup("视图 (" + views.size() + ")", items));
            }
        } catch (Exception ignored) {
            // skip views on failure
        }
    }

    private String resolveConnectionColor(ConnectionConfig config) {
        if (config.getColor() != null && !config.getColor().isBlank()) {
            return config.getColor();
        }
        if (config.getType() == null) {
            return "#EFC524";
        }
        return switch (config.getType().toLowerCase(Locale.ROOT)) {
            case "mysql" -> "#00758F";
            case "postgresql" -> "#336791";
            case "sqlite" -> "#0F7B8A";
            case "redis" -> "#DC382D";
            case "mongodb" -> "#47A248";
            case "elasticsearch" -> "#FED10A";
            default -> "#EFC524";
        };
    }

    private void setStatusBarConnected(boolean connected, ConnectionConfig config) {
        if (statusBar == null) {
            return;
        }
        statusBar.getStyleClass().removeAll("disconnected");
        if (!connected || config == null) {
            statusBar.getStyleClass().add("disconnected");
            statusBar.setStyle("");
            return;
        }
        String color = resolveConnectionColor(config);
        statusBar.setStyle("-fx-background-color: " + color + ";");
    }

    @FXML
    private void onActivityConnections() {
        setActivityActive(activityConnectionsBtn);
        if (sidebarCollapsed) {
            onToggleSidebar();
        }
        sidebarPane.setVisible(true);
        sidebarPane.setManaged(true);
    }

    @FXML
    private void onActivityHistory() {
        setActivityActive(activityHistoryBtn);
        onQueryHistory();
        setActivityActive(activityConnectionsBtn);
    }

    @FXML
    private void onActivitySettings() {
        setActivityActive(activitySettingsBtn);
        onSettings();
        setActivityActive(activityConnectionsBtn);
    }

    private void setActivityActive(Button active) {
        for (Button btn : new Button[]{activityConnectionsBtn, activityHistoryBtn, activitySettingsBtn}) {
            if (btn == null) {
                continue;
            }
            btn.getStyleClass().remove("active");
        }
        if (active != null && !active.getStyleClass().contains("active")) {
            active.getStyleClass().add("active");
        }
    }

    private ContextMenu createConnectionContextMenu(ConnectionConfig config) {
        ContextMenu menu = new ContextMenu();
        boolean connected = activeConnections.containsKey(config.getId());

        MenuItem connectOrDisconnect = new MenuItem(connected ? "断开" : "连接");
        connectOrDisconnect.setOnAction(e -> {
            if (connected) {
                disconnectConnection(config);
            } else {
                openConnection(config);
            }
        });

        MenuItem newQuery = new MenuItem("新建查询");
        newQuery.setOnAction(e -> {
            Connection conn = activeConnections.get(config.getId());
            if (conn != null) {
                currentConnection = conn;
                openQueryTab(conn);
            } else {
                statusLabel.setText("请先连接: " + config.getName());
            }
        });

        MenuItem edit = new MenuItem("编辑");
        edit.setOnAction(e -> showConnectionDialog(config));

        MenuItem delete = new MenuItem("删除");
        delete.setOnAction(e -> deleteConnection(config));

        MenuItem refresh = new MenuItem("刷新");
        refresh.setOnAction(e -> refreshConnectionNode(config));

        menu.getItems().addAll(connectOrDisconnect, newQuery, edit, delete, refresh);
        return menu;
    }

    private ContextMenu createTableContextMenu(TableInfo tableInfo) {
        ContextMenu menu = new ContextMenu();

        MenuItem viewData = new MenuItem("查看数据");
        viewData.setOnAction(e -> openTableData(tableInfo));

        MenuItem viewStructure = new MenuItem("查看结构");
        viewStructure.setOnAction(e -> showTableStructure(tableInfo));

        MenuItem generateSelect = new MenuItem("生成 SELECT *");
        generateSelect.setOnAction(e -> {
            Connection conn = activeConnections.get(tableInfo.connectionId);
            if (conn == null) {
                statusLabel.setText("连接未打开");
                return;
            }
            String sql = "SELECT * FROM " + tableInfo.tableName;
            QueryTabController controller = openQueryTab(conn);
            controller.setSql(sql);
            statusLabel.setText("已生成查询");
        });

        menu.getItems().addAll(viewData, viewStructure, generateSelect);
        return menu;
    }

    // ── Connect / disconnect ─────────────────────────────────────────────────

    private void openConnection(ConnectionConfig config) {
        if (connectingIds.contains(config.getId())) {
            return;
        }
        connectingIds.add(config.getId());
        failedConnectionIds.remove(config.getId());
        statusLabel.setText("正在连接: " + config.getName() + "...");

        Task<Connection> task = new Task<>() {
            @Override
            protected Connection call() throws Exception {
                return connectionManager.connect(config);
            }
        };

        task.setOnSucceeded(e -> {
            connectingIds.remove(config.getId());
            Connection conn = task.getValue();
            activeConnections.put(config.getId(), conn);
            currentConnection = conn;
            failedConnectionIds.remove(config.getId());

            connectionLabel.setText(config.getName());
            databaseLabel.setText(conn.getCurrentDatabase() != null ? conn.getCurrentDatabase() : "");
            statusLabel.setText("已连接: " + config.getName());
            setStatusBarConnected(true, config);

            loadEntities(config, conn);
            pushSidebarData();
            openQueryTab(conn);
        });

        task.setOnFailed(e -> {
            connectingIds.remove(config.getId());
            failedConnectionIds.add(config.getId());
            Throwable ex = task.getException();
            String msg = ex != null ? ex.getMessage() : "未知错误";
            statusLabel.setText("连接失败: " + msg);
            pushSidebarData();
        });

        Thread thread = new Thread(task, "jdbx-connect-" + config.getName());
        thread.setDaemon(true);
        thread.start();
    }

    private void disconnectConnection(ConnectionConfig config) {
        try {
            connectionManager.closeConnection(config.getId());
        } catch (Exception ignored) {
            // best effort
        }
        activeConnections.remove(config.getId());
        failedConnectionIds.remove(config.getId());
        entitiesByConnection.remove(config.getId());

        if (currentConnection != null && config.getId().equals(currentConnection.getId())) {
            currentConnection = null;
            connectionLabel.setText("未连接");
            databaseLabel.setText("");
            setStatusBarConnected(false, null);
        }

        pushSidebarData();
        statusLabel.setText("已断开: " + config.getName());
    }

    private void refreshConnectionNode(ConnectionConfig config) {
        Connection conn = activeConnections.get(config.getId());
        if (conn != null && conn.isConnected()) {
            loadEntities(config, conn);
        } else {
            entitiesByConnection.remove(config.getId());
        }
        pushSidebarData();
        statusLabel.setText("已刷新: " + config.getName());
    }

    private void deleteConnection(ConnectionConfig config) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("删除连接");
        alert.setHeaderText(null);
        alert.setContentText("确定删除连接 \"" + config.getName() + "\" 吗？");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }
        connectionManager.deleteConnection(config.getId());
        activeConnections.remove(config.getId());
        failedConnectionIds.remove(config.getId());
        entitiesByConnection.remove(config.getId());
        if (currentConnection != null && config.getId().equals(currentConnection.getId())) {
            currentConnection = null;
            connectionLabel.setText("未连接");
            databaseLabel.setText("");
            setStatusBarConnected(false, null);
        }
        pushSidebarData();
        statusLabel.setText("已删除连接: " + config.getName());
    }

    // ── Table / view tabs ────────────────────────────────────────────────────

    private void openTableData(TableInfo tableInfo) {
        Connection conn = activeConnections.get(tableInfo.connectionId);
        if (conn == null) {
            statusLabel.setText("连接未打开");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("table-data.fxml"));
            Parent tableDataRoot = loader.load();

            TableDataController controller = loader.getController();
            controller.setTable(conn, tableInfo.tableName);

            Tab tab = new Tab(tableInfo.tableName);
            tab.setContent(tableDataRoot);
            tab.setClosable(true);

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            updateWelcomeVisibility();
            statusLabel.setText("已打开表: " + tableInfo.tableName);
        } catch (Exception e) {
            statusLabel.setText("打开表数据失败: " + e.getMessage());
        }
    }

    private void showTableStructure(TableInfo tableInfo) {
        Connection conn = activeConnections.get(tableInfo.connectionId);
        if (conn == null) {
            statusLabel.setText("连接未打开");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("table-structure.fxml"));
            DialogPane dialogPane = loader.load();

            TableStructureController controller = loader.getController();
            controller.setTable(conn, tableInfo.tableName);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("表结构 - " + tableInfo.tableName);
            dialog.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("显示表结构失败: " + e.getMessage());
        }
    }

    private void showViewStructure(ViewInfo viewInfo) {
        statusLabel.setText("视图: " + viewInfo.viewName);
    }

    // ── Query tab ────────────────────────────────────────────────────────────

    private QueryTabController openQueryTab(Connection conn) {
        return openQueryTab(conn, null);
    }

    private QueryTabController openQueryTab(Connection conn, String sql) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("query-tab.fxml"));
            Parent queryRoot = loader.load();

            QueryTabController controller = loader.getController();
            if (conn != null) {
                controller.setConnection(conn);
                currentConnection = conn;
                connectionLabel.setText(conn.getName() != null ? conn.getName() : conn.getDatabaseType());
                databaseLabel.setText(conn.getCurrentDatabase() != null ? conn.getCurrentDatabase() : "");
            }
            controller.setHistoryManager(historyManager);
            controller.setMaxRows(configManager.getQueryConfig().getMaxRows());
            controller.setStatusListener((message, millis) -> {
                statusLabel.setText(message);
                if (millis >= 0) {
                    queryTimeLabel.setText(millis + " ms");
                } else {
                    queryTimeLabel.setText("");
                }
            });

            String engine = configManager.getString("editor.engine", "native");
            controller.setEditorEngine(engine);

            if (sql != null) {
                controller.setSql(sql);
            }

            String tabName;
            if (conn != null) {
                tabName = conn.getName() != null ? conn.getName() : conn.getDatabaseType();
            } else {
                tabName = "查询 " + (tabPane.getTabs().size() + 1);
            }

            Tab tab = new Tab(tabName);
            tab.setContent(queryRoot);
            tab.setClosable(true);

            Connection tabConn = conn;
            tab.setOnClosed(event -> {
                if (tabConn != null && currentConnection == tabConn) {
                    // keep connection alive; only clear if no other tab uses it
                }
            });

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            updateWelcomeVisibility();
            return controller;
        } catch (Exception e) {
            statusLabel.setText("创建查询编辑器失败: " + e.getMessage());
            return null;
        }
    }

    // ── Sidebar / theme ──────────────────────────────────────────────────────

    @FXML
    private void onToggleSidebar() {
        if (sidebarCollapsed) {
            mainSplitPane.setDividerPositions(lastSidebarDivider);
            toggleSidebarButton.setText("«");
            sidebarCollapsed = false;
        } else {
            double[] positions = mainSplitPane.getDividerPositions();
            if (positions.length > 0 && positions[0] > 0.01) {
                lastSidebarDivider = positions[0];
            }
            mainSplitPane.setDividerPositions(0);
            toggleSidebarButton.setText("»");
            sidebarCollapsed = true;
        }
    }

    @FXML
    private void onToggleTheme() {
        themeManager.toggleTheme();
        applyTheme();
        configManager.setTheme(themeManager.getCurrentTheme().name().toLowerCase(Locale.ROOT));
        updateThemeButtonLabel();
        statusLabel.setText("主题: " + themeManager.getCurrentTheme().name());
    }

    private void updateThemeButtonLabel() {
        if (themeToggleButton != null) {
            themeToggleButton.setText(switch (themeManager.getCurrentTheme()) {
                case LIGHT -> "浅色";
                case DARK -> "深色";
                case SYSTEM -> "系统";
            });
        }
    }

    private void applyTheme() {
        if (tabPane != null && tabPane.getScene() != null) {
            applyThemeToScene(tabPane.getScene());
        }
        if (historyStage != null && historyStage.getScene() != null) {
            applyThemeToScene(historyStage.getScene());
        }
        if (connectionStage != null && connectionStage.getScene() != null) {
            applyThemeToScene(connectionStage.getScene());
        }
    }

    private void applyThemeToScene(Scene scene) {
        String css = themeManager.getThemeCss();
        if (css != null && scene != null) {
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource(css).toExternalForm());
        }
    }

    // ── Connection dialog ────────────────────────────────────────────────────

    private void showConnectionDialog(ConnectionConfig existing) {
        try {
            editingConnection = existing;
            if (connectionStage == null) {
                connectionStage = new Stage();
                if (stage != null) {
                    connectionStage.initOwner(stage);
                }
                connectionStage.initModality(Modality.NONE);
                connectionStage.setTitle(existing != null ? "编辑连接" : "新建连接");

                connectionWeb = new WebPanel();
                Scene scene = new Scene(connectionWeb, 520, 620);
                applyThemeToScene(scene);
                connectionStage.setScene(scene);
                connectionWeb.loadPage("/dino/jdbx/app/web/connection.html", new JdbxJsBridge(this));

                connectionStage.setOnCloseRequest(e -> {
                    e.consume();
                    webCloseConnectionDialog();
                });
            } else {
                connectionStage.setTitle(existing != null ? "编辑连接" : "新建连接");
            }
            connectionStage.show();
            connectionStage.toFront();
            webRequestPlugins();
        } catch (Exception e) {
            statusLabel.setText("打开连接对话框失败: " + e.getMessage());
        }
    }

    // ── Menu handlers ────────────────────────────────────────────────────────

    @FXML
    private void onNewQuery() {
        if (currentConnection != null && currentConnection.isConnected()) {
            openQueryTab(currentConnection);
        } else {
            openQueryTab(null);
            statusLabel.setText("新建查询（未连接）");
        }
    }

    @FXML
    private void onOpenFile() {
        System.out.println("打开文件");
    }

    @FXML
    private void onImportData() {
        if (currentConnection == null || !currentConnection.isConnected()) {
            showAlert(Alert.AlertType.WARNING, "请先连接到数据库");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("导入数据");
        dialog.setHeaderText("输入目标表名");
        dialog.setContentText("表名:");
        if (selectedConnectionId != null) {
            ConnectionConfig selected = connectionManager.getConnection(selectedConnectionId);
            if (selected != null) {
                dialog.setHeaderText("导入到连接: " + selected.getName());
            }
        }

        Optional<String> tableResult = dialog.showAndWait();
        if (tableResult.isEmpty() || tableResult.get().isBlank()) {
            return;
        }
        String tableName = tableResult.get().trim();

        DataImporter.ImportResult result = DataImporter.importFromCsv(
                tabPane.getScene().getWindow(), currentConnection, tableName);

        if (result != null) {
            if (result.hasErrors()) {
                showAlert(Alert.AlertType.WARNING, result.toString() + "\n" + result.getErrorMessage());
            } else {
                showAlert(Alert.AlertType.INFORMATION, result.toString());
                statusLabel.setText("导入完成: " + result.toString());
            }
        }
    }

    @FXML
    private void onExportData() {
        showAlert(Alert.AlertType.INFORMATION, "请先执行查询，然后使用结果表格中的导出功能");
    }

    @FXML
    private void onExit() {
        for (Connection conn : activeConnections.values()) {
            try {
                conn.close();
            } catch (Exception ignored) {
                // ignore
            }
        }
        Platform.exit();
    }

    @FXML
    private void onNewConnection() {
        showConnectionDialog(null);
    }

    @FXML
    private void onManageConnections() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("连接管理");
        dialog.setHeaderText("已保存的连接");

        ListView<ConnectionConfig> listView = new ListView<>();
        listView.getItems().setAll(connectionManager.getAllConnections());
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ConnectionConfig item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + "  (" + item.getType() + " @ " + item.getHost() + ")");
                }
            }
        });
        listView.setPrefHeight(300);
        listView.setPrefWidth(420);

        Button editBtn = new Button("编辑");
        editBtn.setOnAction(e -> {
            ConnectionConfig selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                dialog.close();
                showConnectionDialog(selected);
            }
        });

        Button deleteBtn = new Button("删除");
        deleteBtn.setOnAction(e -> {
            ConnectionConfig selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                deleteConnection(selected);
                listView.getItems().setAll(connectionManager.getAllConnections());
            }
        });

        HBox buttons = new HBox(8, editBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(12, listView, buttons);
        content.setPadding(new Insets(12));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    @FXML
    private void onPluginManager() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("插件管理");
        alert.setHeaderText("已加载的插件");

        StringBuilder content = new StringBuilder();
        for (Plugin plugin : pluginManager.getPlugins()) {
            content.append(plugin.getName())
                    .append(" (")
                    .append(plugin.getVersion())
                    .append(")\n");
        }
        alert.setContentText(content.toString());
        alert.showAndWait();
    }

    @FXML
    private void onQueryHistory() {
        try {
            if (historyStage == null) {
                historyStage = new Stage();
                if (stage != null) {
                    historyStage.initOwner(stage);
                }
                historyStage.initModality(Modality.NONE);
                historyStage.setTitle("查询历史");

                historyWeb = new WebPanel();
                Scene scene = new Scene(historyWeb, 720, 520);
                applyThemeToScene(scene);
                historyStage.setScene(scene);
                historyWeb.loadPage("/dino/jdbx/app/web/history.html", new JdbxJsBridge(this));

                historyStage.setOnCloseRequest(e -> {
                    e.consume();
                    historyStage.hide();
                });
            }
            historyStage.show();
            historyStage.toFront();
            webRequestHistory();
        } catch (Exception e) {
            statusLabel.setText("打开查询历史失败: " + e.getMessage());
        }
    }

    @FXML
    private void onSqlFormatter() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("sql-formatter.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("SQL 格式化工具");
            dialog.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("打开 SQL 格式化工具失败: " + e.getMessage());
        }
    }

    @FXML
    private void onBackupRestore() {
        if (currentConnection == null || !currentConnection.isConnected()) {
            showAlert(Alert.AlertType.WARNING, "请先连接到数据库");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("backup.fxml"));
            DialogPane dialogPane = loader.load();

            BackupController controller = loader.getController();
            controller.setConnection(currentConnection);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("数据库备份/恢复");
            dialog.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("打开备份/恢复工具失败: " + e.getMessage());
        }
    }

    @FXML
    private void onDatabaseCompare() {
        if (activeConnections.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "请先连接到数据库");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("database-compare.fxml"));
            DialogPane dialogPane = loader.load();

            DatabaseCompareController controller = loader.getController();
            controller.setConnections(activeConnections);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("数据库比较");
            dialog.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("打开数据库比较工具失败: " + e.getMessage());
        }
    }

    @FXML
    private void onPerformance() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("performance.fxml"));
            DialogPane dialogPane = loader.load();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("性能监控");
            dialog.showAndWait();
        } catch (Exception e) {
            statusLabel.setText("打开性能监控失败: " + e.getMessage());
        }
    }

    @FXML
    private void onSettings() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("设置");
        dialog.setHeaderText("应用程序设置");

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));

        Label themeLabel = new Label("主题:");
        ComboBox<ThemeManager.Theme> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(ThemeManager.Theme.values());
        themeComboBox.setValue(themeManager.getCurrentTheme());
        HBox themeBox = new HBox(10, themeLabel, themeComboBox);
        themeBox.setAlignment(Pos.CENTER_LEFT);

        Label editorLabel = new Label("SQL 编辑器:");
        ComboBox<String> editorCombo = new ComboBox<>();
        editorCombo.getItems().addAll("native", "webview");
        editorCombo.setValue(configManager.getString("editor.engine", "native"));
        HBox editorBox = new HBox(10, editorLabel, editorCombo);
        editorBox.setAlignment(Pos.CENTER_LEFT);

        content.getChildren().addAll(themeBox, editorBox);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ThemeManager.Theme selectedTheme = themeComboBox.getValue();
            if (selectedTheme != null && selectedTheme != themeManager.getCurrentTheme()) {
                themeManager.setTheme(selectedTheme);
                applyTheme();
                configManager.setTheme(selectedTheme.name().toLowerCase(Locale.ROOT));
                updateThemeButtonLabel();
            }
            String engine = editorCombo.getValue();
            if (engine != null) {
                configManager.set("editor.engine", engine);
                statusLabel.setText("设置已保存（新查询 Tab 将使用 " + engine + " 编辑器）");
            }
        }
    }

    @FXML
    private void onAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 jDbx");
        alert.setHeaderText(null);
        alert.setContentText("jDbx - 轻量级中间件管理工具\n版本: 1.0.0\n\n已加载 "
                + pluginManager.getPlugins().size() + " 个插件");
        alert.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Data types ───────────────────────────────────────────────────────────

    static class TableInfo {
        final String connectionId;
        final String databaseName;
        final String tableName;

        TableInfo(String connectionId, String databaseName, String tableName) {
            this.connectionId = connectionId;
            this.databaseName = databaseName;
            this.tableName = tableName;
        }
    }

    static class ViewInfo {
        final String connectionId;
        final String databaseName;
        final String viewName;

        ViewInfo(String connectionId, String databaseName, String viewName) {
            this.connectionId = connectionId;
            this.databaseName = databaseName;
            this.viewName = viewName;
        }
    }
}
