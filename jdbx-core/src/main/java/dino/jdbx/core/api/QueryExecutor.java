package dino.jdbx.core.api;

import java.util.List;

/**
 * 查询执行器接口
 */
public interface QueryExecutor {

    /**
     * 执行查询
     */
    QueryResult executeQuery(String sql) throws Exception;

    /**
     * 执行更新
     */
    int executeUpdate(String sql) throws Exception;

    /**
     * 执行批处理
     */
    int[] executeBatch(List<String> sqlList) throws Exception;

    /**
     * 获取查询历史
     */
    List<QueryHistory> getHistory();

    /**
     * 清空历史
     */
    void clearHistory();

    /**
     * 取消正在执行的查询
     */
    void cancelQuery();
}