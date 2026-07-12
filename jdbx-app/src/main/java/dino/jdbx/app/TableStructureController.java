package dino.jdbx.app;

import dino.jdbx.core.api.Connection;
import dino.jdbx.core.api.Metadata;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;
import java.util.Map;

/**
 * 表结构对话框控制器
 */
public class TableStructureController {

    @FXML
    private TabPane tabPane;

    // 列信息表格
    @FXML
    private TableView<Metadata.Column> columnsTable;
    @FXML
    private TableColumn<Metadata.Column, String> columnNameColumn;
    @FXML
    private TableColumn<Metadata.Column, String> columnTypeColumn;
    @FXML
    private TableColumn<Metadata.Column, String> nullableColumn;
    @FXML
    private TableColumn<Metadata.Column, String> primaryKeyColumn;
    @FXML
    private TableColumn<Metadata.Column, String> defaultValueColumn;
    @FXML
    private TableColumn<Metadata.Column, String> commentColumn;

    // 索引信息表格
    @FXML
    private TableView<Metadata.Index> indexesTable;
    @FXML
    private TableColumn<Metadata.Index, String> indexNameColumn;
    @FXML
    private TableColumn<Metadata.Index, String> indexColumnColumn;
    @FXML
    private TableColumn<Metadata.Index, String> uniqueColumn;

    // 外键信息表格
    @FXML
    private TableView<Metadata.ForeignKey> foreignKeysTable;
    @FXML
    private TableColumn<Metadata.ForeignKey, String> fkNameColumn;
    @FXML
    private TableColumn<Metadata.ForeignKey, String> fkColumnColumn;
    @FXML
    private TableColumn<Metadata.ForeignKey, String> referencedTableColumn;
    @FXML
    private TableColumn<Metadata.ForeignKey, String> referencedColumnColumn;

    // DDL
    @FXML
    private TextArea ddlArea;

    private Connection connection;
    private String tableName;

    @FXML
    public void initialize() {
        setupColumnsTable();
        setupIndexesTable();
        setupForeignKeysTable();
    }

    private void setupColumnsTable() {
        columnNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        columnTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getType()));
        nullableColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().isNullable() ? "是" : "否"));
        primaryKeyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().isPrimaryKey() ? "是" : "否"));
        defaultValueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getDefaultValue()));
        commentColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getComment()));
    }

    private void setupIndexesTable() {
        indexNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        indexColumnColumn.setCellValueFactory(param -> {
            List<String> columns = param.getValue().getColumns();
            return new SimpleStringProperty(columns != null ? String.join(", ", columns) : "");
        });
        uniqueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().isUnique() ? "是" : "否"));
    }

    private void setupForeignKeysTable() {
        fkNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getName()));
        fkColumnColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getColumn()));
        referencedTableColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReferencedTable()));
        referencedColumnColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getReferencedColumn()));
    }

    /**
     * 设置连接和表名
     */
    public void setTable(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
        loadTableStructure();
    }

    /**
     * 加载表结构
     */
    private void loadTableStructure() {
        if (connection == null || tableName == null) {
            return;
        }

        try {
            Metadata metadata = connection.getTableMetadata(tableName);
            if (metadata != null) {
                // 加载列信息
                List<Metadata.Column> columns = metadata.getColumns();
                columnsTable.setItems(FXCollections.observableArrayList(columns));

                // 加载索引信息
                List<Metadata.Index> indexes = metadata.getIndexes();
                indexesTable.setItems(FXCollections.observableArrayList(indexes));

                // 加载外键信息
                List<Metadata.ForeignKey> foreignKeys = metadata.getForeignKeys();
                foreignKeysTable.setItems(FXCollections.observableArrayList(foreignKeys));

                // 生成 DDL
                generateDdl(metadata);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 生成 DDL
     */
    private void generateDdl(Metadata metadata) {
        StringBuilder ddl = new StringBuilder();
        ddl.append("CREATE TABLE ").append(tableName).append(" (\n");

        List<Metadata.Column> columns = metadata.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Metadata.Column col = columns.get(i);
            ddl.append("    ").append(col.getName()).append(" ").append(col.getType());

            if (!col.isNullable()) {
                ddl.append(" NOT NULL");
            }

            if (col.getDefaultValue() != null && !col.getDefaultValue().isEmpty()) {
                ddl.append(" DEFAULT ").append(col.getDefaultValue());
            }

            if (col.isPrimaryKey()) {
                ddl.append(" PRIMARY KEY");
            }

            if (i < columns.size() - 1) {
                ddl.append(",");
            }
            ddl.append("\n");
        }

        ddl.append(");");

        ddlArea.setText(ddl.toString());
    }
}
