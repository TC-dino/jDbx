package dino.jdbx.app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * SQL 格式化工具控制器
 */
public class SqlFormatterController {

    @FXML
    private TextArea inputArea;

    @FXML
    private TextArea outputArea;

    @FXML
    public void initialize() {
    }

    /**
     * 格式化
     */
    @FXML
    private void onFormat() {
        String sql = inputArea.getText();
        if (sql != null && !sql.trim().isEmpty()) {
            String formatted = SqlFormatter.format(sql);
            outputArea.setText(formatted);
        }
    }

    /**
     * 压缩
     */
    @FXML
    private void onMinify() {
        String sql = inputArea.getText();
        if (sql != null && !sql.trim().isEmpty()) {
            String minified = SqlFormatter.minify(sql);
            outputArea.setText(minified);
        }
    }

    /**
     * 转大写
     */
    @FXML
    private void onUpperCase() {
        String sql = inputArea.getText();
        if (sql != null && !sql.trim().isEmpty()) {
            String upper = SqlFormatter.toUpperCase(sql);
            outputArea.setText(upper);
        }
    }

    /**
     * 转小写
     */
    @FXML
    private void onLowerCase() {
        String sql = inputArea.getText();
        if (sql != null && !sql.trim().isEmpty()) {
            String lower = SqlFormatter.toLowerCase(sql);
            outputArea.setText(lower);
        }
    }

    /**
     * 清空
     */
    @FXML
    private void onClear() {
        inputArea.clear();
        outputArea.clear();
    }

    /**
     * 复制
     */
    @FXML
    private void onCopy() {
        String text = outputArea.getText();
        if (text != null && !text.isEmpty()) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(text);
            clipboard.setContent(content);
        }
    }

    /**
     * 设置输入 SQL
     */
    public void setInput(String sql) {
        inputArea.setText(sql);
    }
}
