package dino.jdbx.app;

import dino.jdbx.core.api.*;
import dino.jdbx.core.connection.DefaultConnectionManager;
import dino.jdbx.core.config.DefaultConfigManager;
import dino.jdbx.core.plugin.DefaultPluginContext;
import dino.jdbx.core.plugin.DefaultPluginManager;
import dino.jdbx.core.theme.DefaultThemeManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * 主窗口控制器
 */
public class MainController {

    @FXML
    private TreeView<String> connectionTree;

    @FXML
    private TabPane tabPane;

    @FXML
    private Label statusLabel;

    @FXML
    private Label connectionLabel;

    @FXML
    private Label databaseLabel;

    @FXML
    private Label queryTimeLabel;

    // 核心管理器
    private PluginManager pluginManager;
    private ConnectionManager connectionManager;
    private ConfigManager configManager;
    private ThemeManager themeManager;

    @FXML
    public void initialize() {
        // 初始化管理器
        configManager = new DefaultConfigManager();
        themeManager = new DefaultThemeManager();
        pluginManager = new DefaultPluginManager();
        connectionManager = new DefaultConnectionManager(pluginManager);

        // 初始化插件上下文
        PluginContext context = new DefaultPluginContext(connectionManager, configManager, themeManager);
        ((DefaultPluginManager) pluginManager).setContext(context);

        // 加载插件
        pluginManager.loadPlugins();

        // 初始化连接树
        initConnectionTree();

        // 更新状态栏
        statusLabel.setText("就绪");
    }

    private void initConnectionTree() {
        TreeItem<String> root = new TreeItem<>("连接");
        root.setExpanded(true);

        // 根据已加载的插件创建连接类型节点
        for (DatabasePlugin plugin : pluginManager.getDatabasePlugins()) {
            TreeItem<String> pluginItem = new TreeItem<>(plugin.getDatabaseType());
            pluginItem.setExpanded(false);

            // 添加该类型的连接
            for (ConnectionConfig config : connectionManager.getAllConnections()) {
                if (config.getType().equalsIgnoreCase(plugin.getId())) {
                    TreeItem<String> connItem = new TreeItem<>(config.getName());
                    pluginItem.getChildren().add(connItem);
                }
            }

            root.getChildren().add(pluginItem);
        }

        connectionTree.setRoot(root);
    }

    @FXML
    private void onNewQuery() {
        // 新建查询标签页
        Tab tab = new Tab("查询 " + (tabPane.getTabs().size() + 1));
        TextArea editor = new TextArea();
        editor.setPromptText("输入 SQL 语句...");
        editor.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        tab.setContent(editor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }

    @FXML
    private void onOpenFile() {
        // 打开文件
        System.out.println("打开文件");
    }

    @FXML
    private void onExit() {
        // 退出应用
        System.exit(0);
    }

    @FXML
    private void onNewConnection() {
        // 新建连接
        System.out.println("新建连接");
    }

    @FXML
    private void onManageConnections() {
        // 连接管理
        System.out.println("连接管理");
    }

    @FXML
    private void onPluginManager() {
        // 插件管理
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
    private void onSettings() {
        // 设置
        System.out.println("设置");
    }

    @FXML
    private void onAbout() {
        // 关于
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于 jDbx");
        alert.setHeaderText(null);
        alert.setContentText("jDbx - 轻量级中间件管理工具\n版本: 1.0.0\n\n已加载 " + pluginManager.getPlugins().size() + " 个插件");
        alert.showAndWait();
    }
}