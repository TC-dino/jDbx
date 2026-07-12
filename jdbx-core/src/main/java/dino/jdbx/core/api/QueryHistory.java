package dino.jdbx.core.api;

import java.time.LocalDateTime;

/**
 * 查询历史记录
 */
public class QueryHistory {

    private String id;
    private String sql;
    private String connectionId;
    private LocalDateTime executedAt;
    private long executionTime;
    private boolean success;
    private String error;
    private int affectedRows;

    public QueryHistory(String sql, String connectionId) {
        this.sql = sql;
        this.connectionId = connectionId;
        this.executedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSql() { return sql; }
    public void setSql(String sql) { this.sql = sql; }

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    public long getExecutionTime() { return executionTime; }
    public void setExecutionTime(long executionTime) { this.executionTime = executionTime; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public int getAffectedRows() { return affectedRows; }
    public void setAffectedRows(int affectedRows) { this.affectedRows = affectedRows; }
}