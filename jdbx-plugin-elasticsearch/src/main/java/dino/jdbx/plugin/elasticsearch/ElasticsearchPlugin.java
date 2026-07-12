package dino.jdbx.plugin.elasticsearch;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * Elasticsearch 数据库插件
 */
public class ElasticsearchPlugin implements DatabasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(ElasticsearchPlugin.class);

    private PluginContext context;

    @Override
    public String getId() {
        return "elasticsearch";
    }

    @Override
    public String getName() {
        return "Elasticsearch";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "Elasticsearch 数据库插件";
    }

    @Override
    public String getAuthor() {
        return "jDbx Team";
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        logger.info("Elasticsearch 插件已初始化");
    }

    @Override
    public void unload() {
        logger.info("Elasticsearch 插件已卸载");
    }

    @Override
    public String getDatabaseType() {
        return "Elasticsearch";
    }

    @Override
    public int getDefaultPort() {
        return 9200;
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new ElasticsearchConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return Arrays.asList(
            new ConnectionParam("host", "主机", "localhost", true),
            new ConnectionParam("port", "端口", "9200", true),
            new ConnectionParam("username", "用户名", "", false),
            new ConnectionParam("password", "密码", "", false)
        );
    }

    @Override
    public List<String> getKeywords() {
        return Arrays.asList(
            // 索引操作
            "PUT", "POST", "GET", "DELETE", "HEAD",
            // 查询 DSL
            "query", "match", "match_all", "match_phrase", "multi_match",
            "term", "terms", "range", "exists", "prefix", "wildcard", "regexp",
            "fuzzy", "ids", "bool", "must", "must_not", "should", "filter",
            "constant_score", "function_score", "dis_max",
            // 聚合
            "aggs", "aggregations", "avg", "sum", "min", "max", "value_count",
            "cardinality", "stats", "extended_stats", "percentiles", "percentile_ranks",
            "terms", "significant_terms", "range", "date_range", "histogram",
            "date_histogram", "composite", "filters", "sampler",
            "top_hits", "nested", "reverse_nested", "adjacency_matrix",
            // 管道聚合
            "bucket_sort", "bucket_selector", "bucket_script", "cumulative_sum",
            "derivative", "moving_avg", "serial_diff",
            // 排序和分页
            "sort", "from", "size", "search_after", "scroll",
            // 脚本
            "script", "source", "lang", "params",
            // 映射
            "mappings", "properties", "type", "index", "analyzer",
            "settings", "number_of_shards", "number_of_replicas",
            // 文档操作
            "_source", "_index", "_id", "_type", "_score",
            // 其他
            "highlight", "suggest", "rescore", "explain", "version",
            "timeout", "terminate_after", "track_total_hits"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        // Elasticsearch 使用查询 DSL 而不是传统函数
        return Arrays.asList(
            "match", "match_phrase", "match_all", "term", "terms",
            "range", "exists", "prefix", "wildcard", "regexp",
            "bool", "must", "must_not", "should", "filter",
            "avg", "sum", "min", "max", "count", "cardinality",
            "date_histogram", "terms", "range", "filter",
            "script", "painless", "expression"
        );
    }
}
