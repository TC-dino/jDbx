package dino.jdbx.app;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Simple SQL keyword autocomplete for native TextArea editors.
 */
public final class SqlAutocomplete {

    private static final List<String> KEYWORDS = Arrays.asList(
            "SELECT", "FROM", "WHERE", "AND", "OR", "INSERT", "INTO", "VALUES",
            "UPDATE", "SET", "DELETE", "CREATE", "TABLE", "DROP", "ALTER",
            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "BY",
            "ORDER", "ASC", "DESC", "LIMIT", "OFFSET", "DISTINCT", "AS", "NULL",
            "COUNT", "SUM", "AVG", "MIN", "MAX", "HAVING", "UNION", "ALL"
    );

    private final Popup popup = new Popup();
    private final ListView<String> listView = new ListView<>();
    private final TextArea textArea;

    public SqlAutocomplete(TextArea textArea) {
        this.textArea = textArea;
        listView.setPrefSize(220, 160);
        popup.getContent().add(listView);
        popup.setAutoHide(true);

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() >= 1) {
                applySelected();
            }
        });

        textArea.textProperty().addListener((obs, o, n) -> maybeShow());
        textArea.caretPositionProperty().addListener((obs, o, n) -> maybeShow());

        textArea.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (!popup.isShowing()) {
                return;
            }
            if (e.getCode() == KeyCode.ESCAPE) {
                popup.hide();
                e.consume();
            } else if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.TAB) {
                applySelected();
                e.consume();
            } else if (e.getCode() == KeyCode.DOWN) {
                listView.getSelectionModel().selectNext();
                e.consume();
            } else if (e.getCode() == KeyCode.UP) {
                listView.getSelectionModel().selectPrevious();
                e.consume();
            }
        });
    }

    private void maybeShow() {
        String prefix = currentWordPrefix();
        if (prefix.length() < 2) {
            popup.hide();
            return;
        }
        String upper = prefix.toUpperCase(Locale.ROOT);
        ObservableList<String> matches = FXCollections.observableArrayList(
                KEYWORDS.stream().filter(k -> k.startsWith(upper)).collect(Collectors.toList())
        );
        if (matches.isEmpty()) {
            popup.hide();
            return;
        }
        listView.setItems(matches);
        listView.getSelectionModel().selectFirst();

        Bounds caretBounds = textArea.lookup(".text") != null
                ? textArea.localToScreen(textArea.getBoundsInLocal())
                : null;
        if (caretBounds != null) {
            popup.show(textArea, caretBounds.getMinX() + 20, caretBounds.getMinY() + 40);
        } else if (textArea.getScene() != null && textArea.getScene().getWindow() != null) {
            popup.show(textArea.getScene().getWindow());
        }
    }

    private String currentWordPrefix() {
        String text = textArea.getText();
        int caret = textArea.getCaretPosition();
        if (text == null || caret <= 0) {
            return "";
        }
        int i = caret - 1;
        while (i >= 0) {
            char c = text.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '_') {
                break;
            }
            i--;
        }
        return text.substring(i + 1, caret);
    }

    private void applySelected() {
        String selected = listView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            popup.hide();
            return;
        }
        String text = textArea.getText();
        int caret = textArea.getCaretPosition();
        int start = caret - currentWordPrefix().length();
        textArea.replaceText(start, caret, selected + " ");
        popup.hide();
    }
}
