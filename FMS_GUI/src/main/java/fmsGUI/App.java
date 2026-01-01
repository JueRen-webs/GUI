package fmsGUI;

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
import javafx.beans.binding.Bindings;
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

        // [新增] 负载/乘客 列
        TableColumn<Flight, String> colLoad = new TableColumn<>("Load / Pax");
        colLoad.setCellValueFactory(cell -> {
            Flight f = cell.getValue();
            if (f instanceof CargoFlight) {
                // 如果是货机，显示重量
                double cap = ((CargoFlight) f).getCargoCapacity();
                return new SimpleStringProperty(String.format("%.0f kg", cap));
            } else {
                // 如果是客机，显示 乘客数 / 最大容量
                int current = f.getBookedPassengers();
                int max = f.getAircraft().getCapacity();
                return new SimpleStringProperty(current + " / " + max);
            }
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

        // [修改] 把 colLoad 加入列列表
        table.getColumns().addAll(colNo, colType, colLoad, colDate, colOrigin, colDest, colDepTime, colArrTime, colStatus);

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
        
        btnStatus.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                // 1. 获取当前状态
                String currentStatus = selected.getStatus();
                
                // 2. 检查是否为终结状态
                if ("Arrived".equalsIgnoreCase(currentStatus) || "Cancelled".equalsIgnoreCase(currentStatus)) {
                    showAlert("Action Denied", 
                        "Flight " + selected.getFlightNumber() + " is already " + currentStatus + ".\n" +
                        "No further changes are allowed for completed or cancelled flights.");
                    return; 
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
        
        centerContent.getChildren().addAll(header, toolBar, table, actions);
    }

   // =================================================
    // 4. Reports & Logs View (Compact Layout)
    // =================================================
    private void showReportsView() {
        centerContent.getChildren().clear();

        // [核心优化] 创建一个独立的紧凑容器，间距设为 5 (原本是 20，太宽了)
        VBox reportContainer = new VBox(5);
        
        // 1. 页面大标题
        Label header = new Label("Reports & Analytics");
        header.getStyleClass().add("content-header");
        // 这里的下边距设为 0，让下面的内容紧贴标题
        header.setPadding(new Insets(0, 0, 0, 0)); 

        // 2. 内容区域
        StackPane contentArea = new StackPane();
        VBox.setVgrow(contentArea, Priority.ALWAYS);
        
        // 默认显示运营统计
        contentArea.getChildren().add(createAnalysisTables(contentArea));
        
        // 将标题和内容放入紧凑容器
        reportContainer.getChildren().addAll(header, contentArea);

        // 最后把这个紧凑容器放入主区域
        centerContent.getChildren().add(reportContainer);
    }

    // 创建统计表格 (修复版：内容居中 + Total行样式)
    private Node createAnalysisTables(StackPane parentContainer) {
        HBox container = new HBox(15);
        container.setPadding(new Insets(0)); 
        
        // --- 1. 准备右侧的导航按钮 ---
        Button btnToHistory = new Button("Flight History");
        btnToHistory.getStyleClass().addAll("btn", "btn-primary");
        btnToHistory.setOnAction(e -> {
            parentContainer.getChildren().clear();
            parentContainer.getChildren().add(createHistoryTable(parentContainer));
        });

        HBox rightNavRow = new HBox(btnToHistory);
        rightNavRow.setAlignment(Pos.CENTER_RIGHT); 
        rightNavRow.setPadding(new Insets(0, 0, 2, 0)); 

        // --- 2. 准备左侧的占位符 ---
        Region leftSpacer = new Region();
        leftSpacer.prefHeightProperty().bind(rightNavRow.heightProperty());
        leftSpacer.minHeightProperty().bind(rightNavRow.heightProperty());

        // --- 3. 左侧表格 (Status) ---
        TableView<StatRow> statsTable = new TableView<>();
        statsTable.getStyleClass().add("live-board");
        statsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // [修改] 列定义 + 设置居中
        TableColumn<StatRow, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setStyle("-fx-alignment: CENTER;"); // <--- 居中
        
        TableColumn<StatRow, Integer> colCount = new TableColumn<>("Count");
        colCount.setCellValueFactory(new PropertyValueFactory<>("count"));
        colCount.setStyle("-fx-alignment: CENTER;"); // <--- 居中
        
        statsTable.getColumns().addAll(colCategory, colCount);
        
        // 填充数据
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

        // 绑定高度 (去除空行)
        statsTable.setFixedCellSize(30); 
        statsTable.prefHeightProperty().bind(
            Bindings.size(statsTable.getItems())
            .multiply(statsTable.getFixedCellSize())
            .add(35) 
        );
        statsTable.minHeightProperty().bind(statsTable.prefHeightProperty());
        statsTable.maxHeightProperty().bind(statsTable.prefHeightProperty());

        // Total 行特殊样式
        statsTable.setRowFactory(tv -> new TableRow<StatRow>() {
            @Override
            protected void updateItem(StatRow item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().remove("total-row"); 
                if (item == null || empty) return;
                if ("TOTAL FLIGHTS".equals(item.getCategory())) {
                    getStyleClass().add("total-row");
                }
            }
        });
        
        applyTableClip(statsTable);

        // --- 4. 右侧表格 (Delay) ---
        TableView<DelayRow> delayTable = new TableView<>();
        delayTable.getStyleClass().add("live-board");
        delayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // [修改] 列定义 + 设置居中
        TableColumn<DelayRow, String> colFlight = new TableColumn<>("Flight No");
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        colFlight.setStyle("-fx-alignment: CENTER;"); // <--- 居中

        TableColumn<DelayRow, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colDate.setStyle("-fx-alignment: CENTER;"); // <--- 居中

        TableColumn<DelayRow, String> colDelayCat = new TableColumn<>("Category");
        colDelayCat.setCellValueFactory(new PropertyValueFactory<>("category"));
        colDelayCat.setStyle("-fx-alignment: CENTER;"); // <--- 居中

        TableColumn<DelayRow, String> colReason = new TableColumn<>("Details");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colReason.setStyle("-fx-alignment: CENTER-LEFT;"); // <--- 长文本保持靠左更好读

        delayTable.getColumns().addAll(colFlight, colDate, colDelayCat, colReason);
        
        ObservableList<DelayRow> delayData = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            for (String r : f.getDelayReasons()) {
                String dateStr = f.getDepartureTime().toLocalDate().toString();
                String category = "Uncategorized"; String detail = r;
                if (r != null && r.contains(": ")) {
                    String[] parts = r.split(": ", 2); 
                    if (parts.length == 2) { category = parts[0]; detail = parts[1]; }
                }
                delayData.add(new DelayRow(f.getFlightNumber(), dateStr, category, detail));
            }
        }
        delayTable.setItems(delayData);
        delayTable.setPlaceholder(new Label("No delays reported."));
        applyTableClip(delayTable);

        // --- 5. 组装布局 ---
        Label lblStatus = new Label("Status Overview");
        lblStatus.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label lblDelay = new Label("Delay Analytics Breakdown");
        lblDelay.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        VBox leftBox = new VBox(2, leftSpacer, lblStatus, statsTable);
        VBox.setVgrow(statsTable, Priority.NEVER); 
        
        VBox rightBox = new VBox(2, rightNavRow, lblDelay, delayTable);
        VBox.setVgrow(delayTable, Priority.ALWAYS); 
        
        HBox.setHgrow(leftBox, Priority.ALWAYS); 
        HBox.setHgrow(rightBox, Priority.ALWAYS); 
        leftBox.setPrefWidth(300);
        rightBox.setPrefWidth(600); 

        container.getChildren().addAll(leftBox, rightBox);
        return container;
    }

    // 创建历史记录表格 (优化版：按钮列紧凑，其他列自动填充)
    private Node createHistoryTable(StackPane parentContainer) {
        VBox mainBox = new VBox(2);
        mainBox.setPadding(new Insets(0)); 

        // 1. 顶部导航行
        HBox navRow = new HBox();
        navRow.setAlignment(Pos.CENTER_RIGHT);
        navRow.setPadding(new Insets(0, 0, 2, 0)); 
        
        Button btnBack = new Button("Operational Statistics");
        btnBack.getStyleClass().addAll("btn", "btn-primary");
        btnBack.setOnAction(e -> {
            parentContainer.getChildren().clear();
            parentContainer.getChildren().add(createAnalysisTables(parentContainer));
        });
        navRow.getChildren().add(btnBack);

        // 2. 标题
        Label lblTitle = new Label("Flight History (Arrived)");
        lblTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        lblTitle.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));

        // 3. 表格
        TableView<Flight> historyTable = new TableView<>();
        historyTable.getStyleClass().add("live-board");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); 
        
        ObservableList<Flight> arrivedFlights = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            if ("Arrived".equalsIgnoreCase(f.getStatus())) arrivedFlights.add(f);
        }
        historyTable.setItems(arrivedFlights);

        // --- 列定义 ---
        
        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colNo.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<Flight, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getArrivalTime().toLocalDate().toString()));
        colDate.setStyle("-fx-alignment: CENTER;");

        TableColumn<Flight, String> colPlane = new TableColumn<>("Aircraft");
        colPlane.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getAircraft().getRegistrationNumber()));
        colPlane.setStyle("-fx-alignment: CENTER;");

        TableColumn<Flight, String> colArrTime = new TableColumn<>("Arr Time (UTC+8）");
        colArrTime.setCellValueFactory(cell -> {
            LocalDateTime dep = cell.getValue().getDepartureTime();
            LocalDateTime arr = cell.getValue().getArrivalTime();
            String time = arr.toLocalTime().toString();
            if (arr.toLocalDate().isAfter(dep.toLocalDate())) time += " (+1)";
            return new SimpleStringProperty(time);
        });
        colArrTime.setStyle("-fx-alignment: CENTER;");

        TableColumn<Flight, Integer> colDelayCount = new TableColumn<>("Delays");
        colDelayCount.setCellValueFactory(cell -> 
            new javafx.beans.property.SimpleObjectProperty<>(cell.getValue().getDelayReasons().size()));
        colDelayCount.setStyle("-fx-alignment: CENTER;");

        // --- Delay Remarks (按钮列) ---
        TableColumn<Flight, Flight> colDetails = new TableColumn<>("Remarks"); // 标题改短一点
        colDetails.setCellValueFactory(cell -> new javafx.beans.property.SimpleObjectProperty<>(cell.getValue()));
        
        colDetails.setCellFactory(col -> new TableCell<Flight, Flight>() {
            private final Button btn = new Button("View"); // 按钮文字改短一点

            {
                btn.getStyleClass().add("btn"); 
                // 按钮做得更小巧精致
                btn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 2 15; -fx-background-radius: 8;");
                
                btn.setOnAction(e -> {
                    Flight f = getItem();
                    if (f != null) {
                        List<String> reasons = f.getDelayReasons();
                        LocalDateTime finalTime = f.getArrivalTime();
                        int totalDelays = reasons.size();
                        LocalDateTime baseTime = finalTime.minusHours(totalDelays);
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < totalDelays; i++) {
                            LocalTime start = baseTime.plusHours(i).toLocalTime();
                            LocalTime end = baseTime.plusHours(i + 1).toLocalTime();
                            sb.append("[").append(start).append("-").append(end).append("] ").append(reasons.get(i));
                            if (i < totalDelays - 1) sb.append("\n⬇\n");
                        }
                        showAlert("Delay Details: " + f.getFlightNumber(), 
                                  "Flight: " + f.getFlightNumber() + " (" + f.getOrigin() + " -> " + f.getDestination() + ")\n" +
                                  "Date: " + f.getArrivalTime().toLocalDate() + "\n\n" +
                                  "--- Timeline ---\n" + sb.toString());
                    }
                });
            }

            @Override
            protected void updateItem(Flight flight, boolean empty) {
                super.updateItem(flight, empty);
                if (empty || flight == null) {
                    setText(null); setGraphic(null);
                } else {
                    if (flight.getDelayReasons().isEmpty()) {
                        setText("On Time");
                        setGraphic(null);
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setText(null);
                        setGraphic(btn);
                        setStyle("-fx-alignment: CENTER;");
                    }
                }
            }
        });

        // [核心修改]: 调整列宽策略
        // 1. 给数据列设置最小宽度，但不设置最大宽度 (setMaxWidth)，让它们可以自动拉伸填满屏幕
        colNo.setMinWidth(80); 
        colDate.setMinWidth(100); 
        colPlane.setMinWidth(100); 
        colArrTime.setMinWidth(100); 
        colDelayCount.setMinWidth(60); 

        // 2. 给按钮列设置固定范围，防止它占满剩余空间
        colDetails.setMinWidth(80); 
        colDetails.setMaxWidth(150); // 锁死宽度，只够放一个小按钮即可

        historyTable.getColumns().addAll(colNo, colDate, colPlane, colArrTime, colDelayCount, colDetails);
        historyTable.setPlaceholder(new Label("No flight history available."));
        applyTableClip(historyTable);
        VBox.setVgrow(historyTable, Priority.ALWAYS); 

        mainBox.getChildren().addAll(navRow, lblTitle, historyTable);
        return mainBox;
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
        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addBtnType);
        addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (regField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Registration Number cannot be empty.");
                    event.consume(); return;
                }
                else if (!regField.getText().trim().matches("^[A-Z0-9]{2,3}-[A-Z0-9]+$")) {
                    showAlert("Validation", "Invalid Format!\nExamples: 9M-ABC, 9M-N12345, 9V-SQA.\n(Must use Uppercase & Numbers, separated by '-')");
                    event.consume(); return;
                }
                else if (brandField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Brand cannot be empty.");
                    event.consume(); return;
                }
                else if (modelField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Model cannot be empty.");
                    event.consume(); return;
                }
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

        dialog.showAndWait().ifPresent(a -> system.addAircraft(a)); 
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
        destField.setPromptText("e.g. TOKYO"); 

        ComboBox<String> aircraftBox = new ComboBox<>(); 
        
        // [新增] 乘客数量输入框
        TextField paxField = new TextField();
        paxField.setPromptText("Passengers");
        
        DatePicker datePicker = new DatePicker(LocalDate.now()); 
        ComboBox<String> hourBox = new ComboBox<>(); 
        for(int i=0; i<24; i++) hourBox.getItems().add(String.format("%02d", i));
        hourBox.setValue("09");
        ComboBox<String> minBox = new ComboBox<>(); 
        minBox.getItems().addAll("00", "15", "30", "45");
        minBox.setValue("00");

        CheckBox cargoCheckbox = new CheckBox("Is Cargo Flight?"); 
        TextField cargoWeightField = new TextField();
        cargoWeightField.setPromptText("Weight (kg)");
        cargoWeightField.setDisable(true); 
        
        // [逻辑] 互斥开关：Cargo 选了就禁 Pax，反之亦然
        cargoCheckbox.setOnAction(e -> {
            boolean isCargo = cargoCheckbox.isSelected();
            cargoWeightField.setDisable(!isCargo); 
            paxField.setDisable(isCargo);          
            if (isCargo) paxField.clear();         
            else cargoWeightField.clear();         
        });

        system.getAllAircrafts().stream().filter(a -> "Available".equalsIgnoreCase(a.getStatus()) || "Scheduled".equalsIgnoreCase(a.getStatus()))
              .forEach(a -> aircraftBox.getItems().add(a.getRegistrationNumber()));

        grid.add(new Label("Flight No:"), 0, 0); grid.add(flightNoField, 1, 0);
        grid.add(new Label("Destination:"), 0, 1); grid.add(destField, 1, 1);
        grid.add(new Label("Aircraft:"), 0, 2); grid.add(aircraftBox, 1, 2);
        
        // [新增] 乘客输入框放在这里
        grid.add(new Label("Passengers:"), 0, 3); grid.add(paxField, 1, 3);
        
        grid.add(new Label("Date:"), 0, 4); grid.add(datePicker, 1, 4);
        grid.add(new Label("Time:"), 0, 5); 
        HBox timeBox = new HBox(5, hourBox, new Label(":"), minBox);
        grid.add(timeBox, 1, 5);
        grid.add(cargoCheckbox, 0, 6); grid.add(cargoWeightField, 1, 6);
        
        dialog.getDialogPane().setContent(grid);

        Button createBtn = (Button) dialog.getDialogPane().lookupButton(createBtnType);
        createBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // --- 1. 获取输入内容 ---
            String fNumInput = flightNoField.getText().trim();
            String destInput = destField.getText().trim();

            if (system.getFlight(fNumInput) != null) {
                showAlert("Duplicate Error", "Flight number " + fNumInput + " already exists!\nPlease use a unique number.");
                event.consume(); 
                return;
            }

            if (destInput.isEmpty()) {
                showAlert("Validation Error", "Destination cannot be empty.");
                event.consume(); return;
            }
            if (!destInput.matches("^[A-Z ]+$")) {
                showAlert("Format Error", "Destination must be UPPERCASE letters only (e.g. LONDON).\nNo lowercase or numbers allowed.");
                event.consume(); return;
            }

            if (aircraftBox.getValue() == null) {
                showAlert("Error", "Please select an aircraft.");
                event.consume(); return;
            }
            if (datePicker.getValue() == null) {
                showAlert("Error", "Please select a date.");
                event.consume(); return;
            }
            if (!fNumInput.matches("^[A-Z]{2,3}-\\d{3,4}$")) { 
                showAlert("Validation", "Invalid Flight No. Format (e.g. MH-101).");
                event.consume(); return;
            }

            // [核心修改] 验证乘客数量或货运重量
            try {
                if (cargoCheckbox.isSelected()) {
                    Double.parseDouble(cargoWeightField.getText());
                } else {
                    String paxText = paxField.getText().trim();
                    if (paxText.isEmpty()) {
                        showAlert("Validation", "Please enter passenger count.");
                        event.consume(); return;
                    }
                    int pax = Integer.parseInt(paxText);
                    
                    // 检查容量
                    String reg = aircraftBox.getValue();
                    if (reg != null) {
                        Aircraft plane = system.getAircraft(reg);
                        if (pax > plane.getCapacity()) {
                            showAlert("Over Capacity", 
                                "Passenger count (" + pax + ") exceeds aircraft capacity (" + plane.getCapacity() + ")!");
                            event.consume(); return;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Passengers or Weight must be valid numbers.");
                event.consume();
                return;
            }

            // 时间冲突检查
            try {
                LocalDateTime dep = LocalDateTime.of(datePicker.getValue(), 
                        LocalTime.of(Integer.parseInt(hourBox.getValue()), Integer.parseInt(minBox.getValue())));
                LocalDateTime arr = dep.plusHours(2); 

                String selectedReg = aircraftBox.getValue();
                boolean available = system.isAircraftAvailable(selectedReg, dep, arr);
                
                if (!available) {
                    showAlert("Scheduling Conflict", 
                        "Aircraft " + selectedReg + " is already booked for this time slot!\n" +
                        "Please choose a different time or aircraft.");
                    event.consume(); return;
                }
            } catch (Exception e) {
                // Ignore
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
                    // CargoFlight 自动传 0 乘客
                    return new CargoFlight(fNum, "BATU PAHAT", dest, dep, arr, plane, w);
                } else {
                    int pax = Integer.parseInt(paxField.getText());
                    // 普通 Flight 传入 pax
                    return new Flight(fNum, "BATU PAHAT", dest, dep, arr, plane, pax);
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

                // --- 2. 定义分类数据 ---
                Map<String, List<String>> delayMap = new LinkedHashMap<>(); 
                
                delayMap.put("Weather Conditions", Arrays.asList(
                    "Heavy Rain / Thunderstorm", "Strong Crosswinds", "Low Visibility / Fog", "Snow / Ice", "Typhoon Warning"));
                delayMap.put("Technical / Aircraft", Arrays.asList(
                    "Engine Inspection", "Hydraulic System Issue", "Navigational System Error", "Door Seal Issue", "Landing Gear Check"));
                delayMap.put("Operational", Arrays.asList(
                    "Late Arrival of Incoming Aircraft", "Crew Rotation / Rest", "Catering Loading", "Baggage Handling", "Refueling Delays"));
                delayMap.put("ATC / Airport", Arrays.asList(
                    "Air Traffic Control Restriction", "Runway Maintenance", "Gate Availability", "Security Clearance"));
                delayMap.put("Others", new ArrayList<>()); 

                // --- 3. 创建界面控件 ---
                GridPane grid = new GridPane();
                grid.setHgap(10); grid.setVgap(10);
                grid.setPadding(new Insets(20, 150, 10, 10));

                ComboBox<String> categoryBox = new ComboBox<>();
                categoryBox.setPromptText("Select Category...");
                categoryBox.getItems().addAll(delayMap.keySet()); 

                ComboBox<String> reasonBox = new ComboBox<>();
                reasonBox.setPromptText("Select Specific Reason...");
                reasonBox.setVisible(false);
                reasonBox.setManaged(false); 

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
                
                StackPane reasonContainer = new StackPane(reasonBox, otherField);
                reasonContainer.setAlignment(Pos.CENTER_LEFT);
                grid.add(reasonContainer, 1, 1);

                delayDialog.getDialogPane().setContent(grid);

                categoryBox.setOnAction(e -> {
                    String selectedCat = categoryBox.getValue();
                    if (selectedCat == null) return;

                    if ("Others".equals(selectedCat)) {
                        reasonBox.setVisible(false); reasonBox.setManaged(false);
                        otherField.setVisible(true); otherField.setManaged(true);
                        otherField.clear();
                    } else {
                        otherField.setVisible(false); otherField.setManaged(false);
                        reasonBox.setVisible(true); reasonBox.setManaged(true);
                        reasonBox.getItems().setAll(delayMap.get(selectedCat));
                        reasonBox.getSelectionModel().clearSelection();
                    }
                    delayDialog.getDialogPane().getScene().getWindow().sizeToScene();
                });

                Button confirmButton = (Button) delayDialog.getDialogPane().lookupButton(okBtn);
                confirmButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    String cat = categoryBox.getValue();
                    
                    if (cat == null) {
                        showAlert("Validation", "Please select a delay category.");
                        event.consume(); return;
                    }

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

                delayDialog.setResultConverter(btn -> {
                    if (btn == okBtn) {
                        String cat = categoryBox.getValue();
                        String detail;
                        if ("Others".equals(cat)) {
                            detail = "Others: " + otherField.getText().trim();
                        } else {
                            detail = cat + ": " + reasonBox.getValue();
                        }
                        return detail;
                    }
                    return null;
                });

                delayDialog.showAndWait().ifPresent(finalReason -> {
                    system.updateFlightStatus(flight.getFlightNumber(), newStatus); 
                    flight.addDelayReason(finalReason); 
                });

            } else {
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
    
    // 辅助方法：给表格加圆角剪裁
    private void applyTableClip(Region region) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    public static void main(String[] args) { launch(); }

    // --- 内部辅助类 ---
    public static class StatRow {
        private final String category;
        private final int count;
        public StatRow(String category, int count) { this.category = category; this.count = count; }
        public String getCategory() { return category; }
        public int getCount() { return count; }
    }

    public static class DelayRow {
        private final String flightNo;
        private final String date;      
        private final String category;  
        private final String reason;    

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