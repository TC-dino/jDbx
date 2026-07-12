package dino.jdbx.core.api;

import java.util.List;
import java.util.Map;

/**
 * 查询结果接口
 */
public interface QueryResult {

    /**
     * 获取列名列表
     */
    List<String> getColumnNames();

    /**
     * 获取列类型列表
     */
    List<String> getColumnTypes();

    /**
     * 获取数据行
     */
    List<Map<String, Object>> getRows();

    /**
     * 获取总行数
     */
    int getRowCount();

    /**
     * 获取影响行数（INSERT/UPDATE/DELETE）
     */
    int getAffectedRows();

    /**
     * 获取执行时间（毫秒）
     */
    long getExecutionTime();

    /**
     * 是否是查询结果（SELECT）
     */
    boolean isQuery();

    /**
     * 获取错误信息
     */
    String getError();

    /**
     * 是否有错误
     */
    boolean hasError();
}