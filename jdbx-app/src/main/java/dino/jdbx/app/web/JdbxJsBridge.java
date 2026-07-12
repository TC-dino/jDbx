package dino.jdbx.app.web;

import javafx.application.Platform;

/**
 * Methods are called from JavaScript ({@code window.jdbx.*}).
 * Must be public for the WebView JS bridge.
 */
public class JdbxJsBridge {

    private final WebUiHost host;

    public JdbxJsBridge(WebUiHost host) {
        this.host = host;
    }

    public void newConnection() {
        Platform.runLater(host::webNewConnection);
    }

    public void connect(String connectionId) {
        Platform.runLater(() -> host.webConnect(connectionId));
    }

    public void connectionContext(String connectionId) {
        Platform.runLater(() -> host.webConnectionContext(connectionId));
    }

    public void openTable(String connectionId, String tableName, String kind) {
        Platform.runLater(() -> host.webOpenTable(connectionId, tableName, kind));
    }

    public void requestConnections() {
        Platform.runLater(host::webRequestConnections);
    }

    public void requestHistory() {
        Platform.runLater(host::webRequestHistory);
    }

    public void clearHistory() {
        Platform.runLater(host::webClearHistory);
    }

    public void replay(String sql) {
        Platform.runLater(() -> host.webReplaySql(sql));
    }

    public void close() {
        Platform.runLater(host::webCloseHistory);
    }

    public void requestPlugins() {
        Platform.runLater(host::webRequestPlugins);
    }

    public void saveConnection(String json) {
        Platform.runLater(() -> host.webSaveConnection(json));
    }

    public void testConnection(String json) {
        Platform.runLater(() -> host.webTestConnection(json));
    }

    public void closeConnectionDialog() {
        Platform.runLater(host::webCloseConnectionDialog);
    }

    public void newQuery() {
        Platform.runLater(host::webNewQuery);
    }
}
