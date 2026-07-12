package dino.jdbx.app;

import dino.jdbx.core.api.QueryResult;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;

import java.util.*;

/**
 * 数据可视化工具
 */
public class DataVisualization {

    /**
     * 图表类型
     */
    public enum ChartType {
        BAR,        // 柱状图
        LINE,       // 折线图
        PIE,        // 饼图
        AREA        // 面积图
    }

    /**
     * 创建柱状图
     */
    public static BarChart<String, Number> createBarChart(QueryResult result, String title,
                                                           String xAxisLabel, String yAxisLabel) {
        if (result == null || !result.isQuery() || result.getRows().isEmpty()) {
            return null;
        }

        List<String> columns = result.getColumnNames();
        if (columns.size() < 2) {
            return null;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);

        // 使用第一列作为分类，第二列作为值
        String categoryColumn = columns.get(0);
        String valueColumn = columns.get(1);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(valueColumn);

        for (Map<String, Object> row : result.getRows()) {
            String category = String.valueOf(row.get(categoryColumn));
            Number value = toNumber(row.get(valueColumn));
            series.getData().add(new XYChart.Data<>(category, value));
        }

        chart.getData().add(series);
        return chart;
    }

    /**
     * 创建折线图
     */
    public static LineChart<String, Number> createLineChart(QueryResult result, String title,
                                                             String xAxisLabel, String yAxisLabel) {
        if (result == null || !result.isQuery() || result.getRows().isEmpty()) {
            return null;
        }

        List<String> columns = result.getColumnNames();
        if (columns.size() < 2) {
            return null;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);

        // 使用第一列作为分类，后续列作为值
        String categoryColumn = columns.get(0);

        for (int i = 1; i < columns.size(); i++) {
            String valueColumn = columns.get(i);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(valueColumn);

            for (Map<String, Object> row : result.getRows()) {
                String category = String.valueOf(row.get(categoryColumn));
                Number value = toNumber(row.get(valueColumn));
                series.getData().add(new XYChart.Data<>(category, value));
            }

            chart.getData().add(series);
        }

        return chart;
    }

    /**
     * 创建饼图
     */
    public static PieChart createPieChart(QueryResult result, String title) {
        if (result == null || !result.isQuery() || result.getRows().isEmpty()) {
            return null;
        }

        List<String> columns = result.getColumnNames();
        if (columns.size() < 2) {
            return null;
        }

        String categoryColumn = columns.get(0);
        String valueColumn = columns.get(1);

        List<PieChart.Data> pieData = new ArrayList<>();
        for (Map<String, Object> row : result.getRows()) {
            String category = String.valueOf(row.get(categoryColumn));
            Number value = toNumber(row.get(valueColumn));
            pieData.add(new PieChart.Data(category, value.doubleValue()));
        }

        PieChart chart = new PieChart(javafx.collections.FXCollections.observableArrayList(pieData));
        chart.setTitle(title);
        return chart;
    }

    /**
     * 创建面积图
     */
    public static AreaChart<String, Number> createAreaChart(QueryResult result, String title,
                                                             String xAxisLabel, String yAxisLabel) {
        if (result == null || !result.isQuery() || result.getRows().isEmpty()) {
            return null;
        }

        List<String> columns = result.getColumnNames();
        if (columns.size() < 2) {
            return null;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel(xAxisLabel);
        yAxis.setLabel(yAxisLabel);

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.setTitle(title);

        // 使用第一列作为分类，后续列作为值
        String categoryColumn = columns.get(0);

        for (int i = 1; i < columns.size(); i++) {
            String valueColumn = columns.get(i);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(valueColumn);

            for (Map<String, Object> row : result.getRows()) {
                String category = String.valueOf(row.get(categoryColumn));
                Number value = toNumber(row.get(valueColumn));
                series.getData().add(new XYChart.Data<>(category, value));
            }

            chart.getData().add(series);
        }

        return chart;
    }

    /**
     * 创建图表
     */
    public static javafx.scene.Node createChart(ChartType type, QueryResult result, String title,
                                                  String xAxisLabel, String yAxisLabel) {
        switch (type) {
            case BAR:
                return createBarChart(result, title, xAxisLabel, yAxisLabel);
            case LINE:
                return createLineChart(result, title, xAxisLabel, yAxisLabel);
            case PIE:
                return createPieChart(result, title);
            case AREA:
                return createAreaChart(result, title, xAxisLabel, yAxisLabel);
            default:
                return null;
        }
    }

    /**
     * 转换为数字
     */
    private static Number toNumber(Object value) {
        if (value == null) {
            return 0;
        }

        if (value instanceof Number) {
            return (Number) value;
        }

        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 自动推荐图表类型
     */
    public static ChartType recommendChartType(QueryResult result) {
        if (result == null || !result.isQuery()) {
            return ChartType.BAR;
        }

        List<String> columns = result.getColumnNames();
        int rowCount = result.getRowCount();

        // 如果只有一列分类和一列值，推荐饼图
        if (columns.size() == 2 && rowCount <= 10) {
            return ChartType.PIE;
        }

        // 如果行数较多，推荐折线图
        if (rowCount > 20) {
            return ChartType.LINE;
        }

        // 默认柱状图
        return ChartType.BAR;
    }
}
