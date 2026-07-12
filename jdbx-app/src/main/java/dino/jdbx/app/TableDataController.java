package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.QueryResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;

import java.util.*;

/**
 * 表数据控制器
 */
public class TableDataController {

    @FXML
    private Label tableNameLabel;

    @FXML
    private TextField filterField;

    @FXML
    private ComboBox<String> sortColumnCombo;

    @FXML
    private ComboBox<String> sortOrderCombo;

    @FXML
    private TableView<Map<String, Object>> dataTable;

    @FXML
    private ComboBox<Integer> pageSizeCombo;

    @FXML
    private Label pageLabel;

    @FXML
    private Label totalRowsLabel;

    @FXML
    private Label statusLabel;

    private Connection connection;
    private String tableName;
    private List<String> columns;
    private ObservableList<Map<String, Object>> data;
    private int currentPage = 1;
    private int pageSize = 100;
    private int totalRows = 0;
    private String whereClause = "";
    private String orderByClause = "";

    @FXML
    public void initialize() {
        data = FXCollections.observableArrayList();

        // 初始化每页行数
        pageSizeCombo.getItems().addAll(50, 100, 200, 500, 1000);
        pageSizeCombo.setValue(pageSize);
        pageSizeCombo.setOnAction(e -> {
            pageSize = pageSizeCombo.getValue();
            currentPage = 1;
            loadData();
        });

        // 初始化排序方式
        sortOrderCombo.getItems().addAll("ASC", "DESC");
        sortOrderCombo.setValue("ASC");

        // 设置双击编辑
        dataTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openEditDialog(selected);
                }
            }
        });
    }

    /**
     * 打开编辑对话框
     */
    private void openEditDialog(Map<String, Object> rowData) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("table-data-edit.fxml"));
            DialogPane dialogPane = loader.load();

            TableDataEditController controller = loader.getController();
            controller.setTableWithData(connection, tableName, rowData);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("编辑行 - " + tableName);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == controller.saveButtonType) {
                if (controller.validate()) {
                    String sql = controller.generateUpdateSql();
                    if (sql != null) {
                        connection.executeUpdate(sql);
                        loadData();
                        statusLabel.setText("更新成功");
                    } else {
                        statusLabel.setText("没有变化");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("编辑失败: " + e.getMessage());
        }
    }

    /**
     * 设置表信息
     */
    public void setTable(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
        tableNameLabel.setText("表: " + tableName);
        initColumns();
        loadData();
    }

    /**
     * 初始化列信息
     */
    private void initColumns() {
        try {
            QueryResult result = connection.executeQuery("SELECT * FROM " + tableName + " LIMIT 0");
            if (result != null && !result.hasError()) {
                columns = result.getColumnNames();

                // 设置表格列
                dataTable.getColumns().clear();
                for (String column : columns) {
                    TableColumn<Map<String, Object>, String> col = new TableColumn<>(column);
                    col.setCellValueFactory(param -> {
                        Map<String, Object> row = param.getValue();
                        Object value = row.get(column);
                        return new SimpleStringProperty(value == null ? "NULL" : value.toString());
                    });
                    col.setPrefWidth(120);
                    col.setMinWidth(80);
                    dataTable.getColumns().add(col);
                }

                // 更新排序列选择
                sortColumnCombo.getItems().clear();
                sortColumnCombo.getItems().addAll(columns);
            }
        } catch (Exception e) {
            statusLabel.setText("获取列信息失败: " + e.getMessage());
        }
    }

    /**
     * 加载数据
     */
    private void loadData() {
        try {
            statusLabel.setText("加载中...");

            // 构建查询
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * FROM ").append(tableName);

            if (whereClause != null && !whereClause.isEmpty()) {
                sql.append(" WHERE ").append(whereClause);
            }

            if (orderByClause != null && !orderByClause.isEmpty()) {
                sql.append(" ORDER BY ").append(orderByClause);
            }

            sql.append(" LIMIT ").append(pageSize);
            sql.append(" OFFSET ").append((currentPage - 1) * pageSize);

            QueryResult result = connection.executeQuery(sql.toString());
            if (result != null && !result.hasError()) {
                data.clear();
                data.addAll(result.getRows());
                dataTable.setItems(data);

                // 更新状态
                totalRowsLabel.setText("显示 " + data.size() + " 行");
                pageLabel.setText("第 " + currentPage + " 页");
                statusLabel.setText("加载完成");
            } else {
                statusLabel.setText("加载失败: " + (result != null ? result.getError() : "未知错误"));
            }
        } catch (Exception e) {
            statusLabel.setText("加载失败: " + e.getMessage());
        }
    }

    /**
     * 刷新
     */
    @FXML
    private void onRefresh() {
        loadData();
    }

    /**
     * 新增行
     */
    @FXML
    private void onAddRow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("table-data-edit.fxml"));
            DialogPane dialogPane = loader.load();

            TableDataEditController controller = loader.getController();
            controller.setTable(connection, tableName);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle("新增行 - " + tableName);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == controller.saveButtonType) {
                if (controller.validate()) {
                    String sql = controller.generateInsertSql();
                    if (sql != null) {
                        connection.executeUpdate(sql);
                        loadData();
                        statusLabel.setText("新增成功");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("新增失败: " + e.getMessage());
        }
    }

    /**
     * 删除行
     */
    @FXML
    private void onDeleteRow() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("请先选择要删除的行");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("确认删除");
        alert.setHeaderText(null);
        alert.setContentText("确定要删除选中的行吗？");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // 构建 DELETE 语句
                    StringBuilder sql = new StringBuilder();
                    sql.append("DELETE FROM ").append(tableName).append(" WHERE ");

                    List<String> conditions = new ArrayList<>();
                    for (Map.Entry<String, Object> entry : selected.entrySet()) {
                        Object value = entry.getValue();
                        if (value == null) {
                            conditions.add(entry.getKey() + " IS NULL");
                        } else {
                            conditions.add(entry.getKey() + " = '" + value.toString().replace("'", "''") + "'");
                        }
                    }
                    sql.append(String.join(" AND ", conditions));

                    connection.executeUpdate(sql.toString());
                    loadData();
                    statusLabel.setText("删除成功");
                } catch (Exception e) {
                    statusLabel.setText("删除失败: " + e.getMessage());
                }
            }
        });
    }

    /**
     * 保存更改
     */
    @FXML
    private void onSave() {
        // TODO: 实现批量保存更改
        statusLabel.setText("保存功能开发中...");
    }

    /**
     * 应用筛选
     */
    @FXML
    private void onApplyFilter() {
        whereClause = filterField.getText();
        currentPage = 1;
        loadData();
    }

    /**
     * 清除筛选
     */
    @FXML
    private void onClearFilter() {
        filterField.clear();
        whereClause = "";
        currentPage = 1;
        loadData();
    }

    /**
     * 应用排序
     */
    @FXML
    private void onApplySort() {
        String column = sortColumnCombo.getValue();
        String order = sortOrderCombo.getValue();
        if (column != null) {
            orderByClause = column + " " + (order != null ? order : "ASC");
            currentPage = 1;
            loadData();
        }
    }

    /**
     * 首页
     */
    @FXML
    private void onFirstPage() {
        currentPage = 1;
        loadData();
    }

    /**
     * 上一页
     */
    @FXML
    private void onPrevPage() {
        if (currentPage > 1) {
            currentPage--;
            loadData();
        }
    }

    /**
     * 下一页
     */
    @FXML
    private void onNextPage() {
        currentPage++;
        loadData();
    }

    /**
     * 末页
     */
    @FXML
    private void onLastPage() {
        // TODO: 计算总页数
        currentPage++;
        loadData();
    }

    /**
     * 导出
     */
    @FXML
    private void onExport() {
        DataExporter.exportToCsv(dataTable.getScene().getWindow(), null);
        statusLabel.setText("导出功能开发中...");
    }

    /**
     * 复制
     */
    @FXML
    private void onCopy() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Object> entry : selected.entrySet()) {
                sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            // 复制到剪贴板
            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(sb.toString());
            clipboard.setContent(content);
            statusLabel.setText("已复制到剪贴板");
        }
    }

    /**
     * 复制为 INSERT
     */
    @FXML
    private void onCopyAsInsert() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("INSERT INTO ").append(tableName).append(" (");

            List<String> colNames = new ArrayList<>(selected.keySet());
            sb.append(String.join(", ", colNames));
            sb.append(") VALUES (");

            List<String> values = new ArrayList<>();
            for (String col : colNames) {
                Object value = selected.get(col);
                if (value == null) {
                    values.add("NULL");
                } else if (value instanceof String) {
                    values.add("'" + ((String) value).replace("'", "''") + "'");
                } else {
                    values.add(value.toString());
                }
            }
            sb.append(String.join(", ", values));
            sb.append(");");

            javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
            javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
            content.putString(sb.toString());
            clipboard.setContent(content);
            statusLabel.setText("已复制为 INSERT 语句");
        }
    }

    /**
     * 查看单元格详情
     */
    @FXML
    private void onViewCell() {
        Map<String, Object> selected = dataTable.getSelectionModel().getSelectedItem();
        TableColumn<Map<String, Object>, String> selectedColumn = dataTable.getSelectionModel().getSelectedCells().isEmpty() ?
                null : dataTable.getSelectionModel().getSelectedCells().get(0).getTableColumn();

        if (selected != null && selectedColumn != null) {
            String columnName = selectedColumn.getText();
            Object value = selected.get(columnName);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("单元格详情");
            alert.setHeaderText(columnName);
            alert.setContentText(value != null ? value.toString() : "NULL");
            alert.showAndWait();
        }
    }
}
