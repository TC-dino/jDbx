package dino.jdbx.app;

/**
 * Resolve which SQL statement to run for Ctrl+Enter.
 */
public final class SqlStatementResolver {

    private SqlStatementResolver() {
    }

    /**
     * If selection is non-blank, return it; otherwise return the statement around caret
     * (split by {@code ;}). If caret unknown, return full text.
     */
    public static String resolve(String fullText, String selection, int caret) {
        if (selection != null && !selection.trim().isEmpty()) {
            return selection.trim();
        }
        if (fullText == null || fullText.isBlank()) {
            return "";
        }
        if (caret < 0 || caret > fullText.length()) {
            return fullText.trim();
        }

        int start = fullText.lastIndexOf(';', Math.max(0, caret - 1));
        start = start < 0 ? 0 : start + 1;
        int end = fullText.indexOf(';', caret);
        end = end < 0 ? fullText.length() : end;
        return fullText.substring(start, end).trim();
    }
}
