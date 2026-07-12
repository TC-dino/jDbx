package dino.jdbx.app;

import dino.jdbx.core.api.QueryHistory;
import dino.jdbx.core.history.QueryHistoryManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 查询历史对话框控制器
 */
public class QueryHistoryController {

    @FXML
    private TextField searchField;

    @FXML
    private TableView<QueryHistory> historyTable;

    @FXML
    private TableColumn<QueryHistory, String> timeColumn;

    @FXML
    private TableColumn<QueryHistory, String> connectionColumn;

    @FXML
    private TableColumn<QueryHistory, String> sqlColumn;

    @FXML
    private TableColumn<QueryHistory, String> statusColumn;

    @FXML
    private TableColumn<QueryHistory, String> timeCostColumn;

    @FXML
    private TextArea sqlPreview;

    @FXML
    public ButtonType replayButtonType;

    private QueryHistoryManager historyManager;
    private QueryHistory selectedHistory;
    private ObservableList<QueryHistory> historyData;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        historyManager = new QueryHistoryManager();
        historyData = FXCollections.observableArrayList();

        // 设置表格列
        setupTableColumns();

        // 设置表格选择监听
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedHistory = newVal;
                sqlPreview.setText(newVal.getSql());
            }
        });

        // 加载历史数据
        loadHistory();
    }

    private void setupTableColumns() {
        timeColumn.setCellValueFactory(param -> {
            QueryHistory history = param.getValue();
            String time = history.getExecutedAt() != null ? history.getExecutedAt().format(formatter) : "";
            return new javafx.beans.property.SimpleStringProperty(time);
        });

        connectionColumn.setCellValueFactory(param -> {
            QueryHistory history = param.getValue();
            return new javafx.beans.property.SimpleStringProperty(history.getConnectionId());
        });

        sqlColumn.setCellValueFactory(param -> {
            QueryHistory history = param.getValue();
            String sql = history.getSql();
            // 截断显示
            if (sql != null && sql.length() > 100) {
                sql = sql.substring(0, 100) + "...";
            }
            return new javafx.beans.property.SimpleStringProperty(sql);
        });

        statusColumn.setCellValueFactory(param -> {
            QueryHistory history = param.getValue();
            String status = history.isSuccess() ? "成功" : "失败";
            return new javafx.beans.property.SimpleStringProperty(status);
        });

        timeCostColumn.setCellValueFactory(param -> {
            QueryHistory history = param.getValue();
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(history.getExecutionTime()));
        });

        historyTable.setItems(historyData);
    }

    /**
     * 加载历史数据
     */
    private void loadHistory() {
        List<QueryHistory> history = historyManager.getAllHistory();
        historyData.setAll(history);
    }

    /**
     * 搜索
     */
    @FXML
    private void onSearch() {
        String keyword = searchField.getText();
        List<QueryHistory> results = historyManager.searchHistory(keyword);
        historyData.setAll(results);
    }

    /**
     * 清空历史
     */
    @FXML
    private void onClearHistory() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认清空");
        alert.setHeaderText(null);
        alert.setContentText("确定要清空所有查询历史吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                historyManager.clearHistory();
                historyData.clear();
                sqlPreview.clear();
            }
        });
    }

    /**
     * 获取选中的历史记录
     */
    public QueryHistory getSelectedHistory() {
        return selectedHistory;
    }

    /**
     * 获取历史管理器
     */
    public QueryHistoryManager getHistoryManager() {
        return historyManager;
    }
}
