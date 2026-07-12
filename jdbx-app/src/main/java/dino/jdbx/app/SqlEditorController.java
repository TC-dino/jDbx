package dino.jdbx.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

/**
 * SQL editor toolbar + {@link SqlEditor} host.
 */
public class SqlEditorController {

    @FXML
    private StackPane editorHost;

    @FXML
    private Button executeButton;

    @FXML
    private Button executeSelectedButton;

    @FXML
    private Button formatButton;

    @FXML
    private Button clearButton;

    @FXML
    private Label statusLabel;

    private SqlEditor editor;
    private OnQueryExecuteListener executeListener;
    private String engine = "native";

    public interface OnQueryExecuteListener {
        void onExecute(String sql, boolean selectedOnly);
    }

    @FXML
    public void initialize() {
        String configured = System.getProperty("jdbx.editor.engine", "native");
        this.engine = configured;
        installEditor(engine);

        executeButton.setTooltip(new Tooltip("执行当前语句 (Ctrl+Enter)"));
        executeSelectedButton.setTooltip(new Tooltip("执行全部 (Ctrl+Shift+Enter)"));

        editorHost.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKey);
    }

    public void setEngine(String engine) {
        if (engine == null || engine.equals(this.engine)) {
            return;
        }
        String text = editor != null ? editor.getText() : "";
        this.engine = engine;
        installEditor(engine);
        if (editor != null) {
            editor.setText(text);
        }
    }

    private void installEditor(String engineName) {
        editorHost.getChildren().clear();
        if ("webview".equalsIgnoreCase(engineName)) {
            SqlEditorWebView web = new SqlEditorWebView();
            editor = web;
            editorHost.getChildren().add(web);
        } else {
            SqlEditorPane pane = new SqlEditorPane();
            pane.setPromptText("在此输入 SQL 语句...");
            editor = pane;
            editorHost.getChildren().add(pane);
            new SqlAutocomplete(pane.getTextArea());
        }
    }

    private void handleKey(KeyEvent event) {
        KeyCombination runCurrent = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN);
        KeyCombination runAll = new KeyCodeCombination(KeyCode.ENTER, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
        if (runAll.match(event)) {
            onExecuteAll();
            event.consume();
        } else if (runCurrent.match(event)) {
            onExecute();
            event.consume();
        }
    }

    public void setOnExecuteListener(OnQueryExecuteListener listener) {
        this.executeListener = listener;
    }

    public void setSql(String sql) {
        if (editor != null) {
            editor.setText(sql == null ? "" : sql);
        }
    }

    public String getSql() {
        return editor == null ? "" : editor.getText();
    }

    public String getSelectedSql() {
        return editor == null ? "" : editor.getSelection();
    }

    public SqlEditor getEditor() {
        return editor;
    }

    @FXML
    private void onExecute() {
        if (executeListener == null || editor == null) {
            return;
        }
        String sql = SqlStatementResolver.resolve(editor.getText(), editor.getSelection(), editor.getCaretPosition());
        executeListener.onExecute(sql, false);
    }

    @FXML
    private void onExecuteSelected() {
        onExecuteAll();
    }

    private void onExecuteAll() {
        if (executeListener == null || editor == null) {
            return;
        }
        executeListener.onExecute(editor.getText(), false);
    }

    @FXML
    private void onFormat() {
        if (editor == null) {
            return;
        }
        String sql = editor.getText();
        if (sql == null || sql.isBlank()) {
            return;
        }
        try {
            String formatted = SqlFormatter.format(sql);
            editor.setText(formatted);
            setStatus("已格式化");
        } catch (Exception e) {
            // fallback simple formatter
            editor.setText(simpleFormat(sql));
            setStatus("已格式化");
        }
    }

    @FXML
    private void onClear() {
        if (editor != null) {
            editor.setText("");
        }
        setStatus("已清空");
    }

    public void setStatus(String status) {
        if (statusLabel != null) {
            statusLabel.setText(status);
        }
    }

    private static String simpleFormat(String sql) {
        sql = sql.replaceAll("\\s+", " ").trim();
        String[] keywords = {"SELECT", "FROM", "WHERE", "AND", "OR", "ORDER BY", "GROUP BY", "HAVING",
                "JOIN", "LEFT JOIN", "RIGHT JOIN", "INNER JOIN",
                "INSERT INTO", "VALUES", "UPDATE", "SET", "DELETE FROM"};
        for (String keyword : keywords) {
            sql = sql.replaceAll("(?i)\\b" + keyword + "\\b", "\n" + keyword);
        }
        return sql.trim();
    }
}
