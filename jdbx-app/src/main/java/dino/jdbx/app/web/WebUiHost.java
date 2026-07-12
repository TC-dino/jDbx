package dino.jdbx.app.web;

/**
 * Host callbacks invoked from HTML/JS via {@link JdbxJsBridge}.
 */
public interface WebUiHost {
    void webNewConnection();

    void webConnect(String connectionId);

    void webConnectionContext(String connectionId);

    void webOpenTable(String connectionId, String tableName, String kind);

    void webRequestConnections();

    void webRequestHistory();

    void webClearHistory();

    void webReplaySql(String sql);

    void webCloseHistory();

    void webRequestPlugins();

    void webSaveConnection(String json);

    void webTestConnection(String json);

    void webCloseConnectionDialog();

    void webNewQuery();
}
