package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * 备份/恢复控制器
 */
public class BackupController {

    @FXML
    private Label statusLabel;

    private Connection connection;

    @FXML
    public void initialize() {
    }

    /**
     * 设置连接
     */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * 备份结构
     */
    @FXML
    private void onBackupStructure() {
        if (connection == null) {
            statusLabel.setText("未连接数据库");
            return;
        }

        try {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            DatabaseBackup.backupStructure(stage, connection);
            statusLabel.setText("结构备份完成");
        } catch (Exception e) {
            statusLabel.setText("备份失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 备份数据
     */
    @FXML
    private void onBackupData() {
        if (connection == null) {
            statusLabel.setText("未连接数据库");
            return;
        }

        try {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            DatabaseBackup.backupData(stage, connection);
            statusLabel.setText("数据备份完成");
        } catch (Exception e) {
            statusLabel.setText("备份失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 完整备份
     */
    @FXML
    private void onBackupFull() {
        if (connection == null) {
            statusLabel.setText("未连接数据库");
            return;
        }

        try {
            Stage stage = (Stage) statusLabel.getScene().getWindow();
            DatabaseBackup.backupFull(stage, connection);
            statusLabel.setText("完整备份完成");
        } catch (Exception e) {
            statusLabel.setText("备份失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 恢复
     */
    @FXML
    private void onRestore() {
        if (connection == null) {
            statusLabel.setText("未连接数据库");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认恢复");
        alert.setHeaderText(null);
        alert.setContentText("恢复操作将覆盖现有数据，是否继续？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Stage stage = (Stage) statusLabel.getScene().getWindow();
                    DatabaseBackup.restore(stage, connection);
                    statusLabel.setText("恢复完成");
                } catch (Exception e) {
                    statusLabel.setText("恢复失败: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
