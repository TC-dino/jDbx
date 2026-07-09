package dino.jdbx.examples;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * 原生JavaFX实现的Bootstrap 3管理仪表板界面
 */
public class NativeBootstrapDemo extends Application {

    // Bootstrap颜色定义
    private static final Color PRIMARY = Color.web("#337ab7");
    private static final Color SUCCESS = Color.web("#5cb85c");
    private static final Color INFO = Color.web("#5bc0de");
    private static final Color WARNING = Color.web("#f0ad4e");
    private static final Color DANGER = Color.web("#d9534f");
    private static final Color DARK = Color.web("#2c3e50");
    private static final Color LIGHT = Color.web("#f5f7fa");
    private static final Color WHITE = Color.WHITE;
    private static final Color GRAY_LIGHT = Color.web("#f8f9fa");
    private static final Color GRAY_MEDIUM = Color.web("#777");
    private static final Color GRAY_DARK = Color.web("#333");

    @Override
    public void start(Stage stage) {
        // 主容器
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + toHex(LIGHT) + ";");

        // 顶部导航栏
        ToolBar topNavBar = createTopNavBar();
        root.setTop(topNavBar);

        // 侧边栏
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // 主内容区
        ScrollPane mainScrollPane = createMainContent();
        root.setCenter(mainScrollPane);

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Bootstrap 3 管理仪表板 - 原生JavaFX实现");
        stage.setScene(scene);
        stage.show();
    }

    private ToolBar createTopNavBar() {
        // 品牌标签
        Label brand = new Label("  AdminPanel");
        brand.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        brand.setTextFill(WHITE);
        brand.setPadding(new Insets(0, 20, 0, 0));

        // 导航链接
        Button homeBtn = createNavButton("首页", true);
        Button dataBtn = createNavButton("数据概览", false);
        Button msgBtn = createNavButton("消息", false);

        // 搜索框
        TextField searchField = new TextField();
        searchField.setPromptText("搜索...");
        searchField.setPrefWidth(200);
        searchField.setStyle(getInputStyle());

        Button searchBtn = new Button("搜索");
        searchBtn.setStyle(getButtonStyle(PRIMARY, WHITE));

        HBox searchBox = new HBox(5, searchField, searchBtn);
        searchBox.setAlignment(Pos.CENTER);

        // 右侧用户菜单
        Button userBtn = createNavButton("管理员", false);

        // 间距
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ToolBar toolBar = new ToolBar(brand, homeBtn, dataBtn, msgBtn, searchBox, spacer, userBtn);
        toolBar.setStyle("-fx-background-color: " + toHex(DARK) + "; -fx-padding: 8 15;");

        return toolBar;
    }

    private Button createNavButton(String text, boolean active) {
        Button btn = new Button(text);
        String style = active ?
                "-fx-background-color: " + toHex(PRIMARY) + "; -fx-text-fill: white; -fx-background-radius: 3; -fx-padding: 6 12;" :
                "-fx-background-color: transparent; -fx-text-fill: #b8c7ce; -fx-background-radius: 3; -fx-padding: 6 12;";
        btn.setStyle(style);
        return btn;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(0);
        sidebar.setPrefWidth(200);
        sidebar.setStyle("-fx-background-color: " + toHex(DARK) + ";");

        // 主导航标题
        Label mainNavTitle = createSidebarHeading("主导航");
        sidebar.getChildren().add(mainNavTitle);

        // 导航项
        String[] mainItems = {"仪表板", "用户管理", "文章管理", "媒体库", "评论管理"};
        String[] mainBadges = {"新", "128", "", "56", ""};
        boolean[] mainActive = {true, false, false, false, false};

        for (int i = 0; i < mainItems.length; i++) {
            sidebar.getChildren().add(createSidebarItem(mainItems[i], mainBadges[i], mainActive[i]));
        }

        // 系统标题
        Label systemTitle = createSidebarHeading("系统");
        sidebar.getChildren().add(systemTitle);

        // 系统项
        String[] systemItems = {"系统设置", "数据统计", "退出登录"};
        for (String item : systemItems) {
            sidebar.getChildren().add(createSidebarItem(item, "", false));
        }

        return sidebar;
    }

    private Label createSidebarHeading(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        label.setTextFill(Color.web("#7f8c8d"));
        label.setPadding(new Insets(18, 20, 10, 20));
        return label;
    }

    private HBox createSidebarItem(String text, String badge, boolean active) {
        Label icon = new Label("•");
        icon.setTextFill(active ? WHITE : Color.web("#b8c7ce"));
        icon.setPrefWidth(20);

        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        label.setTextFill(active ? WHITE : Color.web("#b8c7ce"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox container = new HBox(10, icon, label, spacer);
        container.setPadding(new Insets(12, 20, 12, 20));
        container.setAlignment(Pos.CENTER_LEFT);

        if (active) {
            container.setStyle("-fx-background-color: " + toHex(PRIMARY) + ";");
        } else {
            container.setStyle("-fx-background-color: " + toHex(DARK) + ";");
        }

        // 添加徽章
        if (!badge.isEmpty()) {
            Label badgeLabel = new Label(badge);
            badgeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            badgeLabel.setTextFill(WHITE);
            badgeLabel.setStyle("-fx-background-color: " + toHex(DANGER) + "; -fx-padding: 2 6; -fx-background-radius: 10;");
            container.getChildren().add(badgeLabel);
        }

        return container;
    }

    private ScrollPane createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25, 30, 25, 30));
        content.setStyle("-fx-background-color: " + toHex(LIGHT) + ";");

        // 面包屑导航
        HBox breadcrumb = createBreadcrumb();
        content.getChildren().add(breadcrumb);

        // 页面标题
        HBox pageTitle = createPageTitle();
        content.getChildren().add(pageTitle);

        // Jumbotron欢迎横幅
        VBox jumbotron = createJumbotron();
        content.getChildren().add(jumbotron);

        // 统计面板
        HBox statPanels = createStatPanels();
        content.getChildren().add(statPanels);

        // 轮播/公告面板
        VBox announcementPanel = createAnnouncementPanel();
        content.getChildren().add(announcementPanel);

        // 表格和进度条
        HBox tableAndProgress = createTableAndProgress();
        content.getChildren().add(tableAndProgress);

        // 表单和警告框
        HBox formAndAlerts = createFormAndAlerts();
        content.getChildren().add(formAndAlerts);

        // 缩略图展示
        VBox thumbnails = createThumbnailPanel();
        content.getChildren().add(thumbnails);

        // 分页
        HBox pagination = createPagination();
        content.getChildren().add(pagination);

        // 页脚
        Label footer = new Label("© 2026 AdminPanel — 基于 Bootstrap 3.4.1 构建的界面原型");
        footer.setTextFill(GRAY_MEDIUM);
        footer.setFont(Font.font("Arial", 13));
        footer.setPadding(new Insets(20, 0, 0, 0));
        content.getChildren().add(footer);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: " + toHex(LIGHT) + ";");

        return scrollPane;
    }

    private HBox createBreadcrumb() {
        Label home = new Label("首页");
        home.setTextFill(PRIMARY);
        home.setFont(Font.font("Arial", 13));

        Label sep1 = new Label(" > ");
        sep1.setTextFill(GRAY_MEDIUM);

        Label admin = new Label("管理");
        admin.setTextFill(PRIMARY);
        admin.setFont(Font.font("Arial", 13));

        Label sep2 = new Label(" > ");
        sep2.setTextFill(GRAY_MEDIUM);

        Label dashboard = new Label("仪表板概览");
        dashboard.setTextFill(GRAY_MEDIUM);
        dashboard.setFont(Font.font("Arial", 13));

        HBox breadcrumb = new HBox(5, home, sep1, admin, sep2, dashboard);
        breadcrumb.setPadding(new Insets(0, 0, 10, 0));
        return breadcrumb;
    }

    private HBox createPageTitle() {
        Label title = new Label("仪表板概览");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        title.setTextFill(GRAY_DARK);

        Label subtitle = new Label("  数据总览与快捷操作");
        subtitle.setFont(Font.font("Arial", 15));
        subtitle.setTextFill(GRAY_MEDIUM);

        HBox pageTitle = new HBox(title, subtitle);
        pageTitle.setPadding(new Insets(0, 0, 15, 0));
        pageTitle.setStyle("-fx-border-color: #e0e5ea; -fx-border-width: 0 0 2 0;");
        return pageTitle;
    }

    private VBox createJumbotron() {
        Label title = new Label("欢迎回来，管理员！");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        title.setTextFill(GRAY_DARK);

        Label desc = new Label("这是基于 Bootstrap 3 构建的管理仪表板界面原型，展示了栅格系统、面板、表格、进度条、轮播、标签、徽章、警告框、模态框等核心组件。所有数据均为模拟展示。");
        desc.setWrapText(true);
        desc.setTextFill(Color.web("#666"));
        desc.setFont(Font.font("Arial", 15));

        Button detailBtn = new Button("查看详情");
        detailBtn.setStyle(getButtonStyle(PRIMARY, WHITE));

        Button downloadBtn = new Button("下载报告");
        downloadBtn.setStyle(getButtonStyle(Color.web("#f8f9fa"), GRAY_DARK));

        HBox buttons = new HBox(10, detailBtn, downloadBtn);
        buttons.setPadding(new Insets(15, 0, 0, 0));

        VBox jumbotron = new VBox(10, title, desc, buttons);
        jumbotron.setPadding(new Insets(30, 35, 30, 35));
        jumbotron.setStyle("-fx-background-color: linear-gradient(to bottom right, #fdfefe, #f0f4f8); " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 8; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        return jumbotron;
    }

    private HBox createStatPanels() {
        VBox panel1 = createStatPanel("总注册用户", "1,284", PRIMARY, "用户图标");
        VBox panel2 = createStatPanel("本月营收", "¥56.8K", SUCCESS, "收入图标");
        VBox panel3 = createStatPanel("待处理订单", "3,672", WARNING, "订单图标");
        VBox panel4 = createStatPanel("系统正常运行率", "99.7%", DANGER, "运行图标");

        HBox statPanels = new HBox(15, panel1, panel2, panel3, panel4);
        statPanels.setPadding(new Insets(0, 0, 10, 0));
        return statPanels;
    }

    private VBox createStatPanel(String label, String value, Color color, String iconText) {
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        valueLabel.setTextFill(WHITE);

        Label titleLabel = new Label(label);
        titleLabel.setFont(Font.font("Arial", 13));
        titleLabel.setTextFill(Color.web("rgba(255,255,255,0.8)"));

        Label icon = new Label(iconText);
        icon.setFont(Font.font("Arial", 48));
        icon.setTextFill(Color.web("rgba(255,255,255,0.3)"));

        StackPane iconPane = new StackPane(icon);
        iconPane.setAlignment(Pos.CENTER_RIGHT);

        HBox content = new HBox(20, new VBox(valueLabel, titleLabel), iconPane);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        VBox panel = new VBox(content);
        panel.setPadding(new Insets(22, 24, 22, 24));
        panel.setStyle("-fx-background-color: " + toHex(color) + "; " +
                "-fx-background-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.08), 6, 0, 0, 2);");

        return panel;
    }

    private VBox createAnnouncementPanel() {
        Label title = new Label("系统公告与动态");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 0 0 1 0;");

        // 简化的轮播内容
        VBox content = new VBox(10);
        content.setPadding(new Insets(20, 30, 20, 30));
        content.setStyle("-fx-background-color: linear-gradient(to bottom right, #e8f4fd, #cce5ff);");

        Label announcementTitle = new Label("系统升级通知");
        announcementTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        announcementTitle.setTextFill(Color.web("#1a5276"));

        Label announcementDesc = new Label("平台将于本周六凌晨2:00-6:00进行例行维护升级，届时部分功能可能短暂不可用，请提前做好安排。");
        announcementDesc.setWrapText(true);
        announcementDesc.setTextFill(Color.web("#1a5276"));

        content.getChildren().addAll(announcementTitle, announcementDesc);

        VBox panel = new VBox(title, content);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private HBox createTableAndProgress() {
        // 表格面板
        VBox tablePanel = createTablePanel();

        // 进度条面板
        VBox progressPanel = createProgressPanel();

        HBox tableAndProgress = new HBox(20, tablePanel, progressPanel);
        HBox.setHgrow(tablePanel, Priority.ALWAYS);
        HBox.setHgrow(progressPanel, Priority.ALWAYS);

        return tableAndProgress;
    }

    private VBox createTablePanel() {
        Label title = new Label("最近订单记录");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 0 0 1 0;");

        TableView<String[]> table = new TableView<>();
        table.setPrefHeight(250);

        TableColumn<String[], String> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[0]));
        idCol.setPrefWidth(60);

        TableColumn<String[], String> nameCol = new TableColumn<>("客户名称");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[1]));
        nameCol.setPrefWidth(100);

        TableColumn<String[], String> productCol = new TableColumn<>("产品");
        productCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[2]));
        productCol.setPrefWidth(120);

        TableColumn<String[], String> amountCol = new TableColumn<>("金额");
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[3]));
        amountCol.setPrefWidth(80);

        TableColumn<String[], String> statusCol = new TableColumn<>("状态");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[4]));
        statusCol.setPrefWidth(80);

        TableColumn<String[], String> dateCol = new TableColumn<>("日期");
        dateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()[5]));
        dateCol.setPrefWidth(100);

        table.getColumns().addAll(idCol, nameCol, productCol, amountCol, statusCol, dateCol);

        table.getItems().addAll(
                new String[]{"1001", "张三", "企业版套餐", "¥12,800", "已完成", "2026-07-07"},
                new String[]{"1002", "李四", "标准版套餐", "¥6,800", "处理中", "2026-07-06"},
                new String[]{"1003", "王五", "基础版套餐", "¥2,800", "已发货", "2026-07-05"},
                new String[]{"1004", "赵六", "企业版套餐", "¥12,800", "已取消", "2026-07-04"},
                new String[]{"1005", "陈七", "定制方案", "¥38,000", "已完成", "2026-07-03"}
        );

        table.setStyle("-fx-background-color: white; -fx-table-cell-border-color: #e0e5ea;");

        Label footer = new Label("共 5 条记录 | 查看全部 >");
        footer.setTextFill(PRIMARY);
        footer.setFont(Font.font("Arial", 13));
        footer.setPadding(new Insets(10, 18, 10, 18));
        footer.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 1 0 0 0;");

        VBox panel = new VBox(title, table, footer);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private VBox createProgressPanel() {
        Label title = new Label("项目进度概览");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 0 0 1 0;");

        VBox progressBars = new VBox(15);
        progressBars.setPadding(new Insets(20, 18, 20, 18));

        progressBars.getChildren().addAll(
                createProgressBar("产品研发", 78, SUCCESS),
                createProgressBar("市场推广", 55, INFO),
                createProgressBar("客户服务升级", 92, WARNING),
                createProgressBar("安全审计", 34, DANGER)
        );

        // 列表组
        VBox listGroup = new VBox(0);
        listGroup.setStyle("-fx-border-color: #e8ecf1; -fx-border-width: 1 0 0 0;");

        listGroup.getChildren().addAll(
                createListItem("前端重构", "已完成", SUCCESS),
                createListItem("API对接", "进行中", WARNING),
                createListItem("压力测试", "待开始", DANGER)
        );

        VBox panel = new VBox(title, progressBars, listGroup);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private VBox createProgressBar(String label, int percent, Color color) {
        Label titleLabel = new Label(label + "  " + percent + "%");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setTextFill(GRAY_DARK);

        ProgressBar progressBar = new ProgressBar(percent / 100.0);
        progressBar.setPrefWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(18);
        progressBar.setStyle("-fx-accent: " + toHex(color) + "; -fx-background-radius: 10;");

        VBox container = new VBox(5, titleLabel, progressBar);
        return container;
    }

    private HBox createListItem(String title, String status, Color color) {
        Label icon = new Label("•");
        icon.setTextFill(color);
        icon.setPrefWidth(20);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(GRAY_DARK);

        Label statusLabel = new Label(status);
        statusLabel.setTextFill(color);
        statusLabel.setFont(Font.font("Arial", 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox container = new HBox(10, icon, titleLabel, spacer, statusLabel);
        container.setPadding(new Insets(12, 18, 12, 18));
        container.setStyle("-fx-background-color: white;");

        return container;
    }

    private HBox createFormAndAlerts() {
        // 表单面板
        VBox formPanel = createFormPanel();

        // 警告框和标签
        VBox alertsAndLabels = createAlertsAndLabels();

        HBox formAndAlerts = new HBox(20, formPanel, alertsAndLabels);
        HBox.setHgrow(formPanel, Priority.ALWAYS);
        HBox.setHgrow(alertsAndLabels, Priority.ALWAYS);

        return formAndAlerts;
    }

    private VBox createFormPanel() {
        Label title = new Label("快速添加用户");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setTextFill(WHITE);
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: " + toHex(PRIMARY) + "; -fx-background-radius: 6 6 0 0;");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20, 18, 20, 18));

        form.getChildren().addAll(
                createFormField("姓名", "请输入姓名"),
                createFormField("邮箱地址", "请输入邮箱"),
                createRoleField(),
                createCheckBoxField("发送欢迎邮件"),
                createTextAreaField("备注", "可选备注信息"),
                createFormButtons()
        );

        VBox panel = new VBox(title, form);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private VBox createFormField(String label, String prompt) {
        Label fieldLabel = new Label(label);
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        fieldLabel.setTextFill(GRAY_DARK);

        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(getInputStyle());

        return new VBox(5, fieldLabel, field);
    }

    private VBox createRoleField() {
        Label fieldLabel = new Label("角色");
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        fieldLabel.setTextFill(GRAY_DARK);

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("普通用户", "编辑", "管理员", "超级管理员");
        comboBox.setPromptText("选择角色");
        comboBox.setStyle(getInputStyle());

        return new VBox(5, fieldLabel, comboBox);
    }

    private CheckBox createCheckBoxField(String text) {
        CheckBox checkBox = new CheckBox(text);
        checkBox.setFont(Font.font("Arial", 13));
        return checkBox;
    }

    private VBox createTextAreaField(String label, String prompt) {
        Label fieldLabel = new Label(label);
        fieldLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        fieldLabel.setTextFill(GRAY_DARK);

        TextArea textArea = new TextArea();
        textArea.setPromptText(prompt);
        textArea.setPrefRowCount(2);
        textArea.setStyle(getInputStyle());

        return new VBox(5, fieldLabel, textArea);
    }

    private HBox createFormButtons() {
        Button submitBtn = new Button("添加用户");
        submitBtn.setStyle(getButtonStyle(PRIMARY, WHITE));

        Button resetBtn = new Button("重置");
        resetBtn.setStyle(getButtonStyle(Color.web("#f8f9fa"), GRAY_DARK));

        return new HBox(10, submitBtn, resetBtn);
    }

    private VBox createAlertsAndLabels() {
        VBox container = new VBox(15);

        // 警告框
        container.getChildren().addAll(
                createAlert("成功！数据已成功保存到服务器。", SUCCESS),
                createAlert("提示：系统将在30分钟后进行自动备份。", INFO),
                createAlert("警告！磁盘空间不足，请及时清理。", WARNING)
        );

        // 标签和徽章面板
        container.getChildren().add(createLabelsAndBadgesPanel());

        // Well组件
        container.getChildren().add(createWellComponent());

        return container;
    }

    private HBox createAlert(String text, Color color) {
        Label alert = new Label(text);
        alert.setWrapText(true);
        alert.setTextFill(WHITE);
        alert.setFont(Font.font("Arial", 14));
        alert.setPadding(new Insets(12, 15, 12, 15));
        alert.setStyle("-fx-background-color: " + toHex(color) + "; " +
                "-fx-background-radius: 4;");

        Button closeBtn = new Button("×");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16;");
        closeBtn.setOnAction(e -> alert.setVisible(false));

        HBox alertBox = new HBox(10, alert, closeBtn);
        alertBox.setAlignment(Pos.CENTER_LEFT);
        alertBox.setStyle("-fx-background-color: " + toHex(color) + "; " +
                "-fx-background-radius: 4; " +
                "-fx-padding: 0 10 0 0;");

        return alertBox;
    }

    private VBox createLabelsAndBadgesPanel() {
        Label title = new Label("标签与徽章示例");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 0 0 1 0;");

        VBox content = new VBox(15);
        content.setPadding(new Insets(18, 18, 18, 18));

        // 标签
        HBox labels = new HBox(10);
        labels.getChildren().addAll(
                createLabel("默认", Color.web("#777")),
                createLabel("主要", PRIMARY),
                createLabel("成功", SUCCESS),
                createLabel("信息", INFO),
                createLabel("警告", WARNING),
                createLabel("危险", DANGER)
        );

        // 徽章按钮
        HBox badges = new HBox(10);
        Button msgBtn = new Button("消息 8");
        msgBtn.setStyle(getButtonStyle(PRIMARY, WHITE));

        Button pendingBtn = new Button("待处理 12");
        pendingBtn.setStyle(getButtonStyle(DANGER, WHITE));

        badges.getChildren().addAll(msgBtn, pendingBtn);

        content.getChildren().addAll(labels, badges);

        VBox panel = new VBox(title, content);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private Label createLabel(String text, Color color) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        label.setTextFill(WHITE);
        label.setPadding(new Insets(4, 9, 4, 9));
        label.setStyle("-fx-background-color: " + toHex(color) + "; -fx-background-radius: 3;");
        return label;
    }

    private VBox createWellComponent() {
        Label title = new Label("快速笔记 (Well组件)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        title.setTextFill(GRAY_DARK);

        Label content = new Label("这是一个使用 well 类创建的凹陷容器，适合放置备注信息或提示内容。Bootstrap 3 特有组件，在 BS4/5 中已被移除。");
        content.setWrapText(true);
        content.setTextFill(Color.web("#666"));

        Label footer = new Label("— 最后更新于 2026-07-08");
        footer.setFont(Font.font("Arial", 12));
        footer.setTextFill(GRAY_MEDIUM);

        VBox well = new VBox(8, title, content, footer);
        well.setPadding(new Insets(20));
        well.setStyle("-fx-background-color: #f5f5f5; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.03), 2, 0, 0, 1);");

        return well;
    }

    private VBox createThumbnailPanel() {
        Label title = new Label("产品缩略图展示 (Thumbnails)");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        title.setPadding(new Insets(14, 18, 14, 18));
        title.setStyle("-fx-background-color: #fafbfc; -fx-border-color: #e8ecf1; -fx-border-width: 0 0 1 0;");

        HBox thumbnails = new HBox(15);
        thumbnails.setPadding(new Insets(20, 18, 20, 18));

        String[] titles = {"云服务", "存储方案", "数据分析", "安全服务", "全球加速", "技术支持"};
        String[] descs = {"弹性计算", "安全可靠", "智能洞察", "全面防护", "CDN网络", "7×24小时"};
        Color[] colors = {PRIMARY, SUCCESS, WARNING, DANGER, Color.web("#8e44ad"), Color.web("#1abc9c")};

        for (int i = 0; i < titles.length; i++) {
            thumbnails.getChildren().add(createThumbnail(titles[i], descs[i], colors[i]));
        }

        VBox panel = new VBox(title, thumbnails);
        panel.setStyle("-fx-background-color: white; " +
                "-fx-background-radius: 6; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        return panel;
    }

    private VBox createThumbnail(String title, String desc, Color color) {
        StackPane iconPane = new StackPane();
        iconPane.setStyle("-fx-background-color: " + toHex(color) + "20; -fx-background-radius: 4 4 0 0;");
        iconPane.setPrefSize(120, 100);

        Label icon = new Label("●");
        icon.setFont(Font.font("Arial", 40));
        icon.setTextFill(color);
        iconPane.getChildren().add(icon);

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setTextFill(GRAY_DARK);
        titleLabel.setPadding(new Insets(8, 0, 2, 0));

        Label descLabel = new Label(desc);
        descLabel.setFont(Font.font("Arial", 12));
        descLabel.setTextFill(GRAY_MEDIUM);

        VBox thumbnail = new VBox(iconPane, titleLabel, descLabel);
        thumbnail.setAlignment(Pos.CENTER);
        thumbnail.setStyle("-fx-background-color: white; " +
                "-fx-border-color: #e8ecf1; " +
                "-fx-border-radius: 6; " +
                "-fx-background-radius: 6; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 4, 0, 0, 1);");

        return thumbnail;
    }

    private HBox createPagination() {
        HBox pagination = new HBox(5);
        pagination.setAlignment(Pos.CENTER);

        Button prevBtn = new Button("«");
        prevBtn.setDisable(true);
        prevBtn.setStyle(getPaginationButtonStyle());

        for (int i = 1; i <= 5; i++) {
            Button pageBtn = new Button(String.valueOf(i));
            if (i == 1) {
                pageBtn.setStyle(getActivePaginationButtonStyle());
            } else {
                pageBtn.setStyle(getPaginationButtonStyle());
            }
            pagination.getChildren().add(pageBtn);
        }

        Button nextBtn = new Button("»");
        nextBtn.setStyle(getPaginationButtonStyle());
        pagination.getChildren().add(nextBtn);

        Label info = new Label("共 5 页，总计 48 条记录");
        info.setTextFill(GRAY_MEDIUM);
        info.setFont(Font.font("Arial", 13));

        VBox container = new VBox(8, pagination, info);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(15, 0, 0, 0));

        return new HBox(container);
    }

    // ==================== 辅助方法 ====================

    private String getInputStyle() {
        return "-fx-background-color: white; -fx-border-color: #ced4da; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 8 12; -fx-font-size: 14;";
    }

    private String getButtonStyle(Color bgColor, Color textColor) {
        return "-fx-background-color: " + toHex(bgColor) + "; -fx-text-fill: " + toHex(textColor) + "; -fx-background-radius: 4; -fx-padding: 8 16;";
    }

    private String getPaginationButtonStyle() {
        return "-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 12;";
    }

    private String getActivePaginationButtonStyle() {
        return "-fx-background-color: " + toHex(PRIMARY) + "; -fx-text-fill: white; -fx-background-radius: 4; -fx-padding: 6 12;";
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    private Color darken(Color color) {
        return Color.color(
                color.getRed() * 0.8,
                color.getGreen() * 0.8,
                color.getBlue() * 0.8
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}