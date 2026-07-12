package dino.jdbx.plugin.mysql;

import dino.jdbx.core.api.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * MySQL 查询结果
 */
public class MySQLQueryResult implements QueryResult {

    private List<String> columnNames;
    private List<String> columnTypes;
    private List<Map<String, Object>> rows;
    private int affectedRows;
    private long executionTime;
    private boolean query;
    private String error;

    /**
     * 查询结果构造函数
     */
    public MySQLQueryResult(List<String> columnNames, List<String> columnTypes,
                           List<Map<String, Object>> rows, long executionTime) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.rows = rows;
        this.executionTime = executionTime;
        this.query = true;
        this.affectedRows = 0;
    }

    /**
     * 更新结果构造函数
     */
    public MySQLQueryResult(int affectedRows, long executionTime) {
        this.affectedRows = affectedRows;
        this.executionTime = executionTime;
        this.query = false;
    }

    /**
     * 错误结果构造函数
     */
    public MySQLQueryResult(String error, long executionTime) {
        this.error = error;
        this.executionTime = executionTime;
        this.query = false;
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
        return query;
    }

    @Override
    public String getError() {
        return error;
    }

    @Override
    public boolean hasError() {
        return error != null;
    }
}
