package dino.jdbx.plugin.sqlite;

import dino.jdbx.core.api.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * SQLite 查询结果实现
 */
public class SQLiteQueryResult implements QueryResult {

    private List<String> columnNames;
    private List<String> columnTypes;
    private List<Map<String, Object>> rows;
    private long executionTime;
    private int affectedRows;
    private String error;
    private boolean isQuery;

    // 查询结果构造函数
    public SQLiteQueryResult(List<String> columnNames, List<String> columnTypes, List<Map<String, Object>> rows, long executionTime) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.rows = rows;
        this.executionTime = executionTime;
        this.isQuery = true;
    }

    // 更新结果构造函数
    public SQLiteQueryResult(int affectedRows, long executionTime) {
        this.affectedRows = affectedRows;
        this.executionTime = executionTime;
        this.isQuery = false;
    }

    // 错误结果构造函数
    public SQLiteQueryResult(String error, long executionTime) {
        this.error = error;
        this.executionTime = executionTime;
        this.isQuery = false;
    }

    @Override
    public List<String> getColumnNames() {
        return columnNames;
    }

    @Override
    public List<String> getColumnTypes() {
        return columnTypes;
    }

    @Override
    public List<Map<String, Object>> getRows() {
        return rows;
    }

    @Override
    public int getRowCount() {
        return rows != null ? rows.size() : 0;
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
        return isQuery;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}