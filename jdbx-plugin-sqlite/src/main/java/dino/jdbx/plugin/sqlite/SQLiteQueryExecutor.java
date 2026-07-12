package dino.jdbx.plugin.sqlite;

import dino.jdbx.core.api.QueryExecutor;
import dino.jdbx.core.api.QueryHistory;
import dino.jdbx.core.api.QueryResult;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 查询执行器实现
 */
public class SQLiteQueryExecutor implements QueryExecutor {

    private final SQLiteConnection connection;
    private final List<QueryHistory> history = new ArrayList<>();
    private volatile boolean cancelled = false;

    public SQLiteQueryExecutor(SQLiteConnection connection) {
        this.connection = connection;
    }

    @Override
    public QueryResult executeQuery(String sql) throws Exception {
        cancelled = false;
        long startTime = System.currentTimeMillis();

        try {
            QueryResult result = connection.executeQuery(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyRecord = new QueryHistory(sql, connection.getId());
            historyRecord.setExecutionTime(executionTime);
            historyRecord.setSuccess(!result.hasError());
            historyRecord.setError(result.getError());
            if (!result.isQuery()) {
                historyRecord.setAffectedRows(result.getAffectedRows());
            }
            history.add(0, historyRecord);

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyRecord = new QueryHistory(sql, connection.getId());
            historyRecord.setExecutionTime(executionTime);
            historyRecord.setSuccess(false);
            historyRecord.setError(e.getMessage());
            history.add(0, historyRecord);

            throw e;
        }
    }

    @Override
    public int executeUpdate(String sql) throws Exception {
        cancelled = false;
        long startTime = System.currentTimeMillis();

        try {
            int affectedRows = connection.executeUpdate(sql);
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyRecord = new QueryHistory(sql, connection.getId());
            historyRecord.setExecutionTime(executionTime);
            historyRecord.setSuccess(true);
            historyRecord.setAffectedRows(affectedRows);
            history.add(0, historyRecord);

            return affectedRows;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            // 记录历史
            QueryHistory historyRecord = new QueryHistory(sql, connection.getId());
            historyRecord.setExecutionTime(executionTime);
            historyRecord.setSuccess(false);
            historyRecord.setError(e.getMessage());
            history.add(0, historyRecord);

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
        return new ArrayList<>(history);
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