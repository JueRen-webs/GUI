package com.example;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

public class App extends Application {

    private FlightManagementSystem system = new FlightManagementSystem();
    private BorderPane rootLayout;
    private VBox centerContent;

    @Override
    public void start(Stage stage) {
        // 🔥 1. 启动时从文件加载数据
        system.loadData();

        rootLayout = new BorderPane();
        VBox sideMenu = createSideMenu();
        rootLayout.setLeft(sideMenu);

        centerContent = new VBox(20);
        centerContent.setPadding(new Insets(20));
        rootLayout.setCenter(centerContent);

        showDashboard();

        Scene scene = new Scene(rootLayout, 1000, 600);
        // 加载 CSS (防止报错，加了判空)
        if (getClass().getResource("/style.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        }
        stage.setTitle("Flight Management System (Pro Version)");
        stage.setScene(scene);
        stage.show();
    }

    // 🔥 2. 关闭程序前自动保存
    @Override
    public void stop() throws Exception {
        system.saveData();
        super.stop();
    }

    private VBox createSideMenu() {
        VBox menu = new VBox(0);
        menu.setPadding(new Insets(20));
        menu.getStyleClass().add("sidebar");
        menu.setPrefWidth(220);

        Label title = new Label("FMS System");
        title.getStyleClass().add("sidebar-title");

        Button btnDashboard = createMenuButton("Dashboard");
        Button btnAircraft = createMenuButton("Aircraft Management");
        Button btnFlight = createMenuButton("Flight Management");
        Button btnReports = createMenuButton("Reports & Logs");
        Button btnExit = createMenuButton("Exit");

        btnDashboard.setOnAction(e -> showDashboard());
        btnAircraft.setOnAction(e -> showAircraftView());
        btnFlight.setOnAction(e -> showFlightView());
        btnReports.setOnAction(e -> showReportsView());
        
        // 🔥 3. 修复 Exit 按钮：先保存再退出
        btnExit.setOnAction(e -> {
            system.saveData();
            System.exit(0);
        });

        menu.getChildren().addAll(title, new Separator(), btnDashboard, btnAircraft, btnFlight, btnReports, new Separator(), btnExit);
        return menu;
    }

    private Button createMenuButton(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("menu-btn");
        return btn;
    }

    // ================== 1. Dashboard View ==================
    private void showDashboard() {
        centerContent.getChildren().clear();
        Label header = new Label("Operational Dashboard");
        header.getStyleClass().add("content-header");

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

        Label listHeader = new Label("Live Flight Status Board");
        listHeader.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        listHeader.setPadding(new Insets(20, 0, 10, 0));

        TableView<Flight> statusTable = new TableView<>();
        statusTable.getStyleClass().add("live-board");
        statusTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        statusTable.setPrefHeight(400);

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

        statusTable.getColumns().addAll(colNo, colRoute, colTime, colStatus);
        statusTable.setItems(FXCollections.observableArrayList(system.getAllFlights()));

        applyTableClip(statusTable);
        centerContent.getChildren().addAll(header, statsBox, listHeader, statusTable);
    }

    // ================== 2. Aircraft View ==================
    private void showAircraftView() {
        centerContent.getChildren().clear();
        Label header = new Label("Aircraft Management");
        header.getStyleClass().add("content-header");

        TextField searchField = new TextField();
        searchField.setPromptText("Search Reg No, Brand or Model...");
        searchField.setMaxWidth(400);
        searchField.setStyle("-fx-padding: 8px; -fx-border-color: #ccc; -fx-border-radius: 5px;");

        ObservableList<Aircraft> masterData = FXCollections.observableArrayList(system.getAllAircrafts());
        FilteredList<Aircraft> filteredData = new FilteredList<>(masterData, p -> true);

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
        VBox.setVgrow(table, Priority.ALWAYS);

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

        colStatus.setCellFactory(col -> new TableCell<Aircraft, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    if ("Available".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    else if ("Maintenance".equalsIgnoreCase(item)) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");//
                    else setStyle("-fx-text-fill: #2c3e50;");
                } else { setText(null); setStyle(""); }
            }
        });

        table.getColumns().addAll(colReg, colBrand, colModel, colCap, colStatus);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        Button btnAdd = new Button("Add New Aircraft");
        btnAdd.getStyleClass().addAll("btn", "btn-primary");
        Button btnDelete = new Button("Delete Selected");
        btnDelete.getStyleClass().addAll("btn", "btn-danger");

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
        centerContent.getChildren().addAll(header, searchField, table, actions);
    }

    // ================== 3. Flight View ==================
    private void showFlightView() {
        centerContent.getChildren().clear();
        Label header = new Label("Flight Management");
        header.getStyleClass().add("content-header");

        TextField searchField = new TextField();
        searchField.setPromptText("Search Flight No, Origin or Destination...");
        searchField.setMaxWidth(400);
        searchField.setStyle("-fx-padding: 8px; -fx-border-color: #ccc; -fx-border-radius: 5px;");

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

        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        TableColumn<Flight, String> colOrigin = new TableColumn<>("Origin");
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        TableColumn<Flight, String> colDest = new TableColumn<>("Destination");
        colDest.setCellValueFactory(new PropertyValueFactory<>("destination"));
        TableColumn<Flight, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<Flight, String> colPlane = new TableColumn<>("Aircraft");
        colPlane.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAircraft().getRegistrationNumber()));

        colStatus.setCellFactory(col -> new TableCell<Flight, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item);
                    if (item.equalsIgnoreCase("Delayed") || item.equalsIgnoreCase("Cancelled")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    else if (item.equalsIgnoreCase("Departed") ||item.equalsIgnoreCase("Arrived") || item.equalsIgnoreCase("In Flight")) setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    else setStyle("-fx-text-fill: #2c3e50;");//
                } else { setText(null); setStyle(""); }
            }
        });

        table.getColumns().addAll(colNo, colOrigin, colDest, colStatus, colPlane);

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
            if (selected != null) { showUpdateStatusDialog(selected); table.refresh(); }
            else showAlert("Select Flight", "Please select a flight first.");
        });

        btnCancel.setOnAction(e -> {
            Flight selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                    "Are you sure you want to cancel flight " + selected.getFlightNumber() + "?", 
                    ButtonType.YES, ButtonType.NO);
                confirm.showAndWait().ifPresent(res -> {
                    if (res == ButtonType.YES) {
                        system.deleteFlight(selected.getFlightNumber());
                        showFlightView();
                    }
                });
            } else { showAlert("Warning", "Please select a flight to cancel."); }
        });

        actions.getChildren().addAll(btnAdd, btnStatus, btnCancel);
        applyTableClip(table);
        centerContent.getChildren().addAll(header, searchField, table, actions);
    }

    // =================================================
    // 4. Reports & Logs View (🔥 按钮切换模式 + 表格)
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

        // 创建两个漂亮的切换按钮
        Button btnAnalysis = new Button("Operational Statistics");
        btnAnalysis.getStyleClass().addAll("btn","btn-primary"); // 🔥 应用渐变样式

        Button btnHistory = new Button("Flight History");
        btnHistory.getStyleClass().addAll("btn", "btn-primary"); // 🔥 应用渐变样式

        // 按钮点击事件：切换内容
        btnAnalysis.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createAnalysisTables());
        });

        btnHistory.setOnAction(e -> {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(createHistoryTable());
        });

        HBox actions = new HBox(15, btnAnalysis, btnHistory);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(15, 0, 0, 0));

        centerContent.getChildren().addAll(header, contentArea, actions);
    }

    // 辅助方法：创建分析页 (左右两个表格)
    private Node createAnalysisTables() {
        HBox container = new HBox(20);
        container.setPadding(new Insets(15));
        
        // 左表
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
        
        ObservableList<StatRow> statData = FXCollections.observableArrayList(
            new StatRow("Scheduled / Boarding", scheduled),
            new StatRow("Departed", active),
            new StatRow("Completed (Arrived)", completed),
            new StatRow("Delayed", delayed),
            new StatRow("Cancelled", cancelled),
            new StatRow("TOTAL FLIGHTS", system.getAllFlights().size())
        );
        statsTable.setItems(statData);
        applyTableClip(statsTable);

        // 右表
        TableView<DelayRow> delayTable = new TableView<>();
        delayTable.getStyleClass().add("live-board");
        delayTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<DelayRow, String> colFlight = new TableColumn<>("Flight No");
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flightNo"));
        TableColumn<DelayRow, String> colReason = new TableColumn<>("Delay Reason");
        colReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        delayTable.getColumns().addAll(colFlight, colReason);
        
        ObservableList<DelayRow> delayData = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            for (String r : f.getDelayReasons()) {
                delayData.add(new DelayRow(f.getFlightNumber(), r));
            }
        }
        delayTable.setItems(delayData);
        delayTable.setPlaceholder(new Label("No delays reported. Good job!"));
        applyTableClip(delayTable);

        VBox leftBox = new VBox(6, new Label("Status Overview"), statsTable);
        VBox rightBox = new VBox(10, new Label("Delay Details"), delayTable);
        VBox.setVgrow(statsTable, Priority.ALWAYS);
        VBox.setVgrow(delayTable, Priority.ALWAYS);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        HBox.setHgrow(rightBox, Priority.ALWAYS);

        container.getChildren().addAll(leftBox, rightBox);
        return container;
    }

    // 辅助方法：创建历史记录表格
    private Node createHistoryTable() {
        TableView<Flight> historyTable = new TableView<>();
        historyTable.getStyleClass().add("live-board");
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        ObservableList<Flight> arrivedFlights = FXCollections.observableArrayList();
        for (Flight f : system.getAllFlights()) {
            if ("Arrived".equalsIgnoreCase(f.getStatus())) {
                arrivedFlights.add(f);
            }
        }
        historyTable.setItems(arrivedFlights);

        TableColumn<Flight, String> colNo = new TableColumn<>("Flight No");
        colNo.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        TableColumn<Flight, String> colRoute = new TableColumn<>("Route");
        colRoute.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getOrigin() + " -> " + cell.getValue().getDestination()));
        TableColumn<Flight, String> colTime = new TableColumn<>("Arrival Time");
        colTime.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getArrivalTime().toString().replace("T", " ")));
        TableColumn<Flight, String> colPlane = new TableColumn<>("Aircraft");
        colPlane.setCellValueFactory(cell -> new SimpleStringProperty(
            cell.getValue().getAircraft().getRegistrationNumber()));

        historyTable.getColumns().addAll(colNo, colRoute, colTime, colPlane);
        historyTable.setPlaceholder(new Label("No history records available."));
        applyTableClip(historyTable);

        VBox box = new VBox(historyTable);
        box.setPadding(new Insets(15));
        return box;
    }

    // =================================================
    // 5. Dialogs (输入拦截 + 冲突检测)
    // =================================================

    private void showAddAircraftDialog() {
        Dialog<Aircraft> dialog = new Dialog<>();
        dialog.setTitle("Add New Aircraft");
        dialog.setHeaderText("Enter aircraft details");
        ButtonType addBtnType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField regField = new TextField(); regField.setPromptText("e.g. 9M-ABC");
        TextField brandField = new TextField();
        TextField modelField = new TextField();
        TextField capField = new TextField();

        grid.add(new Label("Reg No:"), 0, 0); grid.add(regField, 1, 0);
        grid.add(new Label("Brand:"), 0, 1); grid.add(brandField, 1, 1);
        grid.add(new Label("Model:"), 0, 2); grid.add(modelField, 1, 2);
        grid.add(new Label("Capacity:"), 0, 3); grid.add(capField, 1, 3);
        dialog.getDialogPane().setContent(grid);

        Button addBtn = (Button) dialog.getDialogPane().lookupButton(addBtnType);
        addBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            try {
                if (regField.getText().trim().isEmpty()) {
                    showAlert("Validation Error", "Registration Number cannot be empty.");
                    event.consume(); return;
                }
                Integer.parseInt(capField.getText());
            } catch (NumberFormatException ex) {
                showAlert("Validation Error", "Capacity must be a valid number.");
                event.consume(); 
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == addBtnType) {
                return new Aircraft(regField.getText(), brandField.getText(), modelField.getText(), 
                        Integer.parseInt(capField.getText()), "Available");
            }
            return null;
        });

        dialog.showAndWait().ifPresent(a -> system.addAircraft(a));
    }

    private void showAddFlightDialog() {
        Dialog<Flight> dialog = new Dialog<>();
        dialog.setTitle("Add New Flight");
        dialog.setHeaderText("Create schedule");
        ButtonType createBtnType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField flightNoField = new TextField(); flightNoField.setPromptText("e.g. MH-101");
        TextField destField = new TextField();
        ComboBox<String> aircraftBox = new ComboBox<>();
        
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
        cargoCheckbox.setOnAction(e -> cargoWeightField.setDisable(!cargoCheckbox.isSelected()));

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
            if (aircraftBox.getValue() == null) {
                showAlert("Error", "Please select an aircraft.");
                event.consume(); return;
            }
            if (datePicker.getValue() == null) {
                showAlert("Error", "Please select a date.");
                event.consume(); return;
            }
            if (!flightNoField.getText().matches("^[A-Z]{2,3}-\\d{3,4}$")) {
                showAlert("Validation", "Invalid Flight No. Format (e.g. MH-101).");
                event.consume(); return;
            }

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

                if (cargoCheckbox.isSelected()) {
                    Double.parseDouble(cargoWeightField.getText());
                }
            } catch (NumberFormatException e) {
                showAlert("Error", "Cargo weight must be a valid number.");
                event.consume();
            }
        });

        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                String reg = aircraftBox.getValue();
                Aircraft plane = system.getAircraft(reg);
                String fNum = flightNoField.getText();
                String dest = destField.getText();
                LocalDateTime dep = LocalDateTime.of(datePicker.getValue(), 
                        LocalTime.of(Integer.parseInt(hourBox.getValue()), Integer.parseInt(minBox.getValue())));
                LocalDateTime arr = dep.plusHours(2);

                if (cargoCheckbox.isSelected()) {
                    double w = Double.parseDouble(cargoWeightField.getText());
                    return new CargoFlight(fNum, "Batu Pahat", dest, dep, arr, plane, w);
                } else {
                    return new Flight(fNum, "Batu Pahat", dest, dep, arr, plane);
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(f -> {
            system.addFlight(f);
            f.getAircraft().setStatus("Scheduled");
        });
    }

    private void showUpdateStatusDialog(Flight flight) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(flight.getStatus(), 
            "Boarding", "Departed", "Arrived", "Delayed", "Cancelled");
        dialog.setTitle("Update Status");
        dialog.setHeaderText("Update status for " + flight.getFlightNumber());
        dialog.setContentText("New Status:");

        dialog.showAndWait().ifPresent(newStatus -> {
            system.updateFlightStatus(flight.getFlightNumber(), newStatus);
            if ("Delayed".equals(newStatus)) {
                TextInputDialog reason = new TextInputDialog();
                reason.setHeaderText("Enter Delay Reason");
                reason.showAndWait().ifPresent(flight::addDelayReason);
            }
        });
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title); alert.setContentText(content);
        alert.showAndWait();
    }
    
    // 辅助方法：创建卡片
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
    
    // 辅助方法：圆角剪裁
    private void applyTableClip(Region region) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(30); clip.setArcHeight(30);
        clip.widthProperty().bind(region.widthProperty());
        clip.heightProperty().bind(region.heightProperty());
        region.setClip(clip);
    }

    public static void main(String[] args) { launch(); }

    // Helper Classes
    public static class StatRow {
        private final String category;
        private final int count;
        public StatRow(String category, int count) { this.category = category; this.count = count; }
        public String getCategory() { return category; }
        public int getCount() { return count; }
    }

    public static class DelayRow {
        private final String flightNo;
        private final String reason;
        public DelayRow(String flightNo, String reason) { this.flightNo = flightNo; this.reason = reason; }
        public String getFlightNo() { return flightNo; }
        public String getReason() { return reason; }
    }
}