package dino.jdbx.app.web;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.util.ArrayList;
import java.util.List;

/**
 * WebView host that loads local HTML pages and installs {@link JdbxJsBridge}.
 */
public class WebPanel extends StackPane {

    private final WebView webView = new WebView();
    private final WebEngine engine = webView.getEngine();
    private JdbxJsBridge bridge;
    private boolean bridgeReady;
    private final List<String> pendingScripts = new ArrayList<>();

    public WebPanel() {
        getChildren().add(webView);
        webView.setContextMenuEnabled(false);
        getStyleClass().add("web-panel");
        setStyle("-fx-background-color: #0e2429;");
    }

    public WebEngine getEngine() {
        return engine;
    }

    public void loadPage(String classpathHtml, JdbxJsBridge bridge) {
        this.bridge = bridge;
        this.bridgeReady = false;
        pendingScripts.clear();
        engine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                installBridge();
                flushPending();
            }
        });
        var url = getClass().getResource(classpathHtml);
        if (url == null) {
            engine.loadContent("<html><body style='background:#0e2429;color:#efc524;font-family:sans-serif;padding:16px'>"
                    + "Missing resource: " + classpathHtml + "</body></html>");
            return;
        }
        engine.load(url.toExternalForm());
    }

    private void installBridge() {
        if (bridge == null) {
            return;
        }
        try {
            JSObject window = (JSObject) engine.executeScript("window");
            window.setMember("jdbx", bridge);
            bridgeReady = true;
            engine.executeScript("if (typeof onJdbxReady === 'function') onJdbxReady();");
        } catch (Exception e) {
            System.err.println("安装 Web 桥接失败: " + e.getMessage());
        }
    }

    public void callJs(String script) {
        if (!bridgeReady) {
            pendingScripts.add(script);
            return;
        }
        tryExecute(script);
    }

    private void flushPending() {
        List<String> copy = new ArrayList<>(pendingScripts);
        pendingScripts.clear();
        for (String script : copy) {
            tryExecute(script);
        }
    }

    private void tryExecute(String script) {
        try {
            engine.executeScript(script);
        } catch (Exception e) {
            System.err.println("执行 JS 失败: " + e.getMessage());
        }
    }
}
