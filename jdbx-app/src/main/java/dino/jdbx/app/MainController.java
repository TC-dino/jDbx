package dino.jdbx.app;

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

    @FXML
    public void initialize() {
        // 初始化连接树
        initConnectionTree();
    }

    private void initConnectionTree() {
        TreeItem<String> root = new TreeItem<>("连接");
        root.setExpanded(true);

        // 这里后续会从ConnectionManager加载连接
        TreeItem<String> mysqlItem = new TreeItem<>("MySQL");
        TreeItem<String> postgresqlItem = new TreeItem<>("PostgreSQL");
        TreeItem<String> sqliteItem = new TreeItem<>("SQLite");

        root.getChildren().addAll(mysqlItem, postgresqlItem, sqliteItem);
        connectionTree.setRoot(root);
    }

    @FXML
    private void onNewQuery() {
        // 新建查询标签页
        Tab tab = new Tab("查询 " + (tabPane.getTabs().size() + 1));
        tab.setContent(new Label("SQL 编辑器"));
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
        System.out.println("插件管理");
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
        alert.setContentText("jDbx - 轻量级中间件管理工具\n版本: 1.0.0");
        alert.showAndWait();
    }
}