package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.Metadata;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * 表数据编辑对话框控制器
 */
public class TableDataEditController {

    @FXML
    private VBox formContainer;

    @FXML
    public ButtonType saveButtonType;

    private Connection connection;
    private String tableName;
    private List<Metadata.Column> columns;
    private Map<String, TextField> fieldMap = new LinkedHashMap<>();
    private Map<String, Object> originalData;
    private boolean editMode = false;

    @FXML
    public void initialize() {
    }

    /**
     * 设置表信息（新增模式）
     */
    public void setTable(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
        this.editMode = false;
        loadColumns();
        buildForm();
    }

    /**
     * 设置表信息和数据（编辑模式）
     */
    public void setTableWithData(Connection connection, String tableName, Map<String, Object> data) {
        this.connection = connection;
        this.tableName = tableName;
        this.originalData = data;
        this.editMode = true;
        loadColumns();
        buildForm();
        fillFormData(data);
    }

    /**
     * 加载列信息
     */
    private void loadColumns() {
        try {
            Metadata metadata = connection.getTableMetadata(tableName);
            if (metadata != null) {
                columns = metadata.getColumns();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建表单
     */
    private void buildForm() {
        formContainer.getChildren().clear();
        fieldMap.clear();

        if (columns == null) {
            return;
        }

        for (Metadata.Column column : columns) {
            HBox row = new HBox(10);
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // 列标签
            Label label = new Label(column.getName());
            label.setPrefWidth(120);
            label.setStyle("-fx-font-weight: bold;");

            // 类型信息
            Label typeLabel = new Label(column.getType());
            typeLabel.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
            typeLabel.setPrefWidth(100);

            // 输入框
            TextField field = new TextField();
            field.setPromptText(column.isNullable() ? "可为空" : "必填");
            field.setPrefWidth(200);

            // 主键标识
            if (column.isPrimaryKey()) {
                label.setStyle("-fx-font-weight: bold; -fx-text-fill: #0078D4;");
                if (editMode) {
                    field.setEditable(false); // 主键在编辑模式下不可修改
                    field.setStyle("-fx-background-color: #f0f0f0;");
                }
            }

            row.getChildren().addAll(label, typeLabel, field);
            formContainer.getChildren().add(row);

            fieldMap.put(column.getName(), field);
        }
    }

    /**
     * 填充表单数据
     */
    private void fillFormData(Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            TextField field = fieldMap.get(entry.getKey());
            if (field != null) {
                Object value = entry.getValue();
                field.setText(value != null ? value.toString() : "");
            }
        }
    }

    /**
     * 获取保存的数据
     */
    public Map<String, Object> getData() {
        Map<String, Object> data = new LinkedHashMap<>();
        for (Map.Entry<String, TextField> entry : fieldMap.entrySet()) {
            String value = entry.getValue().getText();
            if (value != null && !value.isEmpty()) {
                data.put(entry.getKey(), value);
            } else {
                data.put(entry.getKey(), null);
            }
        }
        return data;
    }

    /**
     * 生成 INSERT SQL
     */
    public String generateInsertSql() {
        Map<String, Object> data = getData();
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");

        List<String> colNames = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (entry.getValue() != null) {
                colNames.add(entry.getKey());
                if (entry.getValue() instanceof String) {
                    values.add("'" + ((String) entry.getValue()).replace("'", "''") + "'");
                } else {
                    values.add(entry.getValue().toString());
                }
            }
        }

        sql.append(String.join(", ", colNames));
        sql.append(") VALUES (");
        sql.append(String.join(", ", values));
        sql.append(")");

        return sql.toString();
    }

    /**
     * 生成 UPDATE SQL
     */
    public String generateUpdateSql() {
        if (originalData == null) {
            return null;
        }

        Map<String, Object> newData = getData();
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(tableName).append(" SET ");

        List<String> setClauses = new ArrayList<>();
        for (Map.Entry<String, Object> entry : newData.entrySet()) {
            String colName = entry.getKey();
            Object newValue = entry.getValue();
            Object oldValue = originalData.get(colName);

            // 只更新有变化的列
            if (!Objects.equals(newValue, oldValue)) {
                if (newValue == null) {
                    setClauses.add(colName + " = NULL");
                } else if (newValue instanceof String) {
                    setClauses.add(colName + " = '" + ((String) newValue).replace("'", "''") + "'");
                } else {
                    setClauses.add(colName + " = " + newValue.toString());
                }
            }
        }

        if (setClauses.isEmpty()) {
            return null; // 没有变化
        }

        sql.append(String.join(", ", setClauses));
        sql.append(" WHERE ");

        // 使用主键作为 WHERE 条件
        List<String> whereClauses = new ArrayList<>();
        for (Metadata.Column column : columns) {
            if (column.isPrimaryKey()) {
                Object value = originalData.get(column.getName());
                if (value == null) {
                    whereClauses.add(column.getName() + " IS NULL");
                } else if (value instanceof String) {
                    whereClauses.add(column.getName() + " = '" + ((String) value).replace("'", "''") + "'");
                } else {
                    whereClauses.add(column.getName() + " = " + value.toString());
                }
            }
        }

        sql.append(String.join(" AND ", whereClauses));
        return sql.toString();
    }

    /**
     * 是否是编辑模式
     */
    public boolean isEditMode() {
        return editMode;
    }

    /**
     * 验证必填字段
     */
    public boolean validate() {
        for (Metadata.Column column : columns) {
            if (!column.isNullable() && !column.isPrimaryKey()) {
                TextField field = fieldMap.get(column.getName());
                if (field != null && (field.getText() == null || field.getText().isEmpty())) {
                    // 检查是否有默认值
                    if (column.getDefaultValue() == null || column.getDefaultValue().isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, column.getName() + " 不能为空");
                        field.requestFocus();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
