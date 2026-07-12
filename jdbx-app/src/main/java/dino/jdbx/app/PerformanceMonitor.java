package dino.jdbx.app;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 性能监控器
 */
public class PerformanceMonitor {

    private static final PerformanceMonitor INSTANCE = new PerformanceMonitor();

    // 查询统计
    private final AtomicInteger totalQueries = new AtomicInteger(0);
    private final AtomicInteger successfulQueries = new AtomicInteger(0);
    private final AtomicInteger failedQueries = new AtomicInteger(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong maxExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);

    // 连接统计
    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private final AtomicInteger activeConnections = new AtomicInteger(0);

    // 最近查询
    private final List<QueryMetric> recentQueries = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_RECENT_QUERIES = 100;

    // 连接使用统计
    private final Map<String, ConnectionMetric> connectionMetrics = new ConcurrentHashMap<>();

    private PerformanceMonitor() {
    }

    public static PerformanceMonitor getInstance() {
        return INSTANCE;
    }

    /**
     * 记录查询
     */
    public void recordQuery(String connectionId, String sql, long executionTime, boolean success) {
        totalQueries.incrementAndGet();

        if (success) {
            successfulQueries.incrementAndGet();
        } else {
            failedQueries.incrementAndGet();
        }

        totalExecutionTime.addAndGet(executionTime);

        // 更新最大最小执行时间
        updateMaxExecutionTime(executionTime);
        updateMinExecutionTime(executionTime);

        // 添加到最近查询列表
        QueryMetric metric = new QueryMetric(connectionId, sql, executionTime, success, new Date());
        recentQueries.add(0, metric);

        // 限制列表大小
        while (recentQueries.size() > MAX_RECENT_QUERIES) {
            recentQueries.remove(recentQueries.size() - 1);
        }

        // 更新连接统计
        connectionMetrics.computeIfAbsent(connectionId, k -> new ConnectionMetric(k))
            .recordQuery(executionTime, success);
    }

    /**
     * 记录连接
     */
    public void recordConnection(String connectionId) {
        totalConnections.incrementAndGet();
        activeConnections.incrementAndGet();
    }

    /**
     * 记录断开连接
     */
    public void recordDisconnection(String connectionId) {
        activeConnections.decrementAndGet();
    }

    /**
     * 获取总查询数
     */
    public int getTotalQueries() {
        return totalQueries.get();
    }

    /**
     * 获取成功查询数
     */
    public int getSuccessfulQueries() {
        return successfulQueries.get();
    }

    /**
     * 获取失败查询数
     */
    public int getFailedQueries() {
        return failedQueries.get();
    }

    /**
     * 获取平均执行时间
     */
    public double getAverageExecutionTime() {
        int total = totalQueries.get();
        if (total == 0) {
            return 0;
        }
        return (double) totalExecutionTime.get() / total;
    }

    /**
     * 获取最大执行时间
     */
    public long getMaxExecutionTime() {
        long max = maxExecutionTime.get();
        return max == 0 ? 0 : max;
    }

    /**
     * 获取最小执行时间
     */
    public long getMinExecutionTime() {
        long min = minExecutionTime.get();
        return min == Long.MAX_VALUE ? 0 : min;
    }

    /**
     * 获取总连接数
     */
    public int getTotalConnections() {
        return totalConnections.get();
    }

    /**
     * 获取活动连接数
     */
    public int getActiveConnections() {
        return activeConnections.get();
    }

    /**
     * 获取最近查询
     */
    public List<QueryMetric> getRecentQueries() {
        return new ArrayList<>(recentQueries);
    }

    /**
     * 获取连接统计
     */
    public Map<String, ConnectionMetric> getConnectionMetrics() {
        return new HashMap<>(connectionMetrics);
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        int total = totalQueries.get();
        if (total == 0) {
            return 0;
        }
        return (double) successfulQueries.get() / total * 100;
    }

    /**
     * 重置统计
     */
    public void reset() {
        totalQueries.set(0);
        successfulQueries.set(0);
        failedQueries.set(0);
        totalExecutionTime.set(0);
        maxExecutionTime.set(0);
        minExecutionTime.set(Long.MAX_VALUE);
        totalConnections.set(0);
        activeConnections.set(0);
        recentQueries.clear();
        connectionMetrics.clear();
    }

    /**
     * 更新最大执行时间
     */
    private void updateMaxExecutionTime(long executionTime) {
        long currentMax;
        do {
            currentMax = maxExecutionTime.get();
            if (executionTime <= currentMax) {
                break;
            }
        } while (!maxExecutionTime.compareAndSet(currentMax, executionTime));
    }

    /**
     * 更新最小执行时间
     */
    private void updateMinExecutionTime(long executionTime) {
        long currentMin;
        do {
            currentMin = minExecutionTime.get();
            if (executionTime >= currentMin) {
                break;
            }
        } while (!minExecutionTime.compareAndSet(currentMin, executionTime));
    }

    /**
     * 获取统计摘要
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("性能监控摘要:\n");
        sb.append("- 总查询数: ").append(getTotalQueries()).append("\n");
        sb.append("- 成功查询: ").append(getSuccessfulQueries()).append("\n");
        sb.append("- 失败查询: ").append(getFailedQueries()).append("\n");
        sb.append("- 成功率: ").append(String.format("%.2f%%", getSuccessRate())).append("\n");
        sb.append("- 平均执行时间: ").append(String.format("%.2f ms", getAverageExecutionTime())).append("\n");
        sb.append("- 最大执行时间: ").append(getMaxExecutionTime()).append(" ms\n");
        sb.append("- 最小执行时间: ").append(getMinExecutionTime()).append(" ms\n");
        sb.append("- 总连接数: ").append(getTotalConnections()).append("\n");
        sb.append("- 活动连接数: ").append(getActiveConnections()).append("\n");
        return sb.toString();
    }

    /**
     * 查询指标
     */
    public static class QueryMetric {
        private final String connectionId;
        private final String sql;
        private final long executionTime;
        private final boolean success;
        private final Date timestamp;

        public QueryMetric(String connectionId, String sql, long executionTime, boolean success, Date timestamp) {
            this.connectionId = connectionId;
            this.sql = sql;
            this.executionTime = executionTime;
            this.success = success;
            this.timestamp = timestamp;
        }

        public String getConnectionId() { return connectionId; }
        public String getSql() { return sql; }
        public long getExecutionTime() { return executionTime; }
        public boolean isSuccess() { return success; }
        public Date getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return String.format("[%s] %s - %d ms - %s",
                timestamp, sql.length() > 50 ? sql.substring(0, 50) + "..." : sql,
                executionTime, success ? "成功" : "失败");
        }
    }

    /**
     * 连接指标
     */
    public static class ConnectionMetric {
        private final String connectionId;
        private final AtomicInteger queryCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger failCount = new AtomicInteger(0);
        private final AtomicLong totalExecutionTime = new AtomicLong(0);

        public ConnectionMetric(String connectionId) {
            this.connectionId = connectionId;
        }

        public void recordQuery(long executionTime, boolean success) {
            queryCount.incrementAndGet();
            if (success) {
                successCount.incrementAndGet();
            } else {
                failCount.incrementAndGet();
            }
            totalExecutionTime.addAndGet(executionTime);
        }

        public String getConnectionId() { return connectionId; }
        public int getQueryCount() { return queryCount.get(); }
        public int getSuccessCount() { return successCount.get(); }
        public int getFailCount() { return failCount.get(); }

        public double getAverageExecutionTime() {
            int count = queryCount.get();
            if (count == 0) {
                return 0;
            }
            return (double) totalExecutionTime.get() / count;
        }

        public double getSuccessRate() {
            int total = queryCount.get();
            if (total == 0) {
                return 0;
            }
            return (double) successCount.get() / total * 100;
        }
    }
}
