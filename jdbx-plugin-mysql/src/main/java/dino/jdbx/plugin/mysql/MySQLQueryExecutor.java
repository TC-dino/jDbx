package dino.jdbx.plugin.mysql;

import dino.jdbx.core.api.QueryExecutor;
import dino.jdbx.core.api.QueryHistory;
import dino.jdbx.core.api.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 查询执行器
 */
public class MySQLQueryExecutor implements QueryExecutor {

    private final MySQLConnection connection;
    private final List<QueryHistory> history;
    private volatile boolean cancelled;

    public MySQLQueryExecutor(MySQLConnection connection) {
        this.connection = connection;
        this.history = new ArrayList<>();
        this.cancelled = false;
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        cancelled = false;
        long startTime = System.currentTimeMillis();
        try {
            QueryResult result = connection.executeQuery(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyEntry = new QueryHistory(sql, connection.getId());
            historyEntry.setExecutionTime(executionTime);
            historyEntry.setSuccess(!result.hasError());
            historyEntry.setError(result.getError());
            history.add(historyEntry);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录失败历史
            QueryHistory historyEntry = new QueryHistory(sql, connection.getId());
            historyEntry.setExecutionTime(executionTime);
            historyEntry.setSuccess(false);
            historyEntry.setError(e.getMessage());
            history.add(historyEntry);

            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        cancelled = false;
        long startTime = System.currentTimeMillis();
        try {
            int result = connection.executeUpdate(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyEntry = new QueryHistory(sql, connection.getId());
            historyEntry.setExecutionTime(executionTime);
            historyEntry.setSuccess(true);
            historyEntry.setAffectedRows(result);
            history.add(historyEntry);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录失败历史
            QueryHistory historyEntry = new QueryHistory(sql, connection.getId());
            historyEntry.setExecutionTime(executionTime);
            historyEntry.setSuccess(false);
            historyEntry.setError(e.getMessage());
            history.add(historyEntry);

            throw e;
        }
    }

    @Override
    public int[] executeBatch(List<String> sqlList) throws Exception {
        cancelled = false;
        int[] results = new int[sqlList.size()];
        for (int i = 0; i < sqlList.size(); i++) {
            if (cancelled) {
                break;
            }
            try {
                results[i] = connection.executeUpdate(sqlList.get(i));
            } catch (Exception e) {
                results[i] = -1;
            }
        }
        return results;
    }

    @Override
    public List<QueryHistory> getHistory() {
        return history;
    }

    @Override
    public void clearHistory() {
        history.clear();
    }

    @Override
    public void cancelQuery() {
        cancelled = true;
    }
}
