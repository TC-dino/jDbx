package dino.jdbx.app;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlStatementResolverTest {

    @Test
    void usesSelectionWhenPresent() {
        assertEquals("SELECT 1", SqlStatementResolver.resolve("SELECT 1; SELECT 2", "SELECT 1", 0));
    }

    @Test
    void resolvesStatementAroundCaret() {
        String sql = "SELECT a FROM t; UPDATE t SET x=1; SELECT 2";
        int caret = sql.indexOf("UPDATE") + 3;
        assertEquals("UPDATE t SET x=1", SqlStatementResolver.resolve(sql, "", caret));
    }

    @Test
    void returnsFullTextWhenCaretUnknown() {
        assertEquals("SELECT 1", SqlStatementResolver.resolve("SELECT 1", "", -1));
    }
}
