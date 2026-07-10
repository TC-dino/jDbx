package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 字体切换示例 - 演示如何切换系统字体和加载自定义字体文件
 */
public class FontSwitcherExample extends Application {

    private ComboBox<String> fontFamilyCombo;
    private ComboBox<String> fontSizeCombo;
    private TextArea previewArea;
    private List<Font> customFonts = new ArrayList<>();
    private Font defaultFont;

    @Override
    public void start(Stage stage) {
        // 保存默认字体
        defaultFont = Font.getDefault();

        // 主布局
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");

        // 顶部标题
        Label title = new Label("字体切换示例");
        title.setFont(Font.font("Arial", 24));
        title.setStyle("-fx-text-fill: #333;");
        BorderPane.setAlignment(title, Pos.CENTER);
        root.setTop(title);

        // 中间内容区域
        VBox centerBox = createCenterContent();
        root.setCenter(centerBox);

        // 底部按钮区域
        HBox bottomBox = createBottomButtons(stage);
        BorderPane.setAlignment(bottomBox, Pos.CENTER);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 600, 450);
        stage.setTitle("字体切换示例");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createCenterContent() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(20, 0, 0, 0));

        // 字体选择区域
        HBox fontSelectionBox = createFontSelectionBox();
        box.getChildren().add(fontSelectionBox);

        // 预览区域
        Label previewLabel = new Label("预览文本：");
        previewLabel.setFont(Font.font("Arial", 14));
        previewLabel.setStyle("-fx-text-fill: #555;");

        previewArea = new TextArea();
        previewArea.setText("示例文本：Hello World 你好世界 1234567890\nABCDEFGHIJKLMNOPQRSTUVWXYZ\nabcdefghijklmnopqrstuvwxyz");
        previewArea.setPrefHeight(200);
        previewArea.setFont(Font.font("Arial", 16));
        previewArea.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");

        box.getChildren().addAll(previewLabel, previewArea);
        return box;
    }

    private HBox createFontSelectionBox() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER_LEFT);

        // 字体族选择
        Label fontLabel = new Label("系统字体：");
        fontLabel.setFont(Font.font("Arial", 14));
        fontLabel.setStyle("-fx-text-fill: #555;");

        fontFamilyCombo = new ComboBox<>();
        fontFamilyCombo.setPrefWidth(200);
        // 加载系统字体
        fontFamilyCombo.getItems().addAll(Font.getFamilies());
        fontFamilyCombo.setValue(Font.getDefault().getFamily());
        fontFamilyCombo.setOnAction(e -> updatePreviewFont());

        // 字体大小选择
        Label sizeLabel = new Label("字体大小：");
        sizeLabel.setFont(Font.font("Arial", 14));
        sizeLabel.setStyle("-fx-text-fill: #555;");

        fontSizeCombo = new ComboBox<>();
        fontSizeCombo.setPrefWidth(80);
        fontSizeCombo.getItems().addAll("12", "14", "16", "18", "20", "24", "28", "32");
        fontSizeCombo.setValue("16");
        fontSizeCombo.setOnAction(e -> updatePreviewFont());

        box.getChildren().addAll(fontLabel, fontFamilyCombo, sizeLabel, fontSizeCombo);
        return box;
    }

    private HBox createBottomButtons(Stage stage) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 0, 0, 0));

        // 加载字体文件按钮
        Button loadBtn = new Button("加载字体文件");
        loadBtn.setStyle(getButtonStyle("#337ab7", "white"));
        loadBtn.setOnAction(e -> loadFontFile(stage));

        // 应用字体按钮
        Button applyBtn = new Button("应用字体");
        applyBtn.setStyle(getButtonStyle("#5cb85c", "white"));
        applyBtn.setOnAction(e -> applyFont());

        // 重置字体按钮
        Button resetBtn = new Button("重置字体");
        resetBtn.setStyle(getButtonStyle("#777", "white"));
        resetBtn.setOnAction(e -> resetFont());

        box.getChildren().addAll(loadBtn, applyBtn, resetBtn);
        return box;
    }

    private void loadFontFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择字体文件");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("字体文件", "*.ttf", "*.otf"),
                new FileChooser.ExtensionFilter("TrueType字体", "*.ttf"),
                new FileChooser.ExtensionFilter("OpenType字体", "*.otf"),
                new FileChooser.ExtensionFilter("所有文件", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                // 加载字体
                Font customFont = Font.loadFont(selectedFile.toURI().toString(), 16);
                if (customFont != null) {
                    customFonts.add(customFont);
                    String fontName = customFont.getFamily();
                    // 添加到下拉列表并选中
                    if (!fontFamilyCombo.getItems().contains(fontName)) {
                        fontFamilyCombo.getItems().add(fontName);
                    }
                    fontFamilyCombo.setValue(fontName);
                    updatePreviewFont();
                    showAlert("成功", "字体加载成功：" + fontName);
                } else {
                    showAlert("错误", "无法加载字体文件，请检查文件格式是否正确");
                }
            } catch (Exception ex) {
                showAlert("错误", "加载字体文件失败：" + ex.getMessage());
            }
        }
    }

    private void updatePreviewFont() {
        String family = fontFamilyCombo.getValue();
        int size = Integer.parseInt(fontSizeCombo.getValue());

        // 查找自定义字体
        Font selectedFont = null;
        for (Font font : customFonts) {
            if (font.getFamily().equals(family)) {
                selectedFont = Font.font(family, size);
                break;
            }
        }

        // 如果不是自定义字体，使用系统字体
        if (selectedFont == null) {
            selectedFont = Font.font(family, size);
        }

        previewArea.setFont(selectedFont);
    }

    private void applyFont() {
        updatePreviewFont();
    }

    private void resetFont() {
        fontFamilyCombo.setValue(defaultFont.getFamily());
        fontSizeCombo.setValue("16");
        previewArea.setFont(defaultFont);
    }

    private String getButtonStyle(String bgColor, String textColor) {
        return "-fx-background-color: " + bgColor + "; " +
               "-fx-text-fill: " + textColor + "; " +
               "-fx-background-radius: 4; " +
               "-fx-padding: 8 16; " +
               "-fx-font-size: 14;";
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
