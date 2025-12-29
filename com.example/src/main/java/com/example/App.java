package com.example;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Optional;

public class App extends Application {

    // 实例化你的后端系统
    private FlightManagementSystem system = new FlightManagementSystem();

    // 布局容器
    private BorderPane rootLayout;
    private VBox centerContent;

    @Override
    public void start(Stage stage) {
        // --- 1. 初始化一些测试数据 (可选，让你一运行就能看到东西) ---
        initTestData();

        // --- 2. 主布局 ---
        rootLayout = new BorderPane();

        // 创建左侧菜单
        VBox sideMenu = createSideMenu();
        rootLayout.setLeft(sideMenu);

        // 创建中间内容区域
        centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        rootLayout.setCenter(centerContent);

        // 默认显示仪表盘
        showDashboard();

        // --- 3. 舞台设置 ---
        Scene scene = new Scene(rootLayout, 1000, 600); // 宽1000，高600
        // 加载 CSS 样式表
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setTitle("Flight Management System (GUI Version)");
        stage.setScene(scene);
        stage.show();
    }

    // =================================================
    // A. 侧边栏菜单 (Side Menu)
    // =================================================
    private VBox createSideMenu() {
        VBox menu = new VBox(0);
        menu.setPadding(new Insets(20));
        menu.getStyleClass().add("sidebar"); // 🔥 贴上 sidebar 标签
        menu.setPrefWidth(220);

        Label title = new Label("FMS System");
        title.getStyleClass().add("sidebar-title"); // 🔥 贴上 sidebar-title 标签

        // 导航按钮
        Button btnDashboard = createMenuButton("Dashboard");
        Button btnAircraft = createMenuButton("Aircraft Management");
        Button btnFlight = createMenuButton("Flight Management");
        Button btnReports = createMenuButton("Reports & Logs");
        Button btnExit = createMenuButton("Exit");

        // 按钮点击事件
        btnDashboard.setOnAction(e -> showDashboard());
        btnAircraft.setOnAction(e -> showAircraftView());
        btnFlight.setOnAction(e -> showFlightView());
        btnReports.setOnAction(e -> showReportsView());
        btnExit.setOnAction(e -> System.exit(0));

        menu.getChildren().addAll(title, new Separator(), btnDashboard, btnAircraft, btnFlight, btnReports,
                new Separator(),
                btnExit);
        return menu;
    }

    // 找到 createMenuButton 方法，彻底重写：
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("menu-btn"); // 🔥 核心：只用这一句代码应用样式
        return btn;
    }

    // =================================================
    // B. 视图 1: 仪表盘 (Dashboard)
    // =================================================
    // =================================================
    // =================================================
    // B. 视图 1: 仪表盘 (修复版)
    // =================================================
    @SuppressWarnings("unchecked")
    private void showDashboard() {
        // 1. 最重要的一步：清空之前的内容！防止重复添加报错
        centerContent.getChildren().clear();

        // --- 顶部标题 ---
        Label header = new Label("Operational Dashboard");
        header.getStyleClass().add("content-header");

        // --- 统计数据计算 ---
        int totalFlights = system.getAllFlights().size();
        int totalAircraft = system.getAllAircrafts().size();
        long activeFlights = system.getAllFlights().stream()
                .filter(f -> "Departed".equals(f.getStatus()) || "In Flight".equals(f.getStatus()))
                .count();

        // --- 统计卡片区域 (居中 + 大间距) ---
        HBox statsBox = new HBox(50); // 间距 50
        statsBox.setAlignment(Pos.CENTER); // 整体居中
        statsBox.getChildren().addAll(
                createStatCard("Total Aircraft", String.valueOf(totalAircraft)),
                createStatCard("Scheduled Flights", String.valueOf(totalFlights)),
                createStatCard("Active In-Air", String.valueOf(activeFlights)));

        // --- 实时看板标题 ---
        Label listHeader = new Label("Live Flight Status Board");
        listHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        listHeader.setPadding(new Insets(20, 0, 10, 0)); // 上间距30，下间距20

        // --- 实时看板表格 ---
        TableView<Flight> statusTable = new TableView<>();
        statusTable.getStyleClass().add("live-board"); // 应用圆角 CSS
        statusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // 自动铺满宽度
        statusTable.setPrefHeight(400); // 设置高度

        // --- 定义列 (只定义一次！) ---
        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        TableColumn<Flight, String> colRoute = new TableColumn<>("Route");
        colRoute.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getOrigin() + " ➔ " + cellData.getValue().getDestination()));

        TableColumn<Flight, String> colTime = new TableColumn<>("Departure");
        colTime.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDepartureTime().toString().replace("T", " ")));

        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 给状态列加颜色逻辑
        colStatus.setCellFactory(column -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Delayed") || item.equalsIgnoreCase("Cancelled")) {
                        setStyle("-fx-text-fill: #ff1900ff; -fx-font-weight: bold;"); // 红
                    } else if (item.equalsIgnoreCase("Departed") || item.equalsIgnoreCase("In Flight")) {
                        setStyle("-fx-text-fill: #0fcd5eff; -fx-font-weight: bold;"); // 绿
                    } else if (item.equalsIgnoreCase("Arrived")) {
                        setStyle("-fx-text-fill: #95a5a6;"); // 灰
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;"); // 默认深蓝
                    }
                }
            }
        });

        // --- 填充数据 ---
        statusTable.setItems(FXCollections.observableArrayList(system.getAllFlights()));

        // ============================================================
        // 🔥【在此处插入】圆角剪裁代码
        // ============================================================
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(30); // 圆角大小
        clip.setArcHeight(30);

        // 让剪裁区域永远跟随表格大小变化
        clip.widthProperty().bind(statusTable.widthProperty());
        clip.heightProperty().bind(statusTable.heightProperty());

        // 应用剪裁
        statusTable.setClip(clip);
        // ============================================================

        // --- 最终组装 (只添加一次！) ---
        centerContent.getChildren().addAll(header, statsBox, listHeader, statusTable);

        // --- 添加列到表格 (只添加一次！) ---
        statusTable.getColumns().addAll(colNo, colRoute, colTime, colStatus);

        // --- 填充数据 ---
        statusTable.setItems(FXCollections.observableArrayList(system.getAllFlights()));
    }

    // 找到 createStatCard 方法，修改成这样：
    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card"); // 🔥 应用 card 样式
        card.setPrefWidth(220); // 稍微宽一点
        card.setAlignment(Pos.CENTER);

        Label lblValue = new Label(value);
        lblValue.getStyleClass().add("card-value"); // 🔥 应用数值样式

        Label lblTitle = new Label(title);
        lblTitle.getStyleClass().add("card-title"); // 🔥 应用标题样式

        card.getChildren().addAll(lblValue, lblTitle);
        return card;
    }

    // =================================================
    // C. 视图 2: 飞机管理 (Aircraft Management)
    // =================================================
    @SuppressWarnings("unchecked")
    private void showAircraftView() {
        centerContent.getChildren().clear();

        Label header = new Label("Aircraft Management");
        header.getStyleClass().add("content-header");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // 1. 创建表格
        TableView<Aircraft> table = new TableView<>();

        // 🔥【关键步骤 1】应用和 Dashboard 一样的漂亮样式
        // 这样它就会有斑马纹、大间距和居中文字
        table.getStyleClass().add("live-board");

        // 让列宽自适应铺满
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // 🔥【关键 1】让表格占满剩下的垂直空间，把按钮挤到最下面
        VBox.setVgrow(table, Priority.ALWAYS);

        // 定义列 (注意: 你的Aircraft类必须有 getRegistrationNumber() 等方法)
        TableColumn<Aircraft, String> colReg = new TableColumn<>("Reg No.");
        colReg.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));

        TableColumn<Aircraft, String> colBrand = new TableColumn<>("Brand");
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));

        TableColumn<Aircraft, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Aircraft, Integer> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacity"));

        TableColumn<Aircraft, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 给状态列加颜色 (可选，为了好看)
        colStatus.setCellFactory(column -> new TableCell<Aircraft, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Available".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // 绿
                    } else if ("Maintenance".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // 红
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;");
                    }
                }
            }
        });

        table.getColumns().addAll(colReg, colBrand, colModel, colCap, colStatus);

        // 填充数据
        refreshAircraftTable(table);

        // 2. 操作按钮栏
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAdd = new Button("Add New Aircraft");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");

        Button btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");

        // 添加逻辑
        btnAdd.setOnAction(e -> {
            showAddAircraftDialog();
            refreshAircraftTable(table); // 刷新表格
        });

        // 删除逻辑
        btnDelete.setOnAction(e -> {
            Aircraft selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                system.deleteAircraft(selected.getRegistrationNumber());
                refreshAircraftTable(table);
            } else {
                showAlert("Warning", "Please select an aircraft to delete.");
            }
        });

        actions.getChildren().addAll(btnAdd, btnDelete);

        // 圆角剪裁
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        clip.widthProperty().bind(table.widthProperty());
        clip.heightProperty().bind(table.heightProperty());
        table.setClip(clip);

        // 🔥【关键 3】改变添加顺序：Header -> Table -> Actions (按钮在最后)
        centerContent.getChildren().addAll(header, table, actions);
    }

    private void refreshAircraftTable(TableView<Aircraft> table) {
        ObservableList<Aircraft> data = FXCollections.observableArrayList(system.getAllAircrafts());
        table.setItems(data);
    }

    // =================================================
    // D. 视图 3: 航班管理 (Flight Management)
    // =================================================
    private void showFlightView() {
        centerContent.getChildren().clear();

        Label header = new Label("Flight Management");
        header.getStyleClass().add("content-header");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // 1. 创建表格
        TableView<Flight> table = new TableView<>();

        // 🔥【关键 1】应用统一的样式 (斑马纹、大行距)
        table.getStyleClass().add("live-board");
        // 让列宽自适应
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        // 🔥【关键 1】让表格占满剩下的垂直空间，把按钮挤到最下面
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        TableColumn<Flight, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));

        TableColumn<Flight, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 显示飞机的注册号 (因为 flight.getAircraft() 返回的是对象，我们需要字符串)
        TableColumn<Flight, String> colPlane = new TableColumn<>("Aircraft");
        colPlane.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getAircraft().getRegistrationNumber()));

        // 给状态列加颜色 (保持和 Dashboard 一致)
        colStatus.setCellFactory(column -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Delayed") || item.equalsIgnoreCase("Cancelled")) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // 红
                    } else if (item.equalsIgnoreCase("Departed") || item.equalsIgnoreCase("In Flight")) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // 绿
                    } else if (item.equalsIgnoreCase("Arrived")) {
                        setStyle("-fx-text-fill: #95a5a6;"); // 灰
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;"); // 默认蓝
                    }
                }
            }
        });

        table.getColumns().addAll(colNo, colOrigin, colDest, colStatus, colPlane);
        refreshFlightTable(table);

        // 2. 按钮栏
        HBox actions = new HBox(10);
        // 🔥【关键 2】设置为靠右对齐
        actions.setAlignment(Pos.CENTER_RIGHT);
        // 增加一点上边距，不要紧贴表格
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAdd = new Button("Add Flight");
        btnAdd.getStyleClass().addAll("btn", "btn-primary"); // 蓝色按钮

        Button btnStatus = new Button("Update Status");
        btnStatus.getStyleClass().addAll("btn", "btn-secondary"); // 灰色按钮

        Button btnCancel = new Button("Cancel Flight");
        btnCancel.getStyleClass().addAll("btn", "btn-danger"); // 红色按钮

        btnAdd.setOnAction(e -> {
            showAddFlightDialog();
            refreshFlightTable(table);
        });

        btnStatus.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showUpdateStatusDialog(selected);
                refreshFlightTable(table);
            } else {
                showAlert("Select Flight", "Please select a flight first.");
            }
        });

        btnCancel.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                system.deleteFlight(selected.getFlightNumber());
                refreshFlightTable(table);
            }
        });

        actions.getChildren().addAll(btnAdd, btnStatus, btnCancel);

        // ============================================================
        // 🔥【关键 2】给表格加上圆角剪裁 (Clip)
        // ============================================================
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        clip.widthProperty().bind(table.widthProperty());
        clip.heightProperty().bind(table.heightProperty());
        table.setClip(clip);
        // ============================================================

        centerContent.getChildren().addAll(header, table, actions);
    }

    private void refreshFlightTable(TableView<Flight> table) {
        ObservableList<Flight> data = FXCollections.observableArrayList(system.getAllFlights());
        table.setItems(data);
    }

    // =================================================
    // E. 弹窗输入逻辑 (Dialogs) - 代替 Scanner
    // =================================================

    // 弹窗：添加飞机
    // 弹窗：添加飞机 (修正版：只负责添加飞机)
    private void showAddAircraftDialog() {
        Dialog<Aircraft> dialog = new Dialog<>();
        dialog.setTitle("Add New Aircraft");
        dialog.setHeaderText("Enter aircraft details");

        ButtonType loginButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField regField = new TextField();
        regField.setPromptText("Reg No (e.g. 9M-ABC)");
        TextField brandField = new TextField();
        TextField modelField = new TextField();
        TextField capField = new TextField();

        grid.add(new Label("Reg No:"), 0, 0);
        grid.add(regField, 1, 0);
        grid.add(new Label("Brand:"), 0, 1);
        grid.add(brandField, 1, 1);
        grid.add(new Label("Model:"), 0, 2);
        grid.add(modelField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3);
        grid.add(capField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                try {
                    int cap = Integer.parseInt(capField.getText());
                    // 返回一个新的飞机对象
                    return new Aircraft(regField.getText(), brandField.getText(), modelField.getText(), cap,
                            "Available");
                } catch (Exception ex) {
                    showAlert("Error", "Invalid Input: Capacity must be a number.");
                    return null;
                }
            }
            return null;
        });

        Optional<Aircraft> result = dialog.showAndWait();
        result.ifPresent(aircraft -> system.addAircraft(aircraft));
    }

    // 弹窗：添加航班
    // 弹窗：添加航班 (修正版：包含 CargoFlight 继承演示)
    private void showAddFlightDialog() {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Create a new flight schedule");

        ButtonType addButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField flightNoField = new TextField();
        TextField destField = new TextField();
        ComboBox<String> aircraftBox = new ComboBox<>();
        
        // --- 新增：货运选项 (Inheritance Demo) ---
        CheckBox cargoCheckbox = new CheckBox("Is Cargo Flight?");
        TextField cargoWeightField = new TextField();
        cargoWeightField.setPromptText("Cargo Weight (kg)");
        cargoWeightField.setDisable(true); // 默认禁用

        // 勾选后才允许输入重量
        cargoCheckbox.setOnAction(e -> {
            cargoWeightField.setDisable(!cargoCheckbox.isSelected());
        });

        // 只能选择 Available 的飞机
        for (Aircraft a : system.getAllAircrafts()) {
            if ("Available".equalsIgnoreCase(a.getStatus())) {
                aircraftBox.getItems().add(a.getRegistrationNumber());
            }
        }

        grid.add(new Label("Flight No:"), 0, 0);
        grid.add(flightNoField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1);
        grid.add(destField, 1, 1);
        grid.add(new Label("Assign Aircraft:"), 0, 2);
        grid.add(aircraftBox, 1, 2);
        // 添加 Cargo 控件
        grid.add(cargoCheckbox, 0, 3);
        grid.add(cargoWeightField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String reg = aircraftBox.getValue();
                Aircraft plane = system.getAircraft(reg);
                
                if (plane != null) {
                    try {
                        // 自动生成时间 (简化演示)
                        LocalDateTime dep = LocalDateTime.now().plusHours(2);
                        LocalDateTime arr = dep.plusHours(2);
                        String fNum = flightNoField.getText();
                        String dest = destField.getText();

                        // ★★★ 关键点：根据勾选创建不同对象 (体现继承和多态) ★★★
                        if (cargoCheckbox.isSelected()) {
                            // 必须确保 cargoWeightField 输入了数字
                            double weight = Double.parseDouble(cargoWeightField.getText());
                            // 创建子类 CargoFlight 对象
                            return new CargoFlight(fNum, "Batu Pahat", dest, dep, arr, plane, weight);
                        } else {
                            // 创建父类 Flight 对象
                            return new Flight(fNum, "Batu Pahat", dest, dep, arr, plane);
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Invalid Input", "Please enter a valid number for Cargo Weight.");
                        return null; // 或者抛出异常阻止关闭，但在 Dialogconverter 里返回 null 最简单
                    }
                }
            }
            return null;
        });

        Optional<Flight> result = dialog.showAndWait();
        result.ifPresent(flight -> {
            // 这里体现多态：system.addFlight 接收 Flight，但我们可能传入了 CargoFlight
            system.addFlight(flight);
            // 更新飞机状态
            flight.getAircraft().setStatus("Scheduled");
        });
    }

    // 弹窗：更新状态
    private void showUpdateStatusDialog(Flight flight) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(flight.getStatus(), "Boarding", "Departed", "Arrived",
                "Delayed", "Cancelled");
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update status for flight " + flight.getFlightNumber());
        dialog.setContentText("Choose new status:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStatus -> {
            system.updateFlightStatus(flight.getFlightNumber(), newStatus);
            // 这里可以添加逻辑：如果 Delayed，再弹出一个 TextInputDialog 询问原因
            if ("Delayed".equals(newStatus)) {
                TextInputDialog reasonDialog = new TextInputDialog();
                reasonDialog.setHeaderText("Enter Delay Reason");
                reasonDialog.showAndWait().ifPresent(reason -> flight.addDelayReason(reason));
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // 初始化假数据
    // 初始化假数据
    private void initTestData() {
        // 1. 先加飞机
        Aircraft a1 = new Aircraft("9M-AAA", "Boeing", "737", 180, "Available");
        Aircraft a2 = new Aircraft("9M-BBB", "Airbus", "A320", 150, "Available");
        system.addAircraft(a1);
        system.addAircraft(a2);

        // 2. 再加几个测试航班 (这样你的看板就不会是空的了)
        // 注意：这里需要 Flight 的构造函数参数匹配你的类定义。
        // 假设你的 Flight 构造函数是: (flightNo, origin, dest, depTime, arrTime, aircraft)

        LocalDateTime now = LocalDateTime.now();

        Flight f1 = new Flight("BP-001", "Batu Pahat", "KLIA",
                now.plusHours(1), now.plusHours(2), a1);
        f1.setStatus("Boarding"); // 设置个状态看看颜色

        Flight f2 = new Flight("BP-002", "Batu Pahat", "Johor Bahru",
                now.minusHours(2), now.minusHours(1), a2);
        f2.setStatus("Arrived");

        system.addFlight(f1);
        system.addFlight(f2);

        // 更新飞机状态以匹配航班
        a1.setStatus("Scheduled");
        a2.setStatus("Available");
    }

    // =================================================
    // F. 视图 4: 报告与日志 (Reports View)
    // =================================================
    private void showReportsView() {
        centerContent.getChildren().clear();

        Label header = new Label("Reports & Logs");
        header.getStyleClass().add("content-header");
        header.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        // 创建一个大文本区域来显示报告
        TextArea reportArea = new TextArea();
        reportArea.setEditable(false); // 只读，不能修改
        reportArea.setFont(Font.font("Monospaced", 14)); // 用等宽字体，不然表格会对不齐
        // 🔥【关键 1】应用一点样式，去掉默认的蓝框，让它看起来像张白纸
        reportArea.setStyle("-fx-control-inner-background: white; -fx-background-insets: 0; -fx-padding: 10px;");
        // 🔥【关键 2】让文本框占满剩下的垂直空间
        VBox.setVgrow(reportArea, Priority.ALWAYS);
        // 默认先显示分析
        reportArea.setText(system.getOperationalAnalysisReport());

        // 按钮栏：切换看哪个报告
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAnalysis = new Button("Operational Analysis");
        btnAnalysis.getStyleClass().addAll("btn", "btn-primary"); // 蓝色
        Button btnHistory = new Button("Arrived Flights History");
        btnHistory.getStyleClass().addAll("btn", "btn-secondary"); // 灰色

        // 点击 "Operational Analysis" 显示运营分析
        btnAnalysis.setOnAction(e -> {
            reportArea.setText(system.getOperationalAnalysisReport());
        });

        // 点击 "History" 显示历史记录
        btnHistory.setOnAction(e -> {
            reportArea.setText(system.getFlightHistoryReport());
        });

        actions.getChildren().addAll(btnAnalysis, btnHistory);

        // ============================================================
        // 🔥【关键 4】给文本框施加圆角剪裁 (Clip)
        // ============================================================
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        clip.widthProperty().bind(reportArea.widthProperty());
        clip.heightProperty().bind(reportArea.heightProperty());
        reportArea.setClip(clip);
        // ============================================================

        centerContent.getChildren().addAll(header, reportArea, actions);
    }

    public static void main(String[] args) {
        launch();
    }
}