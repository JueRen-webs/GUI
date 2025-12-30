package com.example;

// 导入 JavaFX 的各种库，用于画图
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*; // 包含 Button, Label, TextField 等控件
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;  // 包含 VBox, HBox, BorderPane 等布局
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// App 类继承 Application，这是 JavaFX 的标准写法
public class App extends Application {

    private FlightManagementSystem system = new FlightManagementSystem(); // 创建逻辑系统实例
    private BorderPane rootLayout; // 根布局，分上、下、左、右、中
    private VBox centerContent;    // 中间区域的内容，我们将在这里切换不同的视图

    // --- 启动方法 ---
    @Override
    public void start(Stage stage) {
        // 1. 启动时先加载数据
        system.loadData();

        rootLayout = new BorderPane(); // 创建根布局
        VBox sideMenu = createSideMenu(); // 创建左侧菜单
        rootLayout.setLeft(sideMenu);     // 把菜单放在左边

        centerContent = new VBox(20);     // 创建中间区域，元素间距 20
        centerContent.setPadding(new Insets(20)); // 内边距 20
        rootLayout.setCenter(centerContent); // 把中间区域放在中间

        showDashboard(); // 默认显示仪表盘

        Scene scene = new Scene(rootLayout, 1000, 600); // 创建场景，宽1000，高600
        
        // 加载 CSS 样式文件 (美化界面)
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        
        stage.setTitle("Flight Management System (Pro Version)"); // [新手修改]: 修改窗口标题
        stage.setScene(scene);
        stage.show(); // 显示窗口
    }

    // --- 停止方法 (关闭窗口时触发) ---
    @Override
    public void stop() throws Exception {
        system.saveData(); // 自动保存数据
        super.stop();
    }

    // [新增] 用来存放侧边栏的 4 个主要导航按钮，方便后续切换样式
    private List<Button> sideMenuBtns = new ArrayList<>();

    // --- 创建左侧菜单 ---
    private VBox createSideMenu() {
        VBox menu = new VBox(0); 
        menu.setPadding(new Insets(20));
        menu.getStyleClass().add("sidebar"); 
        menu.setPrefWidth(220); 

        Label title = new Label("FMS System"); 
        title.getStyleClass().add("sidebar-title");

        // 1. 创建按钮
        Button btnDashboard = createMenuButton("Dashboard");
        Button btnAircraft = createMenuButton("Aircraft Management");
        Button btnFlight = createMenuButton("Flight Management");
        Button btnReports = createMenuButton("Reports & Analytics");
        Button btnExit = createMenuButton("Exit");

        // 2. [关键] 把需要高亮切换的按钮存入列表 (Exit 按钮不需要保持高亮，所以不放进去)
        sideMenuBtns.clear(); // 清空一下，防止重复添加
        sideMenuBtns.addAll(Arrays.asList(btnDashboard, btnAircraft, btnFlight, btnReports));

        // 3. 设置点击事件 (点击后 -> 切换界面 -> 更新按钮高亮)
        btnDashboard.setOnAction(e -> { 
            showDashboard(); 
            updateMenuState(btnDashboard); // 高亮自己
        });
        
        btnAircraft.setOnAction(e -> { 
            showAircraftView(); 
            updateMenuState(btnAircraft); 
        });
        
        btnFlight.setOnAction(e -> { 
            showFlightView(); 
            updateMenuState(btnFlight); 
        });
        
        btnReports.setOnAction(e -> { 
            showReportsView(); 
            updateMenuState(btnReports); 
        });
        
        btnExit.setOnAction(e -> {
            system.saveData(); 
            System.exit(0);    
        });

        // 4. [初始化] 程序刚启动时，默认高亮 Dashboard 按钮
        updateMenuState(btnDashboard);

        menu.getChildren().addAll(title, new Separator(), btnDashboard, btnAircraft, btnFlight, btnReports, new Separator(), btnExit);
        return menu;
    }

    // [新增辅助方法] 切换菜单的高亮状态
    private void updateMenuState(Button activeBtn) {
        for (Button btn : sideMenuBtns) {
            // 如果是当前点击的按钮，加上 "active" 样式
            if (btn == activeBtn) {
                if (!btn.getStyleClass().contains("active")) {
                    btn.getStyleClass().add("active");
                }
            } 
            // 如果不是，移除 "active" 样式
            else {
                btn.getStyleClass().remove("active");
            }
        }
    }

    // 辅助方法：快速创建统一风格的菜单按钮
    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE); // 让按钮填满宽度
        btn.getStyleClass().add("menu-btn"); // 应用 CSS
        return btn;
    }

    // ================== 1. 仪表盘视图 (Dashboard) ==================
    private void showDashboard() {
        centerContent.getChildren().clear(); 

        // 1. 页面大标题 (不放按钮了)
        Label header = new Label("Operational Dashboard");
        header.getStyleClass().add("content-header");

        // --- 统计数据 ---
        int totalFlights = system.getAllFlights().size();
        int totalAircraft = system.getAllAircrafts().size();
        long activeFlights = system.getAllFlights().stream()
                .filter(f -> "Departed".equals(f.getStatus()) || "In Flight".equals(f.getStatus()))
                .count();

        HBox statsBox = new HBox(50);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.getChildren().addAll(
                createStatCard("Total Aircraft", String.valueOf(totalAircraft)),
                createStatCard("Scheduled Flights", String.valueOf(totalFlights)),
                createStatCard("Departed", String.valueOf(activeFlights)));

        // --- 表格区域头部 (标题左对齐，按钮右对齐) ---
        HBox tableHeaderBox = new HBox();
        tableHeaderBox.setAlignment(Pos.CENTER_LEFT);
        tableHeaderBox.setPadding(new Insets(20, 0, 10, 0)); // 上边距20，下边距10

        Label listHeader = new Label("Live Flight Status Board");
        listHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // 占位符，把按钮推到最右边

        // 排序按钮
        Button btnSort = new Button("Sort: Ascending ⬆");
        btnSort.getStyleClass().addAll("btn", "btn-secondary");
        btnSort.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;");

        tableHeaderBox.getChildren().addAll(listHeader, spacer, btnSort);

        // --- 表格构建 ---
        TableView<Flight> statusTable = new TableView<>();
        statusTable.getStyleClass().add("live-board"); 
        statusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); 
        statusTable.setPrefHeight(400);

        // 定义列
        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));

        TableColumn<Flight, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDepartureTime().toLocalDate().toString()));

        TableColumn<Flight, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));

        TableColumn<Flight, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<Flight, String> colDepTime = new TableColumn<>("Dep Time (UTC+8)");
        colDepTime.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().getDepartureTime().toLocalTime().toString()));

        TableColumn<Flight, String> colArrTime = new TableColumn<>("Arr Time (UTC+8)");
        colArrTime.setCellValueFactory(cell -> {
            Flight f = cell.getValue();
            String status = f.getStatus();
            LocalDateTime depTime = f.getDepartureTime();
            LocalDateTime realArrTime = f.getArrivalTime(); 
            String timeStr = realArrTime.toLocalTime().toString();
            if (realArrTime.toLocalDate().isAfter(depTime.toLocalDate())) {
                timeStr += " (+1)";
            }
            String displayTime = "";
            if ("Scheduled".equalsIgnoreCase(status) || "Boarding".equalsIgnoreCase(status)|| "Cancelled".equalsIgnoreCase(status)) {
                displayTime = "-"; 
            } else if ("Arrived".equalsIgnoreCase(status)) {
                displayTime = timeStr;
            } else {
                displayTime = "Est: " + timeStr;
            }
            return new SimpleStringProperty(displayTime);
        });

        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(column -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null); setStyle("");
                } else {
                    setText(item);
                    if (item.equalsIgnoreCase("Delayed") || item.equalsIgnoreCase("Cancelled")) {
                        setStyle("-fx-text-fill: #ff1900ff; -fx-font-weight: bold;"); 
                    } else if (item.equalsIgnoreCase("Departed") || item.equalsIgnoreCase("Arrived")||item.equalsIgnoreCase("In Flight")) {
                        setStyle("-fx-text-fill: #0fcd5eff; -fx-font-weight: bold;"); 
                    } else {
                        setStyle("-fx-text-fill: #2c3e50;"); 
                    }
                }
            }
        });

        statusTable.getColumns().addAll(colNo, colDate, colOrigin, colDest, colDepTime, colArrTime, colStatus); 
        statusTable.setItems(FXCollections.observableArrayList(system.getAllFlights())); 

        // 初始排序
        colDate.setSortType(TableColumn.SortType.ASCENDING);
        colDepTime.setSortType(TableColumn.SortType.ASCENDING);
        colDest.setSortType(TableColumn.SortType.ASCENDING);
        colNo.setSortType(TableColumn.SortType.ASCENDING);
        statusTable.getSortOrder().addAll(colDate, colDepTime, colDest, colNo);

        // 按钮点击事件
        btnSort.setOnAction(e -> {
            statusTable.getSortOrder().clear(); 
            if (btnSort.getText().contains("Ascending")) {
                btnSort.setText("Sort: Descending ⬇");
                colDate.setSortType(TableColumn.SortType.DESCENDING);
                colDepTime.setSortType(TableColumn.SortType.DESCENDING);
                colDest.setSortType(TableColumn.SortType.DESCENDING);
                colNo.setSortType(TableColumn.SortType.DESCENDING);
            } else {
                btnSort.setText("Sort: Ascending ⬆");
                colDate.setSortType(TableColumn.SortType.ASCENDING);
                colDepTime.setSortType(TableColumn.SortType.ASCENDING);
                colDest.setSortType(TableColumn.SortType.ASCENDING);
                colNo.setSortType(TableColumn.SortType.ASCENDING);
            }
            statusTable.getSortOrder().addAll(colDate, colDepTime, colDest, colNo);
            statusTable.sort(); 
        });

        applyTableClip(statusTable); 
        // 注意顺序：Header -> Stats -> TableHeader(含按钮) -> Table
        centerContent.getChildren().addAll(header, statsBox, tableHeaderBox, statusTable); 
    }
    // ================== 2. 飞机管理视图 (Aircraft Management) ==================
    private void showAircraftView() {
        centerContent.getChildren().clear();
        
        // 1. 页面大标题
        Label header = new Label("Aircraft Management");
        header.getStyleClass().add("content-header");

        // --- 工具栏区域 (左边搜索框，右边排序按钮) ---
        HBox toolBar = new HBox(10);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(0, 0, 10, 0));

        // 搜索框
        TextField searchField = new TextField();
        searchField.setPromptText("Search Reg No, Brand or Model...");
        searchField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // 排序按钮
        Button btnSort = new Button("Sort: Ascending ⬆");
        btnSort.getStyleClass().addAll("btn", "btn-secondary");
        btnSort.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;");

        toolBar.getChildren().addAll(searchField, spacer, btnSort);

        // --- 数据准备 ---
        ObservableList<Aircraft> masterData = FXCollections.observableArrayList(system.getAllAircrafts());
        FilteredList<Aircraft> filteredData = new FilteredList<>(masterData, p -> true);

        // 搜索逻辑
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(a -> {
                if (newVal == null || newVal.isEmpty()) return true; 
                String lower = newVal.toLowerCase();
                return a.getRegistrationNumber().toLowerCase().contains(lower) 
                    || a.getBrand().toLowerCase().contains(lower) 
                    || a.getModel().toLowerCase().contains(lower);
            });
        });

        SortedList<Aircraft> sortedData = new SortedList<>(filteredData);
        TableView<Aircraft> table = new TableView<>();
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        table.getStyleClass().add("live-board");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS); // 表格占满剩余空间

        // --- 列定义 ---
        
        // 1. 注册号
        TableColumn<Aircraft, String> colReg = new TableColumn<>("Reg No.");
        colReg.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        
        // 2. 品牌
        TableColumn<Aircraft, String> colBrand = new TableColumn<>("Brand");
        colBrand.setCellValueFactory(new PropertyValueFactory<>("brand"));
        
        // 3. 型号
        TableColumn<Aircraft, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(new PropertyValueFactory<>("model"));
        
        // 4. 载客量
        TableColumn<Aircraft, Integer> colCap = new TableColumn<>("Capacity");
        colCap.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        
        // 5. 状态
        TableColumn<Aircraft, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // 状态颜色逻辑
        colStatus.setCellFactory(col -> new TableCell<Aircraft, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    if ("Available".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); // 绿
                    else if ("Maintenance".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); // 红
                    else setStyle("-fx-text-fill: #2c3e50;");
                } else { setText(null); setStyle(""); }
            }
        });

        table.getColumns().addAll(colReg, colBrand, colModel, colCap, colStatus);

        // --- [核心逻辑] 初始排序 ---
        // 规则: Status -> Reg No -> Brand -> Capacity -> Model
        colStatus.setSortType(TableColumn.SortType.ASCENDING);
        colReg.setSortType(TableColumn.SortType.ASCENDING);
        colBrand.setSortType(TableColumn.SortType.ASCENDING);
        colCap.setSortType(TableColumn.SortType.ASCENDING);
        colModel.setSortType(TableColumn.SortType.ASCENDING);
        
        table.getSortOrder().addAll(colStatus, colReg, colBrand, colCap, colModel);

        // --- [核心逻辑] 按钮事件 ---
        btnSort.setOnAction(e -> {
            table.getSortOrder().clear(); // 清空当前规则
            
            if (btnSort.getText().contains("Ascending")) {
                // 切换到降序
                btnSort.setText("Sort: Descending ⬇");
                colStatus.setSortType(TableColumn.SortType.DESCENDING);
                colReg.setSortType(TableColumn.SortType.DESCENDING);
                colBrand.setSortType(TableColumn.SortType.DESCENDING);
                colCap.setSortType(TableColumn.SortType.DESCENDING);
                colModel.setSortType(TableColumn.SortType.DESCENDING);
            } else {
                // 切换到升序
                btnSort.setText("Sort: Ascending ⬆");
                colStatus.setSortType(TableColumn.SortType.ASCENDING);
                colReg.setSortType(TableColumn.SortType.ASCENDING);
                colBrand.setSortType(TableColumn.SortType.ASCENDING);
                colCap.setSortType(TableColumn.SortType.ASCENDING);
                colModel.setSortType(TableColumn.SortType.ASCENDING);
            }
            
            // 重新应用 5级排序
            table.getSortOrder().addAll(colStatus, colReg, colBrand, colCap, colModel);
            table.sort();
        });

        // 底部按钮栏
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAdd = new Button("Add New Aircraft");
        btnAdd.getStyleClass().addAll("btn", "btn-primary"); // 蓝色按钮
        Button btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().addAll("btn", "btn-danger"); // 红色按钮

        // 按钮事件
        btnAdd.setOnAction(e -> { showAddAircraftDialog(); showAircraftView(); });

        btnDelete.setOnAction(e -> {
            Aircraft selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Are you sure you want to delete " + selected.getRegistrationNumber() + "?", 
                    ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.YES) {
                        system.deleteAircraft(selected.getRegistrationNumber());
                        showAircraftView();
                    }
                });
            } else {
                showAlert("Warning", "Please select an aircraft to delete.");
            }
        });

        actions.getChildren().addAll(btnAdd, btnDelete);
        applyTableClip(table);
        centerContent.getChildren().addAll(header, toolBar, table, actions);
    }

    // 逻辑和飞机视图非常相似，只是列不一样
   // ================== 3. 航班管理视图 (Flight Management) ==================
    private void showFlightView() {
        centerContent.getChildren().clear();
        
        // 1. 页面大标题
        Label header = new Label("Flight Management");
        header.getStyleClass().add("content-header");

        // --- 工具栏区域 (左边搜索框，右边排序按钮) ---
        HBox toolBar = new HBox(10); // 元素间距10
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(0, 0, 10, 0)); // 底部留点空隙

        // 搜索框
        TextField searchField = new TextField();
        searchField.setPromptText("Search Flight No, Origin or Destination...");
        searchField.setPrefWidth(300); //稍微定宽一点

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS); // 占位符

        // 排序按钮
        Button btnSort = new Button("Sort: Ascending ⬆");
        btnSort.getStyleClass().addAll("btn", "btn-secondary");
        btnSort.setStyle("-fx-font-size: 12px; -fx-padding: 5 10;");

        toolBar.getChildren().addAll(searchField, spacer, btnSort);

        // --- 数据准备 ---
        ObservableList<Flight> masterData = FXCollections.observableArrayList(system.getAllFlights());
        FilteredList<Flight> filteredData = new FilteredList<>(masterData, p -> true);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(f -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lower = newVal.toLowerCase();
                return f.getFlightNumber().toLowerCase().contains(lower) 
                    || f.getOrigin().toLowerCase().contains(lower) 
                    || f.getDestination().toLowerCase().contains(lower);
            });
        });

        SortedList<Flight> sortedData = new SortedList<>(filteredData);
        TableView<Flight> table = new TableView<>();
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);
        
        table.getStyleClass().add("live-board");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        // 定义列
        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        
        TableColumn<Flight, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cell -> {
            boolean isCargo = cell.getValue() instanceof CargoFlight;
            return new SimpleStringProperty(isCargo ? "Cargo" : "Passenger");
        });

        TableColumn<Flight, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDepartureTime().toLocalDate().toString()));

        TableColumn<Flight, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));

        TableColumn<Flight, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));

        TableColumn<Flight, String> colDepTime = new TableColumn<>("Dep Time (UTC+8)");
        colDepTime.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getDepartureTime().toLocalTime().toString()));

        TableColumn<Flight, String> colArrTime = new TableColumn<>("Arr Time (UTC+8)");
        colArrTime.setCellValueFactory(cell -> {
            Flight f = cell.getValue();
            String status = f.getStatus();
            LocalDateTime dep = f.getDepartureTime();
            LocalDateTime realArrTime = f.getArrivalTime();
            String timeStr = realArrTime.toLocalTime().toString();
            if (realArrTime.toLocalDate().isAfter(dep.toLocalDate())) { timeStr += " (+1)"; }
            
            String displayTime = "";
            if ("Scheduled".equalsIgnoreCase(status) || "Boarding".equalsIgnoreCase(status)|| "Cancelled".equalsIgnoreCase(status)) { 
            	displayTime = "-"; 
            	} 
            else if ("Arrived".equalsIgnoreCase(status)) { 
            	displayTime = timeStr; 
            	} 
            else { displayTime = "Est: " + timeStr; }
            return new SimpleStringProperty(displayTime);
        });

        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(col -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    if (item.equalsIgnoreCase("Delayed") || item.equalsIgnoreCase("Cancelled")) 
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;"); 
                    else if (item.equalsIgnoreCase("Departed") || item.equalsIgnoreCase("Arrived") || item.equalsIgnoreCase("In Flight")) 
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;"); 
                    else setStyle("-fx-text-fill: #2c3e50;");
                } else { setText(null); setStyle(""); }
            }
        });

        table.getColumns().addAll(colNo, colType, colDate, colOrigin, colDest, colDepTime, colArrTime, colStatus);

        // 初始排序
        colDate.setSortType(TableColumn.SortType.ASCENDING);
        colDepTime.setSortType(TableColumn.SortType.ASCENDING);
        colDest.setSortType(TableColumn.SortType.ASCENDING);
        colNo.setSortType(TableColumn.SortType.ASCENDING);
        table.getSortOrder().addAll(colDate, colDepTime, colDest, colNo);

        // 按钮事件
        btnSort.setOnAction(e -> {
            table.getSortOrder().clear();
            if (btnSort.getText().contains("Ascending")) {
                btnSort.setText("Sort: Descending ⬇");
                colDate.setSortType(TableColumn.SortType.DESCENDING);
                colDepTime.setSortType(TableColumn.SortType.DESCENDING);
                colDest.setSortType(TableColumn.SortType.DESCENDING);
                colNo.setSortType(TableColumn.SortType.DESCENDING);
            } else {
                btnSort.setText("Sort: Ascending ⬆");
                colDate.setSortType(TableColumn.SortType.ASCENDING);
                colDepTime.setSortType(TableColumn.SortType.ASCENDING);
                colDest.setSortType(TableColumn.SortType.ASCENDING);
                colNo.setSortType(TableColumn.SortType.ASCENDING);
            }
            table.getSortOrder().addAll(colDate, colDepTime, colDest, colNo);
            table.sort();
        });

        // 按钮栏 (底部的增删改按钮)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAdd = new Button("Add Flight");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");
        Button btnStatus = new Button("Update Status");
        btnStatus.getStyleClass().addAll("btn", "btn-secondary");
        Button btnCancel = new Button("Delete Flight");
        btnCancel.getStyleClass().addAll("btn", "btn-danger");

        btnAdd.setOnAction(e -> { showAddFlightDialog(); showFlightView(); });
        
     // [核心修改]: 更新状态按钮的逻辑
        btnStatus.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // 1. 获取当前状态
                String currentStatus = selected.getStatus();
                
                // 2. 检查是否为终结状态 (Arrived 或 Cancelled)
                if ("Arrived".equalsIgnoreCase(currentStatus) || "Cancelled".equalsIgnoreCase(currentStatus)) {
                    // 如果是，弹窗警告并阻止操作
                    showAlert("Action Denied", 
                        "Flight " + selected.getFlightNumber() + " is already " + currentStatus + ".\n" +
                        "No further changes are allowed for completed or cancelled flights.");
                    return; // 直接结束，不弹出更新对话框
                }

                // 3. 如果不是终结状态，才允许更新
                showUpdateStatusDialog(selected); 
                table.refresh(); 
            }
            else {
                showAlert("Select Flight", "Please select a flight first.");
            }
        });

        btnCancel.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Are you sure you want to delete flight " + selected.getFlightNumber() + "?", 
                    ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        system.deleteFlight(selected.getFlightNumber());
                        showFlightView();
                    }
                });
            } else { showAlert("Warning", "Please select a flight to delete."); }
        });

        actions.getChildren().addAll(btnAdd, btnStatus, btnCancel);
        applyTableClip(table);
        
        // 核心变化：header -> toolBar(搜索+排序) -> table -> actions
        centerContent.getChildren().addAll(header, toolBar, table, actions);
    }

   // =================================================
    // 4. Reports & Logs View (🔥 优化：按钮高亮切换)
    // =================================================
    private void showReportsView() {
        centerContent.getChildren().clear();

        Label header = new Label("Reports & Analytics");
        header.getStyleClass().add("content-header");

        // 使用 StackPane 作为内容容器 (用于切换视图)
        StackPane contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // 默认显示运营统计
        contentArea.getChildren().add(createAnalysisTables());

        // 创建两个切换按钮
        Button btnAnalysis = new Button("Operational Statistics");
        Button btnHistory = new Button("Flight History");

        // --- [核心修改] 样式初始化 ---
        // 默认显示 Analysis，所以 Analysis 是蓝色 (Primary)，History 是灰色 (Secondary)
        btnAnalysis.getStyleClass().addAll("btn", "btn-primary");
        btnHistory.getStyleClass().addAll("btn", "btn-secondary");

        // --- 按钮点击事件：切换内容 + 切换样式 ---
        
        btnAnalysis.setOnAction(e -> {
            // 1. 切换内容
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createAnalysisTables());
            
            // 2. 切换按钮样式 (自己变蓝，对方变灰)
            updateReportButtons(btnAnalysis, btnHistory);
        });

        btnHistory.setOnAction(e -> {
            // 1. 切换内容
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createHistoryTable());
            
            // 2. 切换按钮样式 (自己变蓝，对方变灰)
            updateReportButtons(btnHistory, btnAnalysis);
        });

        HBox actions = new HBox(15, btnAnalysis, btnHistory);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        centerContent.getChildren().addAll(header, contentArea, actions);
    }

    // [新增辅助方法] 专门处理 Report 页面的按钮变色逻辑
    // activeBtn: 当前被点中的按钮 (要变蓝)
    // inactiveBtn: 另一个按钮 (要变灰)
    private void updateReportButtons(Button activeBtn, Button inactiveBtn) {
        // 让 activeBtn 变回 Primary (蓝)
        activeBtn.getStyleClass().remove("btn-secondary");
        if (!activeBtn.getStyleClass().contains("btn-primary")) {
            activeBtn.getStyleClass().add("btn-primary");
        }

        // 让 inactiveBtn 变成 Secondary (灰)
        inactiveBtn.getStyleClass().remove("btn-primary");
        if (!inactiveBtn.getStyleClass().contains("btn-secondary")) {
            inactiveBtn.getStyleClass().add("btn-secondary");
        }
    }

    // 创建统计表格 (左边统计状态数量，右边显示延误原因)
    // 创建统计表格 (左边统计状态数量，右边显示延误原因详情)
    private Node createAnalysisTables() {
        HBox container = new HBox(20);
        container.setPadding(new Insets(15));
        
        // --- 左侧: 状态统计表 (保持不变) ---
        TableView<StatRow> statsTable = new TableView<>();
        statsTable.getStyleClass().add("live-board");
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<StatRow, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        TableColumn<StatRow, Integer> colCount = new TableColumn<>("Count");
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        statsTable.getColumns().addAll(colCategory, colCount);
        
        int scheduled = 0, active = 0, completed = 0, delayed = 0, cancelled = 0;
        for (Flight f : system.getAllFlights()) {
            String s = f.getStatus();
            if ("Scheduled".equalsIgnoreCase(s) || "Boarding".equalsIgnoreCase(s)) scheduled++;
            else if ("Departed".equalsIgnoreCase(s) || "In Flight".equalsIgnoreCase(s)) active++;
            else if ("Arrived".equalsIgnoreCase(s)) completed++;
            else if ("Delayed".equalsIgnoreCase(s)) delayed++;
            else if ("Cancelled".equalsIgnoreCase(s)) cancelled++;
        }
        
        statsTable.setItems(FXCollections.observableArrayList(
            new StatRow("Scheduled / Boarding", scheduled),
            new StatRow("Departed", active),
            new StatRow("Completed (Arrived)", completed),
            new StatRow("Delayed", delayed),
            new StatRow("Cancelled", cancelled),
            new StatRow("TOTAL FLIGHTS", system.getAllFlights().size())
        ));
        applyTableClip(statsTable);

        // --- 右侧: 延误原因详情 (核心修改) ---
        TableView<DelayRow> delayTable = new TableView<>();
        delayTable.getStyleClass().add("live-board");
        // 注意: 这里不使用 CONSTRAINED_RESIZE_POLICY，因为列比较多，让它可以用滚动条或者按比例分配
        delayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // 1. 航班号
        TableColumn<DelayRow, String> colFlight = new TableColumn<>("Flight No");
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        
        // 2. [新增] 航班日期
        TableColumn<DelayRow, String> colDate = new TableColumn<>("Flight Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 3. [新增] 延误分类
        TableColumn<DelayRow, String> colDelayCat = new TableColumn<>("Category");
        colDelayCat.setCellValueFactory(new PropertyValueFactory<>("category"));

        // 4. [修改] 具体原因详情
        TableColumn<DelayRow, String> colReason = new TableColumn<>("Details");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        
        delayTable.getColumns().addAll(colFlight, colDate, colDelayCat, colReason);
        
        // --- 数据解析逻辑 ---
        ObservableList<DelayRow> delayData = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            // 遍历这个航班的所有延误原因
            for (String r : f.getDelayReasons()) {
                String dateStr = f.getDepartureTime().toLocalDate().toString();
                String category = "Uncategorized"; // 默认分类
                String detail = r;

                // 核心解析: 检查是否有冒号分隔 (例如 "Weather: Rain")
                if (r != null && r.contains(": ")) {
                    String[] parts = r.split(": ", 2); // 只分割第一个冒号，防止内容里也有冒号
                    if (parts.length == 2) {
                        category = parts[0]; // 前面是分类
                        detail = parts[1];   // 后面是具体原因
                    }
                }
                
                delayData.add(new DelayRow(f.getFlightNumber(), dateStr, category, detail));
            }
        }
        
        delayTable.setItems(delayData);
        delayTable.setPlaceholder(new Label("No delays reported. Operations are smooth!"));
        applyTableClip(delayTable);

        VBox leftBox = new VBox(6, new Label("Status Overview"), statsTable);
        VBox rightBox = new VBox(10, new Label("Delay Analytics Breakdown"), delayTable);
        
        // 布局调整: 让右边的表格宽一点，因为它有4列
        HBox.setHgrow(leftBox, Priority.ALWAYS); // 占 1 份宽
        HBox.setHgrow(rightBox, Priority.ALWAYS); // 占 1 份宽
        leftBox.setPrefWidth(300);
        rightBox.setPrefWidth(600); // 右边给多一点空间

        VBox.setVgrow(statsTable, Priority.ALWAYS);
        VBox.setVgrow(delayTable, Priority.ALWAYS);

        container.getChildren().addAll(leftBox, rightBox);
        return container;
    }

    // 创建历史记录表格 (紧凑版：横向排列，减少空白)
    private Node createHistoryTable() {
        TableView<Flight> historyTable = new TableView<>();
        historyTable.getStyleClass().add("live-board");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); 
        historyTable.setPrefHeight(500); 

        // 1. 筛选只显示已到达的航班
        ObservableList<Flight> arrivedFlights = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            if ("Arrived".equalsIgnoreCase(f.getStatus())) {
                arrivedFlights.add(f);
            }
        }
        historyTable.setItems(arrivedFlights);

        // --- 列定义 ---

        // 1. 航班号
        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colNo.setStyle("-fx-alignment: CENTER-LEFT;"); // 左对齐
        
        // 2. 航班日期
        TableColumn<Flight, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getArrivalTime().toLocalDate().toString()));
        colDate.setStyle("-fx-alignment: CENTER-LEFT;");

        // 3. 飞机注册号
        TableColumn<Flight, String> colPlane = new TableColumn<>("Aircraft");
        colPlane.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getAircraft().getRegistrationNumber()));
        colPlane.setStyle("-fx-alignment: CENTER-LEFT;");

        // 4. 最终抵达时间
        TableColumn<Flight, String> colArrTime = new TableColumn<>("Arr Time (UTC+8）");
        colArrTime.setCellValueFactory(cell -> {
            LocalDateTime dep = cell.getValue().getDepartureTime();
            LocalDateTime arr = cell.getValue().getArrivalTime();
            String time = arr.toLocalTime().toString();
            if (arr.toLocalDate().isAfter(dep.toLocalDate())) {
                time += " (+1)";
            }
            return new SimpleStringProperty(time);
        });
        colArrTime.setStyle("-fx-alignment: CENTER-LEFT;");

        // 5. 延误次数
        TableColumn<Flight, Integer> colDelayCount = new TableColumn<>("Delays");
        colDelayCount.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDelayReasons().size()));
        colDelayCount.setStyle("-fx-alignment: CENTER;");

        // 6. [核心修改] 延误详情 (紧凑的横向文本)
        TableColumn<Flight, Flight> colDetails = new TableColumn<>("Delay Remarks");
        colDetails.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        
        colDetails.setCellFactory(col -> new TableCell<Flight, Flight>() {
            @Override
            protected void updateItem(Flight flight, boolean empty) {
                super.updateItem(flight, empty);
                
                if (empty || flight == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    List<String> reasons = flight.getDelayReasons();
                    if (reasons.isEmpty()) {
                        setText("On Time");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER-LEFT;");
                    } else {
                        // --- 倒推时间并拼接字符串 ---
                        LocalDateTime finalTime = flight.getArrivalTime();
                        int totalDelays = reasons.size();
                        LocalDateTime baseTime = finalTime.minusHours(totalDelays);
                        
                        StringBuilder sb = new StringBuilder();

                        for (int i = 0; i < totalDelays; i++) {
                            LocalTime start = baseTime.plusHours(i).toLocalTime();
                            LocalTime end = baseTime.plusHours(i + 1).toLocalTime();
                            String reason = reasons.get(i);
                            
                            // 格式: [13:00-14:00] Weather: Rain
                            sb.append("[").append(start).append("-").append(end).append("] ").append(reason);
                            
                            // 如果不是最后一个，加上分隔符 " | "
                            if (i < totalDelays - 1) {
                                sb.append(" ->  "); 
                            }
                        }
                        
                        setText(sb.toString());
                        setWrapText(true); // 只有当文字真的太长超过屏幕时才换行
                        // 设置字体颜色深灰，左对齐
                        setStyle("-fx-text-fill: #555; -fx-alignment: CENTER-LEFT;"); 
                    }
                }
            }
        });

        // 调整列宽 (让详情列尽可能宽，填满空白)
        colNo.setMinWidth(80); colNo.setMaxWidth(100);
        colDate.setMinWidth(90); colDate.setMaxWidth(110);
        colPlane.setMinWidth(80); colPlane.setMaxWidth(100);
        colArrTime.setMinWidth(80); colArrTime.setMaxWidth(100);
        colDelayCount.setMinWidth(50); colDelayCount.setMaxWidth(60);
        
        // 不设置详情列的 MaxWidth，让它自动占满剩下的所有空间
        colDetails.setMinWidth(300); 

        historyTable.getColumns().addAll(colNo, colDate, colPlane, colArrTime, colDelayCount, colDetails);
        historyTable.setPlaceholder(new Label("No flight history available."));
        applyTableClip(historyTable);

        VBox box = new VBox(historyTable);
        box.setPadding(new Insets(15));
        return box;
    }

    // ================== 5. 弹窗 Dialogs (添加/修改/输入) ==================

    // 弹出添加飞机对话框
    private void showAddAircraftDialog() {
        Dialog<Aircraft> dialog = new Dialog<>();
        dialog.setTitle("Add New Aircraft");
        dialog.setHeaderText("Enter aircraft details");
        ButtonType addBtnType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField regField = new TextField(); regField.setPromptText("e.g. 9M-AB7");
        TextField brandField = new TextField();
        TextField modelField = new TextField();
        TextField capField = new TextField();

        grid.add(new Label("Reg No:"), 0, 0); grid.add(regField, 1, 0);
        grid.add(new Label("Brand:"), 0, 1); grid.add(brandField, 1, 1);
        grid.add(new Label("Model:"), 0, 2); grid.add(modelField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3); grid.add(capField, 1, 3);
        dialog.getDialogPane().setContent(grid);

        // 验证输入
     // 验证输入
        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addBtnType);
        addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                // 1. 验证注册号为空
                if (regField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Registration Number cannot be empty.");
                    event.consume(); return;
                }
                
                // 2. 验证注册号格式 (包含刚才修复的逻辑)
                else if (!regField.getText().trim().matches("^[A-Z0-9]{2,3}-[A-Z0-9]+$")) {
                    showAlert("Validation", "Invalid Format!\nExamples: 9M-ABC, N12345, 9V-SQA.\n(Must use Uppercase & Numbers, separated by '-')");
                    event.consume(); return;
                }

                // 3. [新增] 验证 Brand 不能为空
                else if (brandField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Brand cannot be empty.");
                    event.consume(); return;
                }

                // 4. [新增] 验证 Model 不能为空
                else if (modelField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Model cannot be empty.");
                    event.consume(); return;
                }

                // 5. 验证载客量 (必须是数字)
                Integer.parseInt(capField.getText()); 

            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Capacity must be a valid number.");
                event.consume(); 
            }
        });

        // 转换结果
        dialog.setResultConverter(btn -> {
            if (btn == addBtnType) {
                return new Aircraft(regField.getText(), brandField.getText(), modelField.getText(), 
                        Integer.parseInt(capField.getText()), "Available"); // 新飞机默认 Available
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> system.addAircraft(a)); // 如果点击了 Add，就添加到系统
    }

    // 弹出添加航班对话框
    private void showAddFlightDialog() {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Create schedule");
        ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField flightNoField = new TextField(); 
        flightNoField.setPromptText("e.g. MH-101");
        
        TextField destField = new TextField();
        destField.setPromptText("e.g. TOKYO"); // 提示用户输入大写

        ComboBox<String> aircraftBox = new ComboBox<>(); // 下拉菜单选择飞机
        
        DatePicker datePicker = new DatePicker(LocalDate.now()); // 日期选择器
        ComboBox<String> hourBox = new ComboBox<>(); // 小时选择器
        for(int i=0; i<24; i++) hourBox.getItems().add(String.format("%02d", i));
        hourBox.setValue("09");
        ComboBox<String> minBox = new ComboBox<>(); 
        minBox.getItems().addAll("00", "15", "30", "45");
        minBox.setValue("00");

        CheckBox cargoCheckbox = new CheckBox("Is Cargo Flight?"); // 勾选框：是否货机
        TextField cargoWeightField = new TextField();
        cargoWeightField.setPromptText("Weight (kg)");
        cargoWeightField.setDisable(true); // 默认禁用，勾选后启用
        cargoCheckbox.setOnAction(e -> cargoWeightField.setDisable(!cargoCheckbox.isSelected()));

        // 只显示可用或已排程的飞机
        system.getAllAircrafts().stream().filter(a -> "Available".equalsIgnoreCase(a.getStatus()) || "Scheduled".equalsIgnoreCase(a.getStatus()))
              .forEach(a -> aircraftBox.getItems().add(a.getRegistrationNumber()));

        grid.add(new Label("Flight No:"), 0, 0); grid.add(flightNoField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1); grid.add(destField, 1, 1);
        grid.add(new Label("Aircraft:"), 0, 2); grid.add(aircraftBox, 1, 2);
        grid.add(new Label("Date:"), 0, 3); grid.add(datePicker, 1, 3);
        grid.add(new Label("Time:"), 0, 4); 
        HBox timeBox = new HBox(5, hourBox, new Label(":"), minBox);
        grid.add(timeBox, 1, 4);
        grid.add(cargoCheckbox, 0, 5); grid.add(cargoWeightField, 1, 5);
        
        dialog.getDialogPane().setContent(grid);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createBtnType);
        createBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // --- 1. 获取输入内容 ---
            String fNumInput = flightNoField.getText().trim();
            String destInput = destField.getText().trim();

            // --- 2. [新功能] 检查航班号是否重复 ---
            // 如果 system.getFlight 返回的不是 null，说明已经有这个航班号了
            if (system.getFlight(fNumInput) != null) {
                showAlert("Duplicate Error", "Flight number " + fNumInput + " already exists!\nPlease use a unique number.");
                event.consume(); // 阻止窗口关闭
                return;
            }

            // --- 3. [新功能] 检查目的地格式 (只能大写字母) ---
            if (destInput.isEmpty()) {
                showAlert("Validation Error", "Destination cannot be empty.");
                event.consume(); return;
            }
            // 正则表达式解释: ^[A-Z ]+$ 
            // ^ 表示开始, [A-Z ] 表示只能包含大写字母和空格, + 表示至少一个字符, $ 表示结束
            if (!destInput.matches("^[A-Z ]+$")) {
                showAlert("Format Error", "Destination must be UPPERCASE letters only (e.g. LONDON).\nNo lowercase or numbers allowed.");
                event.consume(); return;
            }

            // --- 4. 原有的校验逻辑 ---
            if (aircraftBox.getValue() == null) {
                showAlert("Error", "Please select an aircraft.");
                event.consume(); return;
            }
            if (datePicker.getValue() == null) {
                showAlert("Error", "Please select a date.");
                event.consume(); return;
            }
            // 校验航班号格式 (比如 MH-101)
            if (!fNumInput.matches("^[A-Z]{2,3}-\\d{3,4}$")) { 
                showAlert("Validation", "Invalid Flight No. Format (e.g. MH-101).");
                event.consume(); return;
            }

            try {
                // 构造时间
                LocalDateTime dep = LocalDateTime.of(datePicker.getValue(), 
                        LocalTime.of(Integer.parseInt(hourBox.getValue()), Integer.parseInt(minBox.getValue())));
                LocalDateTime arr = dep.plusHours(2); 

                // 检查飞机时间冲突
                String selectedReg = aircraftBox.getValue();
                boolean available = system.isAircraftAvailable(selectedReg, dep, arr);
                
                if (!available) {
                    showAlert("Scheduling Conflict", 
                        "Aircraft " + selectedReg + " is already booked for this time slot!\n" +
                        "Please choose a different time or aircraft.");
                    event.consume(); return;
                }

                if (cargoCheckbox.isSelected()) {
                    Double.parseDouble(cargoWeightField.getText());
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Cargo weight must be a valid number.");
                event.consume();
            }
        });

        // 结果转换
        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                String reg = aircraftBox.getValue();
                Aircraft plane = system.getAircraft(reg);
                String fNum = flightNoField.getText().trim();
                String dest = destField.getText().trim();
                LocalDateTime dep = LocalDateTime.of(datePicker.getValue(), 
                        LocalTime.of(Integer.parseInt(hourBox.getValue()), Integer.parseInt(minBox.getValue())));
                LocalDateTime arr = dep.plusHours(2);

                if (cargoCheckbox.isSelected()) {
                    double w = Double.parseDouble(cargoWeightField.getText());
                    return new CargoFlight(fNum, "BATU PAHAT", dest, dep, arr, plane, w);
                } else {
                    return new Flight(fNum, "BATU PAHAT", dest, dep, arr, plane);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            system.addFlight(f);
            f.getAircraft().setStatus("Scheduled"); 
        });
    }
    // 弹出状态更新对话框 (包含高级分类 Delay 原因选择)
    private void showUpdateStatusDialog(Flight flight) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(flight.getStatus(), 
            "Boarding", "Departed", "Arrived", "Delayed", "Cancelled");
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update status for " + flight.getFlightNumber());
        dialog.setContentText("New Status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            // 如果选的是 "Delayed"，弹出高级原因选择框
            if ("Delayed".equals(newStatus)) {
                
                // --- 1. 创建自定义 Dialog ---
                Dialog<String> delayDialog = new Dialog<>();
                delayDialog.setTitle("Delay Details");
                delayDialog.setHeaderText("Select Delay Category & Reason");
                ButtonType okBtn = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
                delayDialog.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

                // --- 2. 定义分类数据 (Map结构: 分类 -> 原因列表) ---
                Map<String, List<String>> delayMap = new LinkedHashMap<>(); // LinkedHashMap 保持插入顺序
                
                // 天气类
                delayMap.put("Weather Conditions", Arrays.asList(
                    "Heavy Rain / Thunderstorm", "Strong Crosswinds", "Low Visibility / Fog", "Snow / Ice", "Typhoon Warning"));
                // 技术类
                delayMap.put("Technical / Aircraft", Arrays.asList(
                    "Engine Inspection", "Hydraulic System Issue", "Navigational System Error", "Door Seal Issue", "Landing Gear Check"));
                // 运营类
                delayMap.put("Operational", Arrays.asList(
                    "Late Arrival of Incoming Aircraft", "Crew Rotation / Rest", "Catering Loading", "Baggage Handling", "Refueling Delays"));
                // 空管类
                delayMap.put("ATC / Airport", Arrays.asList(
                    "Air Traffic Control Restriction", "Runway Maintenance", "Gate Availability", "Security Clearance"));
                // 其他 (手动输入)
                delayMap.put("Others", new ArrayList<>()); 

                // --- 3. 创建界面控件 ---
                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                ComboBox<String> categoryBox = new ComboBox<>();
                categoryBox.setPromptText("Select Category...");
                categoryBox.getItems().addAll(delayMap.keySet()); // 填入大分类

                // 子原因下拉框 (默认隐藏)
                ComboBox<String> reasonBox = new ComboBox<>();
                reasonBox.setPromptText("Select Specific Reason...");
                reasonBox.setVisible(false);
                reasonBox.setManaged(false); // 隐藏时不占位

                // 手动输入框 (默认隐藏)
                TextArea otherField = new TextArea();
                otherField.setPromptText("Please type the specific reason here...");
                otherField.setPrefHeight(60);
                otherField.setPrefWidth(250);
                otherField.setWrapText(true);
                otherField.setVisible(false);
                otherField.setManaged(false);

                grid.add(new Label("Category:"), 0, 0);
                grid.add(categoryBox, 1, 0);
                grid.add(new Label("Reason:"), 0, 1);
                
                // 使用 StackPane 让下拉框和输入框重叠在同一个位置，根据逻辑切换显示
                StackPane reasonContainer = new StackPane(reasonBox, otherField);
                reasonContainer.setAlignment(Pos.CENTER_LEFT);
                grid.add(reasonContainer, 1, 1);

                delayDialog.getDialogPane().setContent(grid);

                // --- 4. 核心交互逻辑 ---
                categoryBox.setOnAction(e -> {
                    String selectedCat = categoryBox.getValue();
                    if (selectedCat == null) return;

                    if ("Others".equals(selectedCat)) {
                        // 如果选了 Others: 显示输入框，隐藏下拉框
                        reasonBox.setVisible(false); reasonBox.setManaged(false);
                        otherField.setVisible(true); otherField.setManaged(true);
                        otherField.clear();
                    } else {
                        // 如果选了普通分类: 显示下拉框，隐藏输入框
                        otherField.setVisible(false); otherField.setManaged(false);
                        reasonBox.setVisible(true); reasonBox.setManaged(true);
                        // 更新下拉框的内容
                        reasonBox.getItems().setAll(delayMap.get(selectedCat));
                        reasonBox.getSelectionModel().clearSelection();
                    }
                    // 触发布局更新 (防止界面错位)
                    delayDialog.getDialogPane().getScene().getWindow().sizeToScene();
                });

                // --- 5. 结果处理与校验 ---
                Button confirmButton = (Button) delayDialog.getDialogPane().lookupButton(okBtn);
                confirmButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    String cat = categoryBox.getValue();
                    
                    // 校验1: 必须选大分类
                    if (cat == null) {
                        showAlert("Validation", "Please select a delay category.");
                        event.consume(); return;
                    }

                    // 校验2: 根据分类校验具体原因
                    if ("Others".equals(cat)) {
                        if (otherField.getText().trim().isEmpty()) {
                            showAlert("Validation", "Please type the reason for 'Others'.");
                            event.consume();
                        }
                    } else {
                        if (reasonBox.getValue() == null) {
                            showAlert("Validation", "Please select a specific reason.");
                            event.consume();
                        }
                    }
                });

                // 转换结果
                delayDialog.setResultConverter(btn -> {
                    if (btn == okBtn) {
                        String cat = categoryBox.getValue();
                        String detail;
                        if ("Others".equals(cat)) {
                            //如果是手动输入，格式: "Others: 原因"
                            detail = "Others: " + otherField.getText().trim();
                        } else {
                            //如果是选择，格式: "Category: 具体原因" (这样在报表里看很清晰)
                            detail = cat + ": " + reasonBox.getValue();
                        }
                        return detail;
                    }
                    return null;
                });

                // --- 6. 获取结果并更新系统 ---
                delayDialog.showAndWait().ifPresent(finalReason -> {
                    system.updateFlightStatus(flight.getFlightNumber(), newStatus); // 更新时间
                    flight.addDelayReason(finalReason); // 存入组合好的原因字符串
                });

            } else {
                // 如果不是 Delayed，直接更新
                system.updateFlightStatus(flight.getFlightNumber(), newStatus);
            }
        });
    }

    // 辅助方法：显示简单弹窗
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(content);
        alert.showAndWait();
    }
    
    // 辅助方法：创建卡片 UI
    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        card.setPrefWidth(220);
        card.setAlignment(Pos.CENTER);
        Label lblVal = new Label(value); lblVal.getStyleClass().add("card-value");
        Label lblTitle = new Label(title); lblTitle.getStyleClass().add("card-title");
        card.getChildren().addAll(lblVal, lblTitle);
        return card;
    }
    
    // 辅助方法：给表格加圆角剪裁 (不加的话圆角CSS可能会有白边)
    private void applyTableClip(Region region) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    // 如果 IDE 无法直接运行 Launcher，也可以运行这个 main
    public static void main(String[] args) { launch(); }

    // --- 内部辅助类 (用于表格显示数据) ---
    // --- 内部辅助类 (用于表格显示数据) ---
    public static class StatRow {
        private final String category;
        private final int count;
        public StatRow(String category, int count) { this.category = category; this.count = count; }
        public String getCategory() { return category; }
        public int getCount() { return count; }
    }

    // [核心修改]: 增加了日期 (date) 和 分类 (category) 字段
    public static class DelayRow {
        private final String flightNo;
        private final String date;      // 新增: 航班日期
        private final String category;  // 新增: 延误大类
        private final String reason;    // 具体原因

        public DelayRow(String flightNo, String date, String category, String reason) { 
            this.flightNo = flightNo; 
            this.date = date;
            this.category = category;
            this.reason = reason; 
        }
        
        public String getFlightNo() { return flightNo; }
        public String getDate() { return date; }
        public String getCategory() { return category; }
        public String getReason() { return reason; }
    }
}