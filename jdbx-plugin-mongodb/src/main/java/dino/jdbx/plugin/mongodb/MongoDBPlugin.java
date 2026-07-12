package dino.jdbx.plugin.mongodb;

import dino.jdbx.core.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * MongoDB 数据库插件
 */
public class MongoDBPlugin implements DatabasePlugin {

    private static final Logger logger = LoggerFactory.getLogger(MongoDBPlugin.class);

    private PluginContext context;

    @Override
    public String getId() {
        return "mongodb";
    }

    @Override
    public String getName() {
        return "MongoDB";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getDescription() {
        return "MongoDB 数据库插件";
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
        logger.info("MongoDB 插件已初始化");
    }

    @Override
    public void unload() {
        logger.info("MongoDB 插件已卸载");
    }

    @Override
    public String getDatabaseType() {
        return "MongoDB";
    }

    @Override
    public int getDefaultPort() {
        return 27017;
    }

    @Override
    public ConnectionFactory createConnectionFactory() {
        return new MongoDBConnectionFactory();
    }

    @Override
    public List<ConnectionParam> getConnectionParams() {
        return Arrays.asList(
            new ConnectionParam("host", "主机", "localhost", true),
            new ConnectionParam("port", "端口", "27017", true),
            new ConnectionParam("database", "数据库", "test", true),
            new ConnectionParam("username", "用户名", "", false),
            new ConnectionParam("password", "密码", "", false)
        );
    }

    @Override
    public List<String> getKeywords() {
        return Arrays.asList(
            // 查询操作
            "find", "findOne", "findMany", "count", "distinct",
            // 插入操作
            "insertOne", "insertMany",
            // 更新操作
            "updateOne", "updateMany", "replaceOne",
            // 删除操作
            "deleteOne", "deleteMany",
            // 聚合操作
            "aggregate", "group", "match", "project", "sort", "limit", "skip",
            "unwind", "lookup", "addFields", "count", "sortByCount",
            // 索引操作
            "createIndex", "dropIndex", "listIndexes",
            // 集合操作
            "createCollection", "dropCollection", "renameCollection",
            // 数据库操作
            "listCollections", "listDatabases", "dropDatabase",
            // 查询操作符
            "$eq", "$ne", "$gt", "$gte", "$lt", "$lte", "$in", "$nin",
            "$and", "$or", "$not", "$nor", "$exists", "$type", "$regex",
            "$expr", "$jsonSchema", "$text", "$where",
            // 更新操作符
            "$set", "$unset", "$inc", "$mul", "$rename", "$min", "$max",
            "$currentDate", "$addToSet", "$pop", "$pull", "$push", "$each",
            // 聚合操作符
            "$sum", "$avg", "$min", "$max", "$first", "$last", "$push",
            "$addToSet", "$size", "$arrayElemAt", "$slice", "$filter",
            "$map", "$reduce", "$zip", "$indexOfArray",
            // 管道操作符
            "$lookup", "$graphLookup", "$unwind", "$group", "$match",
            "$project", "$sort", "$limit", "$skip", "$sample", "$out",
            "$merge", "$facet", "$bucket", "$bucketAuto"
        );
    }

    @Override
    public List<String> getBuiltinFunctions() {
        // MongoDB 使用聚合管道操作符而不是传统函数
        return Arrays.asList(
            "ObjectId", "ISODate", "NumberLong", "NumberInt", "NumberDecimal",
            "UUID", "BinData", "MD5", "HexData",
            "toString", "toLower", "toUpper", "trim", "substr", "strLen",
            "concat", "split", "indexOf",
            "year", "month", "day", "hour", "minute", "second", "millisecond",
            "dateToString", "dateFromString", "dateAdd", "dateDiff",
            "abs", "ceil", "floor", "round", "sqrt", "log", "log10", "pow",
            "exp", "trunc", "rand",
            "arrayToObject", "objectToArray", "map", "filter", "reduce",
            "zip", "indexOfArray", "range", "reverseArray", "sortArray"
        );
    }
}
