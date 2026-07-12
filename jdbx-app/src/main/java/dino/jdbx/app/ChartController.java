package dino.jdbx.app;

import dino.jdbx.core.api.QueryResult;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * 图表控制器
 */
public class ChartController {

    @FXML
    private ComboBox<DataVisualization.ChartType> chartTypeCombo;

    @FXML
    private TextField titleField;

    @FXML
    private TextField xAxisField;

    @FXML
    private TextField yAxisField;

    @FXML
    private VBox chartContainer;

    private QueryResult queryResult;

    @FXML
    public void initialize() {
        // 初始化图表类型
        chartTypeCombo.setItems(FXCollections.observableArrayList(DataVisualization.ChartType.values()));
        chartTypeCombo.setValue(DataVisualization.ChartType.BAR);

        // 设置默认值
        xAxisField.setText("类别");
        yAxisField.setText("值");
    }

    /**
     * 设置查询结果
     */
    public void setQueryResult(QueryResult result) {
        this.queryResult = result;

        if (result != null && result.isQuery()) {
            // 自动推荐图表类型
            DataVisualization.ChartType recommended = DataVisualization.recommendChartType(result);
            chartTypeCombo.setValue(recommended);

            // 自动生成标题
            if (result.getColumnNames().size() >= 2) {
                titleField.setText(result.getColumnNames().get(0) + " vs " + result.getColumnNames().get(1));
            }
        }
    }

    /**
     * 生成图表
     */
    @FXML
    private void onGenerateChart() {
        if (queryResult == null || !queryResult.isQuery()) {
            showAlert(Alert.AlertType.WARNING, "没有可用的查询结果");
            return;
        }

        DataVisualization.ChartType chartType = chartTypeCombo.getValue();
        String title = titleField.getText();
        String xAxisLabel = xAxisField.getText();
        String yAxisLabel = yAxisField.getText();

        if (title == null || title.isEmpty()) {
            title = "数据图表";
        }

        try {
            Node chart = DataVisualization.createChart(chartType, queryResult, title, xAxisLabel, yAxisLabel);

            if (chart != null) {
                chartContainer.getChildren().clear();
                chartContainer.getChildren().add(chart);
            } else {
                showAlert(Alert.AlertType.WARNING, "无法生成图表，请检查数据格式");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "生成图表失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
