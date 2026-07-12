package dino.jdbx.app;

import javafx.scene.Node;

/**
 * Abstraction over SQL editor implementations (native pane / WebView).
 */
public interface SqlEditor {
    String getText();

    void setText(String text);

    String getSelection();

    void replaceSelection(String text);

    void requestFocus();

    Node getNode();

    /** Optional caret position for statement-at-cursor; -1 if unsupported. */
    default int getCaretPosition() {
        return -1;
    }
}
