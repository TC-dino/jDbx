package dino.jdbx.core.api;

/**
 * 插件接口 - 所有插件必须实现
 */
public interface Plugin {

    /**
     * 插件 ID
     */
    String getId();

    /**
     * 插件名称
     */
    String getName();

    /**
     * 插件版本
     */
    String getVersion();

    /**
     * 插件描述
     */
    String getDescription();

    /**
     * 插件作者
     */
    String getAuthor();

    /**
     * 依赖的其他插件 ID
     */
    default String[] getDependencies() {
        return new String[0];
    }

    /**
     * 初始化插件
     */
    void initialize(PluginContext context);

    /**
     * 卸载插件
     */
    default void unload() {
    }
}