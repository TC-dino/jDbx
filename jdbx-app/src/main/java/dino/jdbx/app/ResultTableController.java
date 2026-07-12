package dino.jdbx.app;

import dino.jdbx.core.api.QueryHistory;
import dino.jdbx.core.api.QueryResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Result / message / history panel.
 */
public class ResultTableController {

    @FXML
    private TableView<Map<String, Object>> resultTable;

    @FXML
    private Label rowCountLabel;

    @FXML
    private Label executionTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private TabPane resultTabPane;

    @FXML
    private TextArea messageArea;

    @FXML
    private ListView<QueryHistory> historyListView;

    private QueryResult currentResult;
    private Consumer<String> onReplaySql;

    @FXML
    public void initialize() {
        resultTable.setEditable(false);
        resultTable.setPlaceholder(new Label("无查询结果"));
        resultTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        resultTable.setStyle("-fx-font-family: 'Consolas', 'Courier New'; -fx-font-size: 13px;");

        if (historyListView != null) {
            historyListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(QueryHistory item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        String sql = item.getSql() == null ? "" : item.getSql().replaceAll("\\s+", " ");
                        if (sql.length() > 80) {
                            sql = sql.substring(0, 80) + "...";
                        }
                        String mark = item.isSuccess() ? "✓" : "✗";
                        setText(mark + " " + sql);
                    }
                }
            });
            historyListView.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    QueryHistory selected = historyListView.getSelectionModel().getSelectedItem();
                    if (selected != null && onReplaySql != null) {
                        onReplaySql.accept(selected.getSql());
                    }
                }
            });
        }
    }

    public void setOnReplaySql(Consumer<String> onReplaySql) {
        this.onReplaySql = onReplaySql;
    }

    public void showHistory(List<QueryHistory> history) {
        if (historyListView == null) {
            return;
        }
        historyListView.setItems(FXCollections.observableArrayList(history == null ? List.of() : history));
    }

    public void showResult(QueryResult result) {
        if (result == null) {
            return;
        }
        this.currentResult = result;
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        if (result.hasError()) {
            showError(result.getError());
            return;
        }

        if (result.isQuery()) {
            showQueryResult(result);
        } else {
            showUpdateResult(result);
        }
    }

    private void showQueryResult(QueryResult result) {
        List<String> columns = result.getColumnNames();
        List<Map<String, Object>> rows = result.getRows();
        if (columns == null || columns.isEmpty()) {
            return;
        }

        for (String columnName : columns) {
            TableColumn<Map<String, Object>, String> column = new TableColumn<>(columnName);
            column.setCellValueFactory(param -> {
                Map<String, Object> row = param.getValue();
                Object value = row.get(columnName);
                return new SimpleStringProperty(value == null ? "NULL" : value.toString());
            });
            column.setPrefWidth(120);
            column.setMinWidth(80);
            resultTable.getColumns().add(column);
        }

        ObservableList<Map<String, Object>> data = FXCollections.observableArrayList();
        if (rows != null) {
            data.addAll(rows);
        }
        resultTable.setItems(data);

        rowCountLabel.setText("行数: " + result.getRowCount());
        executionTimeLabel.setText(String.format("耗时: %d ms", result.getExecutionTime()));
        statusLabel.setText("查询成功");

        if (resultTabPane != null) {
            resultTabPane.getSelectionModel().select(0);
        }
    }

    private void showUpdateResult(QueryResult result) {
        rowCountLabel.setText("影响行数: " + result.getAffectedRows());
        executionTimeLabel.setText(String.format("耗时: %d ms", result.getExecutionTime()));
        statusLabel.setText("执行成功");
        showMessage(String.format("影响了 %d 行数据", result.getAffectedRows()));
        if (resultTabPane != null) {
            resultTabPane.getSelectionModel().select(1);
        }
    }

    public void showError(String error) {
        statusLabel.setText("执行失败");
        rowCountLabel.setText("");
        executionTimeLabel.setText("");
        showMessage("错误: " + error);
        if (resultTabPane != null) {
            resultTabPane.getSelectionModel().select(1);
        }
    }

    public void showMessage(String message) {
        if (messageArea != null) {
            messageArea.setText(message);
        }
    }

    public void clear() {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();
        rowCountLabel.setText("");
        executionTimeLabel.setText("");
        statusLabel.setText("就绪");
        if (messageArea != null) {
            messageArea.clear();
        }
    }

    @FXML
    private void onExportCsv() {
        if (currentResult != null && currentResult.isQuery()) {
            DataExporter.exportToCsv(resultTable.getScene().getWindow(), currentResult);
        }
    }

    @FXML
    private void onCopySelected() {
        ObservableList<Map<String, Object>> selected = resultTable.getSelectionModel().getSelectedItems();
        List<String> columns = resultTable.getColumns().stream()
                .map(TableColumn::getText)
                .collect(Collectors.toList());
        if (columns.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.join("\t", columns)).append('\n');
        List<Map<String, Object>> rows = (selected == null || selected.isEmpty())
                ? resultTable.getItems()
                : selected;
        for (Map<String, Object> row : rows) {
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sb.append('\t');
                }
                Object value = row.get(columns.get(i));
                sb.append(value == null ? "" : value.toString());
            }
            sb.append('\n');
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(sb.toString());
        Clipboard.getSystemClipboard().setContent(content);
        statusLabel.setText("已复制 " + rows.size() + " 行");
    }

    @FXML
    private void onShowChart() {
        if (currentResult == null || !currentResult.isQuery()) {
            return;
        }
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("chart.fxml"));
            DialogPane dialogPane = loader.load();
            ChartController controller = loader.getController();
            controller.setQueryResult(currentResult);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("数据可视化");
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
