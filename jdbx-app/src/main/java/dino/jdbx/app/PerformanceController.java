package dino.jdbx.app;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 性能监控控制器
 */
public class PerformanceController {

    @FXML
    private TextArea summaryArea;

    @FXML
    private TableView<PerformanceMonitor.QueryMetric> recentQueriesTable;

    @FXML
    private TableColumn<PerformanceMonitor.QueryMetric, String> timeColumn;

    @FXML
    private TableColumn<PerformanceMonitor.QueryMetric, String> connectionColumn;

    @FXML
    private TableColumn<PerformanceMonitor.QueryMetric, String> sqlColumn;

    @FXML
    private TableColumn<PerformanceMonitor.QueryMetric, String> durationColumn;

    @FXML
    private TableColumn<PerformanceMonitor.QueryMetric, String> statusColumn;

    @FXML
    private TableView<ConnectionStat> connectionStatsTable;

    @FXML
    private TableColumn<ConnectionStat, String> connIdColumn;

    @FXML
    private TableColumn<ConnectionStat, String> queryCountColumn;

    @FXML
    private TableColumn<ConnectionStat, String> successRateColumn;

    @FXML
    private TableColumn<ConnectionStat, String> avgTimeColumn;

    private PerformanceMonitor monitor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @FXML
    public void initialize() {
        monitor = PerformanceMonitor.getInstance();

        setupRecentQueriesTable();
        setupConnectionStatsTable();

        refreshData();
    }

    private void setupRecentQueriesTable() {
        timeColumn.setCellValueFactory(param -> {
            PerformanceMonitor.QueryMetric metric = param.getValue();
            return new SimpleStringProperty(dateFormat.format(metric.getTimestamp()));
        });

        connectionColumn.setCellValueFactory(param -> {
            PerformanceMonitor.QueryMetric metric = param.getValue();
            return new SimpleStringProperty(metric.getConnectionId());
        });

        sqlColumn.setCellValueFactory(param -> {
            PerformanceMonitor.QueryMetric metric = param.getValue();
            String sql = metric.getSql();
            if (sql.length() > 100) {
                sql = sql.substring(0, 100) + "...";
            }
            return new SimpleStringProperty(sql);
        });

        durationColumn.setCellValueFactory(param -> {
            PerformanceMonitor.QueryMetric metric = param.getValue();
            return new SimpleStringProperty(String.valueOf(metric.getExecutionTime()));
        });

        statusColumn.setCellValueFactory(param -> {
            PerformanceMonitor.QueryMetric metric = param.getValue();
            return new SimpleStringProperty(metric.isSuccess() ? "成功" : "失败");
        });
    }

    private void setupConnectionStatsTable() {
        connIdColumn.setCellValueFactory(param -> {
            ConnectionStat stat = param.getValue();
            return new SimpleStringProperty(stat.connectionId);
        });

        queryCountColumn.setCellValueFactory(param -> {
            ConnectionStat stat = param.getValue();
            return new SimpleStringProperty(String.valueOf(stat.queryCount));
        });

        successRateColumn.setCellValueFactory(param -> {
            ConnectionStat stat = param.getValue();
            return new SimpleStringProperty(String.format("%.2f%%", stat.successRate));
        });

        avgTimeColumn.setCellValueFactory(param -> {
            ConnectionStat stat = param.getValue();
            return new SimpleStringProperty(String.format("%.2f ms", stat.avgTime));
        });
    }

    private void refreshData() {
        // 刷新摘要
        summaryArea.setText(monitor.getSummary());

        // 刷新最近查询
        List<PerformanceMonitor.QueryMetric> recentQueries = monitor.getRecentQueries();
        recentQueriesTable.setItems(FXCollections.observableArrayList(recentQueries));

        // 刷新连接统计
        Map<String, PerformanceMonitor.ConnectionMetric> connectionMetrics = monitor.getConnectionMetrics();
        List<ConnectionStat> stats = new ArrayList<>();
        for (Map.Entry<String, PerformanceMonitor.ConnectionMetric> entry : connectionMetrics.entrySet()) {
            PerformanceMonitor.ConnectionMetric metric = entry.getValue();
            ConnectionStat stat = new ConnectionStat();
            stat.connectionId = metric.getConnectionId();
            stat.queryCount = metric.getQueryCount();
            stat.successRate = metric.getSuccessRate();
            stat.avgTime = metric.getAverageExecutionTime();
            stats.add(stat);
        }
        connectionStatsTable.setItems(FXCollections.observableArrayList(stats));
    }

    @FXML
    private void onRefresh() {
        refreshData();
    }

    @FXML
    private void onReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认重置");
        alert.setHeaderText(null);
        alert.setContentText("确定要重置所有性能统计数据吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                monitor.reset();
                refreshData();
            }
        });
    }

    /**
     * 连接统计
     */
    public static class ConnectionStat {
        String connectionId;
        int queryCount;
        double successRate;
        double avgTime;
    }
}
