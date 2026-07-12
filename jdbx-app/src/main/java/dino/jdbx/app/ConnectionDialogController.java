package dino.jdbx.app;

import dino.jdbx.core.api.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * 连接对话框控制器
 */
public class ConnectionDialogController {

    @FXML
    private TextField nameField;

    @FXML
    private ComboBox<DatabasePlugin> typeComboBox;

    @FXML
    private TextField hostField;

    @FXML
    private TextField portField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField databaseField;

    @FXML
    private ColorPicker colorPicker;

    @FXML
    private CheckBox sslCheckBox;

    @FXML
    private ButtonType testButtonType;

    @FXML
    public ButtonType saveButtonType;

    private PluginManager pluginManager;
    private ConnectionManager connectionManager;
    private ConnectionConfig result;
    private DialogPane dialogPane;

    public void setPluginManager(PluginManager pluginManager) {
        this.pluginManager = pluginManager;
        initTypeComboBox();
    }

    public void setConnectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @FXML
    public void initialize() {
        // 设置默认颜色
        colorPicker.setValue(Color.web("#4A90D9"));

        // 设置类型选择变化监听
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                // 更新默认端口
                if (newVal.getDefaultPort() > 0) {
                    portField.setText(String.valueOf(newVal.getDefaultPort()));
                } else {
                    portField.setText("");
                }
            }
        });
    }

    private void initTypeComboBox() {
        if (pluginManager != null) {
            typeComboBox.setItems(FXCollections.observableArrayList(pluginManager.getDatabasePlugins()));
            typeComboBox.setCellFactory(param -> new ListCell<DatabasePlugin>() {
                @Override
                protected void updateItem(DatabasePlugin item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDatabaseType() + " (" + item.getId() + ")");
                    }
                }
            });
            typeComboBox.setButtonCell(new ListCell<DatabasePlugin>() {
                @Override
                protected void updateItem(DatabasePlugin item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item.getDatabaseType());
                    }
                }
            });
        }
    }

    /**
     * 设置对话窗格引用
     */
    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;

        // 获取测试连接按钮并绑定事件
        Button testButton = (Button) dialogPane.lookupButton(testButtonType);
        testButton.setOnAction(event -> testConnection());
    }

    /**
     * 测试连接
     */
    private void testConnection() {
        DatabasePlugin plugin = typeComboBox.getValue();
        if (plugin == null) {
            showAlert(Alert.AlertType.WARNING, "请先选择数据库类型");
            return;
        }

        String host = hostField.getText();
        String port = portField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String database = databaseField.getText();

        // 创建临时配置
        ConnectionConfig config = new ConnectionConfig();
        config.setType(plugin.getId());
        config.setHost(host);
        config.setPort(port.isEmpty() ? 0 : Integer.parseInt(port));
        config.setUsername(username);
        config.setPassword(password);
        config.setDatabase(database);

        try {
            boolean success = connectionManager.testConnection(config);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "连接成功！");
            } else {
                showAlert(Alert.AlertType.ERROR, "连接失败");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "连接失败: " + e.getMessage());
        }
    }

    /**
     * 获取保存的连接配置
     */
    public ConnectionConfig getResult() {
        return result;
    }

    /**
     * 保存连接
     */
    public void saveConnection() {
        DatabasePlugin plugin = typeComboBox.getValue();
        if (plugin == null) {
            showAlert(Alert.AlertType.WARNING, "请选择数据库类型");
            return;
        }

        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "请输入连接名称");
            return;
        }

        // 创建连接配置
        result = new ConnectionConfig();
        result.setName(name);
        result.setType(plugin.getId());
        result.setHost(hostField.getText());
        String port = portField.getText();
        result.setPort(port.isEmpty() ? 0 : Integer.parseInt(port));
        result.setUsername(usernameField.getText());
        result.setPassword(passwordField.getText());
        result.setDatabase(databaseField.getText());
        result.setColor(colorPicker.getValue().toString());
        result.setUseSsl(sslCheckBox.isSelected());
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
