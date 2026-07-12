package dino.jdbx.app;

import javafx.geometry.Insets;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextFlow;

/**
 * SQL 编辑器面板
 * 支持语法高亮的 SQL 编辑器
 */
public class SqlEditorPane extends StackPane implements SqlEditor {

    private TextArea textArea;
    private TextFlow textFlow;
    private ScrollPane scrollPane;
    private boolean isHighlighting = false;

    public SqlEditorPane() {
        initComponents();
        setupEventHandlers();
    }

    private void initComponents() {
        textArea = new TextArea();
        textArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px;");
        textArea.setWrapText(false);
        textArea.setPrefRowCount(15);
        textArea.getStyleClass().add("sql-editor");

        textFlow = new TextFlow();
        textFlow.setPadding(new Insets(4, 8, 4, 8));
        textFlow.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 14px;");

        scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        getChildren().add(textArea);
    }

    private void setupEventHandlers() {
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!isHighlighting) {
                highlightText(newText);
            }
        });
    }

    private void highlightText(String text) {
        isHighlighting = true;
        try {
            TextFlow highlighted = SqlSyntaxHighlighter.highlight(text);
            textFlow.getChildren().setAll(highlighted.getChildren());
        } finally {
            isHighlighting = false;
        }
    }

    @Override
    public String getText() {
        return textArea.getText();
    }

    @Override
    public void setText(String text) {
        textArea.setText(text == null ? "" : text);
    }

    @Override
    public String getSelection() {
        return textArea.getSelectedText();
    }

    @Override
    public void replaceSelection(String text) {
        IndexRange range = textArea.getSelection();
        if (range.getLength() > 0) {
            textArea.replaceText(range, text == null ? "" : text);
        } else {
            textArea.insertText(textArea.getCaretPosition(), text == null ? "" : text);
        }
    }

    @Override
    public void requestFocus() {
        textArea.requestFocus();
    }

    @Override
    public javafx.scene.Node getNode() {
        return this;
    }

    @Override
    public int getCaretPosition() {
        return textArea.getCaretPosition();
    }

    public String getSelectedText() {
        return getSelection();
    }

    public void setPromptText(String promptText) {
        textArea.setPromptText(promptText);
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void clear() {
        textArea.clear();
    }
}
