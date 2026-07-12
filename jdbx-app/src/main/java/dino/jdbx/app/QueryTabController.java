package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.QueryHistory;
import dino.jdbx.core.api.QueryResult;
import dino.jdbx.core.history.QueryHistoryManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Query workspace: editor + results, async execution.
 */
public class QueryTabController {

    @FXML
    private SqlEditorController sqlEditorController;

    @FXML
    private ResultTableController resultTableController;

    private Connection connection;
    private QueryHistoryManager historyManager;
    private int maxRows = 10000;
    private Task<QueryResult> runningTask;
    private BiConsumer<String, Long> statusListener;

    @FXML
    public void initialize() {
        if (sqlEditorController != null) {
            sqlEditorController.setOnExecuteListener(this::executeQuery);
        }
        if (resultTableController != null) {
            resultTableController.setOnReplaySql(sql -> {
                if (sqlEditorController != null) {
                    sqlEditorController.setSql(sql);
                    sqlEditorController.setStatus("已从历史回放");
                }
            });
        }
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
        refreshHistory();
    }

    public void setHistoryManager(QueryHistoryManager historyManager) {
        this.historyManager = historyManager;
        refreshHistory();
    }

    public void setMaxRows(int maxRows) {
        this.maxRows = Math.max(1, maxRows);
    }

    public void setStatusListener(BiConsumer<String, Long> statusListener) {
        this.statusListener = statusListener;
    }

    public void setSql(String sql) {
        if (sqlEditorController != null) {
            sqlEditorController.setSql(sql);
        }
    }

    public void setEditorEngine(String engine) {
        if (sqlEditorController != null) {
            sqlEditorController.setEngine(engine);
        }
    }

    public ResultTableController getResultTableController() {
        return resultTableController;
    }

    public SqlEditorController getSqlEditorController() {
        return sqlEditorController;
    }

    private void refreshHistory() {
        if (resultTableController != null && historyManager != null && connection != null) {
            resultTableController.showHistory(historyManager.getHistoryByConnection(connection.getId()));
        }
    }

    private void executeQuery(String sql, boolean selectedOnly) {
        if (connection == null || !connection.isConnected()) {
            showStatus("请先连接到数据库", -1);
            return;
        }
        if (sql == null || sql.isBlank()) {
            showStatus("SQL 为空", -1);
            return;
        }

        if (runningTask != null && runningTask.isRunning()) {
            runningTask.cancel(true);
        }

        if (resultTableController != null) {
            resultTableController.clear();
        }
        showStatus("执行中...", -1);
        if (sqlEditorController != null) {
            sqlEditorController.setStatus("执行中...");
        }

        final String sqlToRun = sql;
        Task<QueryResult> task = new Task<>() {
            @Override
            protected QueryResult call() throws Exception {
                String upper = sqlToRun.trim().toUpperCase();
                if (upper.startsWith("SELECT") || upper.startsWith("SHOW")
                        || upper.startsWith("DESCRIBE") || upper.startsWith("DESC")
                        || upper.startsWith("EXPLAIN") || upper.startsWith("WITH")
                        || upper.startsWith("PRAGMA")) {
                    return truncate(connection.executeQuery(sqlToRun));
                }
                int affected = connection.executeUpdate(sqlToRun);
                return new SimpleQueryResult(affected, 0);
            }
        };

        runningTask = task;
        task.setOnSucceeded(e -> {
            QueryResult result = task.getValue();
            if (resultTableController != null) {
                resultTableController.showResult(result);
            }
            recordHistory(sqlToRun, result);
            if (result.hasError()) {
                showStatus("执行失败: " + result.getError(), result.getExecutionTime());
            } else {
                showStatus(String.format("执行成功 (%d ms)", result.getExecutionTime()), result.getExecutionTime());
            }
            if (sqlEditorController != null) {
                sqlEditorController.setStatus(result.hasError() ? "失败" : "成功");
            }
            refreshHistory();
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            String msg = ex == null ? "未知错误" : ex.getMessage();
            if (resultTableController != null) {
                resultTableController.showError(msg);
            }
            recordHistoryError(sqlToRun, msg);
            showStatus("执行错误: " + msg, -1);
            if (sqlEditorController != null) {
                sqlEditorController.setStatus("错误");
            }
            refreshHistory();
        });
        task.setOnCancelled(e -> showStatus("已取消", -1));

        Thread thread = new Thread(task, "jdbx-query");
        thread.setDaemon(true);
        thread.start();
    }

    private QueryResult truncate(QueryResult result) {
        if (result == null || result.hasError() || !result.isQuery()) {
            return result;
        }
        if (result.getRows() != null && result.getRows().size() > maxRows) {
            return new TruncatedQueryResult(result, maxRows);
        }
        return result;
    }

    private void recordHistory(String sql, QueryResult result) {
        if (historyManager == null || connection == null) {
            return;
        }
        QueryHistory entry = new QueryHistory(sql, connection.getId());
        entry.setSuccess(!result.hasError());
        entry.setError(result.getError());
        entry.setExecutionTime(result.getExecutionTime());
        entry.setAffectedRows(result.isQuery() ? result.getRowCount() : result.getAffectedRows());
        historyManager.addHistory(entry);
    }

    private void recordHistoryError(String sql, String error) {
        if (historyManager == null || connection == null) {
            return;
        }
        QueryHistory entry = new QueryHistory(sql, connection.getId());
        entry.setSuccess(false);
        entry.setError(error);
        historyManager.addHistory(entry);
    }

    private void showStatus(String message, long millis) {
        if (statusListener != null) {
            Platform.runLater(() -> statusListener.accept(message, millis));
        }
        if (sqlEditorController != null) {
            Platform.runLater(() -> sqlEditorController.setStatus(message));
        }
    }

    private static class SimpleQueryResult implements QueryResult {
        private final int affectedRows;
        private final long executionTime;

        SimpleQueryResult(int affectedRows, long executionTime) {
            this.affectedRows = affectedRows;
            this.executionTime = executionTime;
        }

        @Override
        public List<String> getColumnNames() {
            return Collections.emptyList();
        }

        @Override
        public List<String> getColumnTypes() {
            return Collections.emptyList();
        }

        @Override
        public List<Map<String, Object>> getRows() {
            return Collections.emptyList();
        }

        @Override
        public int getRowCount() {
            return 0;
        }

        @Override
        public int getAffectedRows() {
            return affectedRows;
        }

        @Override
        public long getExecutionTime() {
            return executionTime;
        }

        @Override
        public boolean isQuery() {
            return false;
        }

        @Override
        public String getError() {
            return null;
        }

        @Override
        public boolean hasError() {
            return false;
        }
    }

    private static class TruncatedQueryResult implements QueryResult {
        private final QueryResult delegate;
        private final List<Map<String, Object>> rows;

        TruncatedQueryResult(QueryResult delegate, int maxRows) {
            this.delegate = delegate;
            this.rows = new java.util.ArrayList<>(delegate.getRows().subList(0, maxRows));
        }

        @Override
        public List<String> getColumnNames() {
            return delegate.getColumnNames();
        }

        @Override
        public List<String> getColumnTypes() {
            return delegate.getColumnTypes();
        }

        @Override
        public List<Map<String, Object>> getRows() {
            return rows;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getAffectedRows() {
            return delegate.getAffectedRows();
        }

        @Override
        public long getExecutionTime() {
            return delegate.getExecutionTime();
        }

        @Override
        public boolean isQuery() {
            return true;
        }

        @Override
        public String getError() {
            return null;
        }

        @Override
        public boolean hasError() {
            return false;
        }
    }
}
