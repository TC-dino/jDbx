package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Map;

/**
 * 数据库比较控制器
 */
public class DatabaseCompareController {

    @FXML
    private ComboBox<Connection> sourceCombo;

    @FXML
    private ComboBox<Connection> targetCombo;

    @FXML
    private TextArea summaryArea;

    @FXML
    private TabPane resultTabPane;

    @FXML
    private ListView<String> onlyInSourceList;

    @FXML
    private ListView<String> onlyInTargetList;

    @FXML
    private ListView<String> differencesList;

    @FXML
    private ListView<String> identicalList;

    @FXML
    private TextArea scriptArea;

    private Map<String, Connection> connections;
    private DatabaseComparator.CompareResult compareResult;
    private Connection sourceConnection;
    private Connection targetConnection;

    @FXML
    public void initialize() {
    }

    /**
     * 设置连接列表
     */
    public void setConnections(Map<String, Connection> connections) {
        this.connections = connections;

        sourceCombo.setItems(FXCollections.observableArrayList(connections.values()));
        targetCombo.setItems(FXCollections.observableArrayList(connections.values()));

        // 设置单元格工厂
        sourceCombo.setCellFactory(param -> new ListCell<Connection>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getDatabaseType() + ")");
            }
        });
        sourceCombo.setButtonCell(new ListCell<Connection>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getDatabaseType() + ")");
            }
        });

        targetCombo.setCellFactory(param -> new ListCell<Connection>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getDatabaseType() + ")");
            }
        });
        targetCombo.setButtonCell(new ListCell<Connection>() {
            @Override
            protected void updateItem(Connection item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName() + " (" + item.getDatabaseType() + ")");
            }
        });
    }

    /**
     * 执行比较
     */
    @FXML
    private void onCompare() {
        sourceConnection = sourceCombo.getValue();
        targetConnection = targetCombo.getValue();

        if (sourceConnection == null || targetConnection == null) {
            summaryArea.setText("请选择源数据库和目标数据库");
            return;
        }

        try {
            summaryArea.setText("正在比较...");

            compareResult = DatabaseComparator.compare(sourceConnection, targetConnection);

            // 显示摘要
            summaryArea.setText(compareResult.toSummary());

            // 显示结果
            onlyInSourceList.setItems(FXCollections.observableArrayList(compareResult.getOnlyInSource()));
            onlyInTargetList.setItems(FXCollections.observableArrayList(compareResult.getOnlyInTarget()));
            identicalList.setItems(FXCollections.observableArrayList(compareResult.getIdentical()));

            // 显示差异详情
            java.util.List<String> diffDetails = new java.util.ArrayList<>();
            for (DatabaseComparator.TableDifference diff : compareResult.getDifferences()) {
                diffDetails.add(diff.toDetail());
            }
            differencesList.setItems(FXCollections.observableArrayList(diffDetails));

            scriptArea.clear();

        } catch (Exception e) {
            summaryArea.setText("比较失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 生成同步脚本
     */
    @FXML
    private void onGenerateScript() {
        if (compareResult == null) {
            scriptArea.setText("请先执行比较");
            return;
        }

        try {
            String script = DatabaseComparator.generateSyncScript(compareResult, sourceConnection, targetConnection);
            scriptArea.setText(script);
        } catch (Exception e) {
            scriptArea.setText("生成脚本失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 复制脚本
     */
    @FXML
    private void onCopyScript() {
        String script = scriptArea.getText();
        if (script != null && !script.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(script);
            clipboard.setContent(content);
        }
    }
}
