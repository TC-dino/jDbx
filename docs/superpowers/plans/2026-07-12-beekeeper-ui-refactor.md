# Beekeeper UI Refactor Implementation Plan

> **For agentic workers:** Execute task-by-task. User requested all phases in one pass. Steps use checkbox syntax.

**Goal:** Align jDbx UI/UX with Beekeeper Studio across shell, operations, editor, and polish (Phases 1–3).

**Architecture:** Progressive enhancement of `jdbx-app` + persistence/security in `jdbx-core`. Keep `MainController` as shell; extract sidebar helpers; `SqlEditor` interface with Pane (P1) and WebView (P2).

**Tech Stack:** Java 21, JavaFX 21, Gson, Ikonli, javafx.concurrent.Task

---

## File map

| File | Role |
|------|------|
| `jdbx-core/.../connection/ConnectionStore.java` | Load/save `~/.jdbx/connections.json` |
| `jdbx-core/.../security/PasswordCipher.java` | AES encrypt/decrypt passwords |
| `jdbx-core/.../connection/DefaultConnectionManager.java` | Wire store + cipher |
| `jdbx-core/.../theme/DefaultThemeManager.java` | Default DARK |
| `jdbx-app/.../SqlEditor.java` | Editor interface |
| `jdbx-app/.../SqlEditorPane.java` | Implement SqlEditor |
| `jdbx-app/.../SqlEditorWebView.java` | Implement SqlEditor (P2) |
| `jdbx-app/.../main.fxml` | Beekeeper shell |
| `jdbx-app/.../MainController.java` | Flat tree, search, async connect, collapse |
| `jdbx-app/.../query-tab.fxml` | SplitPane |
| `jdbx-app/.../QueryTabController.java` | Async query, shortcuts, history |
| `jdbx-app/.../SqlEditorController.java` | Use SqlEditor, format via SqlFormatter, shortcuts |
| `jdbx-app/.../ResultTableController.java` | Copy TSV, history tab hook |
| `jdbx-app/.../JdbxApplication.java` | Dark CSS default |
| `jdbx-app/.../styles/dark.css` | Shell polish |
| `jdbx-app/pom.xml` + `module-info.java` | Ikonli deps |

---

### Task 1: Connection persistence + password cipher

- [ ] Implement `PasswordCipher` (AES-128, key file `~/.jdbx/secret.key`)
- [ ] Implement `ConnectionStore` with Gson + LocalDateTime adapter
- [ ] Update `DefaultConnectionManager` to load on construct, save on save/delete
- [ ] Unit test round-trip save/load (temp dir)
- [ ] Commit

### Task 2: Theme default Dark + app entry

- [ ] `DefaultThemeManager` default `Theme.DARK`
- [ ] `JdbxApplication` load dark.css; apply theme from config if present
- [ ] Commit

### Task 3: Beekeeper shell FXML + MainController

- [ ] Rewrite `main.fxml`: horizontal SplitPane (sidebar | workspace), search/`+`/new/manage/`toggle` wired, status bar theme button
- [ ] Flat connection list; search filter; async connect Task; connection context menu; SELECT * action
- [ ] Collapse sidebar via divider/button
- [ ] Commit

### Task 4: Query workspace

- [ ] `query-tab.fxml` SplitPane vertical
- [ ] `SqlEditor` interface; Pane implements; Controller uses it
- [ ] Async execute + Ctrl+Enter / Ctrl+Shift+Enter (statement-at-cursor)
- [ ] Wire QueryHistoryManager; result maxRows
- [ ] Commit

### Task 5: Results + history (Phase 2)

- [ ] Copy selected rows as TSV
- [ ] History list in result panel; double-click replay to editor
- [ ] Commit

### Task 6: WebView editor + autocomplete (Phase 2)

- [ ] `SqlEditorWebView` implements `SqlEditor`
- [ ] Config `editor.engine` = `native` | `webview`; switch in SqlEditorController
- [ ] Simple keyword autocomplete popup for native editor
- [ ] Commit

### Task 7: Table UX + encryption polish (Phase 3)

- [ ] Ensure passwords encrypted at rest via PasswordCipher in store
- [ ] Table data: respect maxRows; improve status feedback
- [ ] Connection manage: list with edit/delete from sidebar manage button
- [ ] Plugin manager: show id + version in dialog
- [ ] Commit

### Task 8: Verify build

- [ ] `mvn -q -DskipTests compile` (and targeted tests)
- [ ] Fix module-info / dependency issues
