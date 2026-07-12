package dino.jdbx.plugin.redis;

import dino.jdbx.core.api.QueryResult;

import java.util.List;
import java.util.Map;

/**
 * Redis 查询结果
 */
public class RedisQueryResult implements QueryResult {

    private List<String> columnNames;
    private List<String> columnTypes;
    private List<Map<String, Object>> rows;
    private long executionTime;
    private String error;

    /**
     * 查询结果构造函数
     */
    public RedisQueryResult(List<String> columnNames, List<String> columnTypes,
                           List<Map<String, Object>> rows, long executionTime) {
        this.columnNames = columnNames;
        this.columnTypes = columnTypes;
        this.rows = rows;
        this.executionTime = executionTime;
    }

    /**
     * 错误结果构造函数
     */
    public RedisQueryResult(String error, long executionTime) {
        this.error = error;
        this.executionTime = executionTime;
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
        return 0;
    }

    @Override
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public boolean isQuery() {
        return true;
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
