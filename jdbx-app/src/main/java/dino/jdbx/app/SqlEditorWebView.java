package dino.jdbx.app;

import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * WebView-backed SQL editor (Phase 2). Implements {@link SqlEditor}.
 */
public class SqlEditorWebView extends StackPane implements SqlEditor {

    private final WebView webView;
    private final WebEngine engine;

    public SqlEditorWebView() {
        webView = new WebView();
        engine = webView.getEngine();
        engine.loadContent(createHtml(""));
        getChildren().add(webView);
        getStyleClass().add("sql-editor-webview");
    }

    private String createHtml(String sql) {
        String escaped = escapeHtml(sql == null ? "" : sql);
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <style>
                html, body { margin:0; padding:0; height:100%%; background:#1E1E1E; }
                #editor {
                  box-sizing:border-box; width:100%%; height:100vh; padding:8px;
                  font-family:Consolas,'Courier New',monospace; font-size:14px;
                  background:#1E1E1E; color:#D4D4D4; border:none; outline:none; resize:none;
                }
              </style>
            </head>
            <body>
              <textarea id="editor" spellcheck="false">%s</textarea>
              <script>
                document.getElementById('editor').addEventListener('keydown', function(e) {
                  if (e.key === 'Tab') {
                    e.preventDefault();
                    var start = this.selectionStart, end = this.selectionEnd;
                    this.value = this.value.substring(0, start) + '    ' + this.value.substring(end);
                    this.selectionStart = this.selectionEnd = start + 4;
                  }
                });
                function getText() { return document.getElementById('editor').value; }
                function setText(t) {
                  var el = document.getElementById('editor');
                  el.value = t || '';
                }
                function getSelection() {
                  var el = document.getElementById('editor');
                  return el.value.substring(el.selectionStart, el.selectionEnd);
                }
                function replaceSelection(t) {
                  var el = document.getElementById('editor');
                  var start = el.selectionStart, end = el.selectionEnd;
                  el.value = el.value.substring(0, start) + (t||'') + el.value.substring(end);
                  el.selectionStart = el.selectionEnd = start + (t||'').length;
                }
                function getCaret() { return document.getElementById('editor').selectionStart; }
              </script>
            </body>
            </html>
            """.formatted(escaped);
    }

    private static String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    @Override
    public String getText() {
        try {
            Object result = engine.executeScript("getText()");
            return result == null ? "" : result.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void setText(String text) {
        String safe = text == null ? "" : text
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "");
        try {
            engine.executeScript("setText('" + safe + "')");
        } catch (Exception e) {
            engine.loadContent(createHtml(text));
        }
    }

    @Override
    public String getSelection() {
        try {
            Object result = engine.executeScript("getSelection()");
            return result == null ? "" : result.toString();
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void replaceSelection(String text) {
        String safe = text == null ? "" : text
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "");
        try {
            engine.executeScript("replaceSelection('" + safe + "')");
        } catch (Exception ignored) {
        }
    }

    @Override
    public void requestFocus() {
        webView.requestFocus();
        try {
            engine.executeScript("document.getElementById('editor').focus()");
        } catch (Exception ignored) {
        }
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public int getCaretPosition() {
        try {
            Object result = engine.executeScript("getCaret()");
            if (result instanceof Number number) {
                return number.intValue();
            }
        } catch (Exception ignored) {
        }
        return -1;
    }
}
