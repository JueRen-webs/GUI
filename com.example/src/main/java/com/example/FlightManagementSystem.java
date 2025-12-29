package com.example;

import java.util.*;
import java.time.LocalDateTime;

public class FlightManagementSystem {
    // ==========================================
    // 1. DATA & CONSTRUCTOR (数据与构造区)
    // ==========================================
    private Map<String, Flight> flights;
    private Map<String, Aircraft> aircrafts;

    public FlightManagementSystem() {
        flights = new HashMap<>();
        aircrafts = new HashMap<>();
    }

    // ==========================================
    // 2. AIRCRAFT MANAGEMENT (飞机管理模块)
    // ==========================================

    // [入口] 飞机管理子菜单
    public void listAllAircraft() {
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("\n====================================================================== "
                    + "\n\t\t\tALL AIRCRAFT "
                    + "\n======================================================================");
            if (aircrafts.isEmpty()) {
                System.out.println("(No aircraft available)");
            } else {
                System.out.printf("%-5s | %-10s | %-10s | %-9s | %-21s|\n",
                        "Reg No.", "Brand", "Model", "Capacity", "Status");
                System.out.println("----------------------------------------------------------------------");
                for (Aircraft aircraft : aircrafts.values()) {
                    System.out.println(aircraft);
                }
            }

            System.out.println("\n--- Aircraft Menu ---");
            System.out.println("1. Add New Aircraft");
            System.out.println("2. Update Aircraft Details");
            System.out.println("3. Delete Aircraft");
            System.out.println("4. Return to Main Menu");
            System.out.print("Select Operation: ");

            choice = getValidOption(scanner, 1, 4);

            switch (choice) {
                case 1:
                    newAircraft(scanner);
                    break;

                case 2:
                    // 修改：先获取输入，再传给 searchAircraft
                    System.out.print("Enter Registration to Update: ");
                    String upReg = scanner.nextLine().trim();
                    Aircraft target = searchAircraft(upReg, scanner);

                    if (target != null) {
                        updateAircraftDetails(target, scanner);
                    }
                    break;

                case 3:
                    // 修改：先获取输入，再传给 searchAircraft
                    System.out.println("--- DELETE AIRCRAFT ---");
                    System.out.print("Enter Registration to Delete: ");
                    String delReg = scanner.nextLine().trim();
                    Aircraft toDelete = searchAircraft(delReg, scanner);

                    if (toDelete != null) {
                        System.out.print("Confirm delete " + toDelete.getRegistrationNumber() + "? (y/n): ");
                        if (scanner.nextLine().equalsIgnoreCase("y")) {
                            deleteAircraft(toDelete.getRegistrationNumber());
                        }
                    }
                    break;

                case 4:
                    System.out.println("Returning...");
                    break;

                default:
                    System.out.println("Invalid Input!");
            }
        } while (choice != 4);
    }

    // 逻辑：新增飞机
    public void newAircraft(Scanner scanner) {
        System.out.println("\n--- NEW AIRCRAFT ---");
        System.out.print("Enter Registration Number: ");
        String regNum = scanner.nextLine();
        System.out.print("Enter Brand: ");
        String brand = scanner.nextLine();
        System.out.print("Enter Model: ");
        String model = scanner.nextLine();
        System.out.print("Enter Capacity: ");
        int capacity = scanner.nextInt();
        scanner.nextLine(); // 吃掉回车

        // 默认状态为 Available
        Aircraft newAircraft = new Aircraft(regNum, brand, model, capacity, "Available");
        addAircraft(newAircraft);
    }

    // 逻辑：修改飞机详情
    public void updateAircraftDetails(Aircraft target, Scanner scanner) {
        boolean editing = true;
        while (editing) {
            System.out.println("\n--- UPDATE: " + target.getRegistrationNumber() + " ---");
            System.out.println("1. Edit Brand");
            System.out.println("2. Edit Model");
            System.out.println("3. Edit Capacity");
            System.out.println("4. Edit Status");
            System.out.println("5. Finish");
            System.out.print("Select: ");

            int choice = getValidOption(scanner, 1, 5);

            switch (choice) {
                case 1:
                    System.out.print("New Brand: ");
                    target.setBrand(scanner.nextLine());
                    System.out.println("Updated.");
                    break;
                case 2:
                    System.out.print("New Model: ");
                    target.setModel(scanner.nextLine());
                    System.out.println("Updated.");
                    break;
                case 3:
                    System.out.print("New Capacity: ");
                    target.setCapacity(scanner.nextInt());
                    scanner.nextLine();
                    System.out.println("Updated.");
                    break;
                case 4:
                    System.out.println("Updating Status...");
                    String newStatus = getStatusFromMenu(scanner);
                    target.setStatus(newStatus);
                    System.out.println("Status updated to: " + newStatus);
                    break;
                case 5:
                    editing = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    // 基础操作：Add/Delete/Get
    public void addAircraft(Aircraft aircraft) {
        aircrafts.put(aircraft.getRegistrationNumber(), aircraft);
        System.out.println("Aircraft added: " + aircraft.getRegistrationNumber());
    }

    public void deleteAircraft(String regNum) {
        if (aircrafts.remove(regNum) != null) {
            System.out.println("Aircraft deleted successfully.");
        } else {
            System.out.println("Aircraft not found.");
        }
    }

    public Aircraft getAircraft(String regNum) {
        return aircrafts.get(regNum);
    }

    // ==========================================
    // 3. FLIGHT MANAGEMENT (航班管理模块)
    // ==========================================

    // [入口] 航班管理子菜单
    public void flightStatus(Scanner scanner) {
        int choice;
        do {
            System.out.println("\n=== FLIGHT MANAGEMENT MENU ===");
            System.out.println("[1] Add New Flight");
            System.out.println("[2] Update Flight Status");
            System.out.println("[3] Cancel Flight");
            System.out.println("[4] Return");
            System.out.print("Select Operation: ");

            choice = getValidOption(scanner, 1, 4);

            switch (choice) {
                case 1:
                    newFlight(scanner);
                    break;

                case 2:
                    handleUpdateFlightStatus(scanner);
                    break;

                case 3:
                    Flight fToDelete = searchFlight(scanner);
                    if (fToDelete != null) {
                        System.out.print("Confirm cancel flight " + fToDelete.getFlightNumber() + "? (y/n): ");
                        if (scanner.nextLine().equalsIgnoreCase("y")) {
                            deleteFlight(fToDelete.getFlightNumber());
                            // 释放飞机
                            if (fToDelete.getAircraft() != null) {
                                fToDelete.getAircraft().setStatus("Available");
                                System.out.println("Aircraft released.");
                            }
                        }
                    }
                    break;

                case 4:
                    System.out.println("Returning...");
                    break;

                default:
                    System.out.println("Invalid input.");
            }
        } while (choice != 4);
    }

    // 逻辑：新增航班
    public void newFlight(Scanner scanner) {
        System.out.println("\n--- CREATE NEW FLIGHT ---");
        System.out.print("Enter Flight Number: ");
        String flightNum = scanner.nextLine();

        String origin = "Batu Pahat";
        System.out.print("Enter Destination: ");
        String destination = scanner.nextLine();

        // 分配飞机循环
        Aircraft assignedAircraft = null;
        while (assignedAircraft == null) {
            System.out.print("Please enter the Aircraft Registration to assign: ");
            String inputReg = scanner.nextLine().trim();
            assignedAircraft = searchAircraft(inputReg, scanner);

            if (assignedAircraft == null) {
                System.out.println("Aircraft required. Try again.");
            } else if (!assignedAircraft.getStatus().equalsIgnoreCase("Available")) {
                System.out.println("Warning: Aircraft is " + assignedAircraft.getStatus());
                System.out.print("Use it anyway? (y/n): ");
                if (!scanner.nextLine().equalsIgnoreCase("y")) {
                    assignedAircraft = null;
                }
            }
        }

        // 自动生成时间
        LocalDateTime depTime = LocalDateTime.now().plusHours(2);
        LocalDateTime arrTime = depTime.plusHours(3);

        Flight newFlight = new Flight(flightNum, origin, destination, depTime, arrTime, assignedAircraft);
        addFlight(newFlight);
    }

    // 基础操作：Add/Update/Delete/Display
    public void addFlight(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
        System.out.println("Flight added: " + flight.getFlightNumber());
    }

    public void updateFlightStatus(String flightNum, String status) {
        Flight flight = flights.get(flightNum);
        if (flight != null) {
            flight.setStatus(status);
            System.out.println("Flight status updated.");
        }
    }

    public void deleteFlight(String flightNum) {
        if (flights.remove(flightNum) != null) {
            System.out.println("Flight deleted successfully.");
        } else {
            System.out.println("Flight not found.");
        }
    }

    public void displayRealTimeFlightStatus() {
        System.out.println(
                "\n------------------------------------REAL-TIME FLIGHT STATUS-----------------------------------------");
        System.out.printf("| %-10s | %-30s | %-15s | %-15s | %s%n",
                "Flight No", "Route", "Status", "Aircraft", "Departure");
        System.out.println(
                "----------------------------------------------------------------------------------------------------");
        for (Flight flight : flights.values()) {
            System.out.println(flight);
        }
    }

    // ==========================================
    // 4. REPORTS & LOGS (报告与日志模块)
    // ==========================================

    // 处理 Main Case 3 的输入逻辑
    // ✅ 新方法：显示所有已抵达航班的完整历史报告
    public void printAllArrivedReports() {
        System.out.println("\n======================================================================");
        System.out.println("               FULL FLIGHT HISTORY REPORT (ARRIVED)                   ");
        System.out.println("======================================================================");

        boolean hasRecord = false;
        int count = 0;

        // 遍历所有航班
        for (Flight flight : flights.values()) {
            // 核心条件：只看已抵达的 (Arrived)
            if (flight.getStatus().equalsIgnoreCase("Arrived")) {
                count++;
                hasRecord = true;

                System.out.println("\n----------------------------------------------------------------------");
                System.out.printf(" %d. FLIGHT %s  (Aircraft: %s)\n",
                        count, flight.getFlightNumber(), flight.getAircraft().getRegistrationNumber());
                System.out.println("----------------------------------------------------------------------");

                // 1. 基础航线信息
                System.out.printf(" Route:      %s -> %s\n", flight.getOrigin(), flight.getDestination());
                System.out.printf(" Departure:  %s\n", flight.getDepartureTime().toString().replace("T", " "));
                System.out.printf(" Arrival:    %s\n", flight.getArrivalTime().toString().replace("T", " ")); // 这里显示预计到达，如果你加了实际到达时间可以改这里

                System.out.println("\n [ FLIGHT LOGS ]");

                // 2. 延误记录
                if (flight.getDelayReasons().isEmpty()) {
                    System.out.println(" - Delays:    [None]");
                } else {
                    System.out.println(" - Delays:");
                    for (String reason : flight.getDelayReasons()) {
                        System.out.println("      " + reason);
                    }
                }

                // 3. 事故记录
                if (flight.getIncidents().isEmpty()) {
                    System.out.println(" - Incidents: [None]");
                } else {
                    System.out.println(" - Incidents:");
                    for (String incident : flight.getIncidents()) {
                        System.out.println("      " + incident);
                    }
                }

                // 4. 机长备注
                if (flight.getCaptainNotes().isEmpty()) {
                    System.out.println(" - Notes:     [None]");
                } else {
                    System.out.println(" - Notes:\n      " + flight.getCaptainNotes());
                }
            }
        }

        if (!hasRecord) {
            System.out.println("\n No arrived flights found in the system history.");
        }

        System.out.println("\n======================================================================");
        System.out.println("\t\t\t2End of Report");
        System.out.println("======================================================================\n");
    }

    public void handleUpdateFlightStatus(Scanner scanner) {
        System.out.println("\n--- UPDATE FLIGHT STATUS ---");
        Flight fToUpdate = searchFlight(scanner);

        if (fToUpdate != null) {
            System.out.println("Current Status: " + fToUpdate.getStatus());
            System.out.println("Assigned Aircraft: " + fToUpdate.getAircraft().getRegistrationNumber());

            // 1. 获取新状态
            String newStatus = getFlightStatusFromMenu(scanner);
            updateFlightStatus(fToUpdate.getFlightNumber(), newStatus);

            // --- 🔥 智能追问逻辑 (Smart Follow-up) ---

            // 情况 A: 延误 (Delayed) 或 取消 (Cancelled) -> 必问原因
            if (newStatus.equals("Delayed") || newStatus.equals("Cancelled")) {
                System.out.println("\n--- ABNORMAL STATUS DETECTED ---");
                System.out.print("Please enter the reason for " + newStatus + ": ");
                String reason = scanner.nextLine();
                fToUpdate.addDelayReason(reason);
                System.out.println("Reason logged.");
            }

            // 情况 B: 抵达 (Arrived) -> 问其他日志 (事故/备注)
            else if (newStatus.equals("Arrived")) {
                System.out.println("\n--- FLIGHT ARRIVED: LOG ENTRY ---");

                // 1. 问事故
                System.out.print("Any incidents to report? (Press Enter to skip): ");
                String incident = scanner.nextLine();
                if (!incident.isEmpty())
                    fToUpdate.addIncident(incident);

                // 2. 问备注
                System.out.print("Captain's Notes (Press Enter to skip): ");
                String notes = scanner.nextLine();
                if (!notes.isEmpty())
                    fToUpdate.setCaptainNotes(notes);

                System.out.println("Flight logs saved.");
            }

            // --- 自动释放/占用飞机逻辑 ---
            Aircraft assignedPlane = fToUpdate.getAircraft();
            if (assignedPlane != null) {
                if (newStatus.equals("Cancelled") || newStatus.equals("Arrived")) {
                    assignedPlane.setStatus("Available");
                    System.out.println(
                            "Auto-Update: Aircraft " + assignedPlane.getRegistrationNumber() + " is now Available.");
                } else if (newStatus.equals("Departed")) {
                    assignedPlane.setStatus("In Flight");
                    System.out.println(
                            "Auto-Update: Aircraft " + assignedPlane.getRegistrationNumber() + " is now In Flight.");
                }
            }
        }
    }

    public void generateOperationalAnalysis() {
        System.out.println("\n=======================================================");
        System.out.println("               OPERATIONAL ANALYSIS DASHBOARD          ");
        System.out.println("=======================================================");

        // --- 1. 基础数据 ---
        System.out.println("\n[1] OVERVIEW");
        System.out.printf("    %-20s: %d\n", "Total Flights", flights.size());
        System.out.printf("    %-20s: %d\n", "Total Aircraft", aircrafts.size());

        // --- 2. 状态分布 (原本的功能) ---
        System.out.println("\n[2] FLIGHT STATUS DISTRIBUTION");
        Map<String, Integer> statusCounts = new HashMap<>();
        for (Flight f : flights.values()) {
            statusCounts.put(f.getStatus(), statusCounts.getOrDefault(f.getStatus(), 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            System.out.printf("    %-20s: %d\n", entry.getKey(), entry.getValue());
        }

        // --- 3. 热门目的地 (Top Destinations) ---
        System.out.println("\n[3] POPULAR DESTINATIONS");
        Map<String, Integer> destCounts = new HashMap<>();
        for (Flight f : flights.values()) {
            destCounts.put(f.getDestination(), destCounts.getOrDefault(f.getDestination(), 0) + 1);
        }

        // 找出最热门的一个 (简单算法)
        String topDest = "None";
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : destCounts.entrySet()) {
            System.out.printf("    %-20s: %d flights\n", entry.getKey(), entry.getValue());
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                topDest = entry.getKey();
            }
        }
        System.out.println("    --------------------");
        System.out.println("     Top Destination  : " + topDest);

        // --- 4. 运营健康度 (延误与事故) ---
        System.out.println("\n[4] OPERATIONAL HEALTH");
        int totalDelays = 0;
        int totalIncidents = 0;

        for (Flight f : flights.values()) {
            if (!f.getDelayReasons().isEmpty())
                totalDelays++;
            if (!f.getIncidents().isEmpty())
                totalIncidents++;
        }

        System.out.printf("    %-20s: %d\n", "Flights w/ Delays", totalDelays);
        System.out.printf("    %-20s: %d\n", "Flights w/ Incidents", totalIncidents);

        // 计算延误率 (简单的百分比)
        if (flights.size() > 0) {
            double delayRate = (double) totalDelays / flights.size() * 100;
            System.out.printf("    %-20s: %.1f%%\n", "Delay Rate", delayRate);
        }

        // --- 5. 机队偏好 (Brand Analysis) ---
        System.out.println("\n[5] FLEET UTILIZATION (By Brand)");
        Map<String, Integer> brandCounts = new HashMap<>();
        for (Flight f : flights.values()) {
            String brand = f.getAircraft().getBrand();
            brandCounts.put(brand, brandCounts.getOrDefault(brand, 0) + 1);
        }

        for (Map.Entry<String, Integer> entry : brandCounts.entrySet()) {
            System.out.printf("    %-20s: %d flights serviced\n", entry.getKey(), entry.getValue());
        }

        System.out.println("=======================================================\n");
    }

    // ==========================================
    // 5. HELPER METHODS (工具/搜索模块)
    // ==========================================

    // 通用搜索：飞机 (修复了中文逗号问题)
    public Aircraft searchAircraft(String inputReg, Scanner scanner) {
        // 1. 精确查找
        for (String key : aircrafts.keySet()) {
            if (key.equalsIgnoreCase(inputReg))
                return aircrafts.get(key);
        }

        // 2. 模糊查找
        System.out.println("Aircraft '" + inputReg + "' not found.");
        List<String> suggestions = new ArrayList<>();
        for (String key : aircrafts.keySet()) {
            if (key.toUpperCase().contains(inputReg.toUpperCase()))
                suggestions.add(key);
        }

        if (!suggestions.isEmpty()) {
            System.out.println("Did you mean: " + suggestions);
        }
        return null;
    }

    // 通用搜索：航班 (带模糊搜索建议)
    public Flight searchFlight(Scanner scanner) {
        System.out.print("\nEnter Flight Number to Search: ");
        String input = scanner.nextLine().trim();

        List<String> suggestions = new ArrayList<>(); // 用来存相似的航班号

        // --- 核心搜索逻辑 ---
        for (String key : flights.keySet()) {
            // 1. 精确查找 (Exact Match)
            if (key.equalsIgnoreCase(input)) {
                return flights.get(key); // 找到了直接返回
            }

            // 2. 模糊查找 (Fuzzy Search) - 收集包含输入字符的航班号
            if (key.toUpperCase().contains(input.toUpperCase())) {
                suggestions.add(key);
            }
        }

        // --- 没找到的处理 ---
        System.out.println(" Flight '" + input + "' not found.");

        // 如果有相似的结果，提示用户
        if (!suggestions.isEmpty()) {
            System.out.println(" Did you mean one of these?");
            for (String s : suggestions) {
                System.out.println("   - " + s);
            }
        }

        return null;
    }

    // 菜单工具：选择飞机状态
    public String getStatusFromMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n[1] Available  \n[2] In Flight  \n[3] Maintenance");
            System.out.print("Choose: ");
            int choice = getValidOption(scanner, 1, 3);
            switch (choice) {
                case 1:
                    return "Available";
                case 2:
                    return "In Flight";
                case 3:
                    return "Maintenance";
                default:
                    return "Available"; // 理论上永远不会走到这里
            }
        }
    }

    // 菜单工具：选择航班状态
    public String getFlightStatusFromMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n[1] Boarding  \n[2] Departed\n[3] Arrived    \n[4] Delayed   \n[5] Cancelled");
            System.out.print("Choose: ");
            int choice = getValidOption(scanner, 1, 5);

            switch (choice) {
                case 1:
                    return "Boarding";
                case 2:
                    return "Departed";
                case 3:
                    return "Arrived";
                case 4:
                    return "Delayed";
                case 5:
                    return "Cancelled";
                default:
                    return "Scheduled"; // 理论上不会走到这里
            }
        }
    }

    // ✅ 新增：通用菜单输入验证方法 (最强卫士)
    public int getValidOption(Scanner scanner, int min, int max) {
        int choice;
        while (true) { // 无限循环，直到用户输对
            // 1. 检查是不是数字
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // ⚠️ 关键：吃掉数字后的回车符

                // 2. 检查数字范围
                if (choice >= min && choice <= max) {
                    return choice; // 完美！返回这个有效的数字
                } else {
                    System.out.print("Option out of range (" + min + "-" + max + "). \nTry again: ");
                }
            } else {
                // 3. 处理非数字输入 (防止崩溃)
                String wrongInput = scanner.nextLine(); // 把错误的文字读走，清空缓冲区
                System.out.print("Invalid input [" + wrongInput + "]. \nPlease enter a number: ");
            }
        }
    }
    // ==========================================
    // 6. GUI SUPPORT (为了给 App.java 提供数据)
    // ==========================================

    // 获取所有飞机列表
    public java.util.Collection<Aircraft> getAllAircrafts() {
        return aircrafts.values();
    }

    // 获取所有航班列表
    public java.util.Collection<Flight> getAllFlights() {
        return flights.values();
    }

    // ==========================================
    // 7. REPORTS GENERATION (为 GUI 生成文字报告)
    // ==========================================

    // 把原本的 generateOperationalAnalysis 改写成返回 String
    public String getOperationalAnalysisReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=======================================================\n");
        sb.append("               OPERATIONAL ANALYSIS DASHBOARD          \n");
        sb.append("=======================================================\n\n");

        // 1. 基础数据
        sb.append("[1] OVERVIEW\n");
        sb.append(String.format("    %-20s: %d\n", "Total Flights", flights.size()));
        sb.append(String.format("    %-20s: %d\n", "Total Aircraft", aircrafts.size()));

        // 2. 状态分布
        sb.append("\n[2] FLIGHT STATUS DISTRIBUTION\n");
        Map<String, Integer> statusCounts = new HashMap<>();
        for (Flight f : flights.values()) {
            statusCounts.put(f.getStatus(), statusCounts.getOrDefault(f.getStatus(), 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            sb.append(String.format("    %-20s: %d\n", entry.getKey(), entry.getValue()));
        }

        // 3. 运营健康度
        sb.append("\n[3] OPERATIONAL HEALTH\n");
        int totalDelays = 0;
        int totalIncidents = 0;
        for (Flight f : flights.values()) {
            if (!f.getDelayReasons().isEmpty())
                totalDelays++;
            if (!f.getIncidents().isEmpty())
                totalIncidents++;
        }
        sb.append(String.format("    %-20s: %d\n", "Flights w/ Delays", totalDelays));
        sb.append(String.format("    %-20s: %d\n", "Flights w/ Incidents", totalIncidents));

        sb.append("\n=======================================================\n");
        return sb.toString();
    }

    // 把原本的 printAllArrivedReports 改写成返回 String
    public String getFlightHistoryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================================\n");
        sb.append("          FULL FLIGHT HISTORY REPORT (ARRIVED)        \n");
        sb.append("======================================================\n");

        boolean hasRecord = false;
        int count = 0;

        for (Flight flight : flights.values()) {
            if (flight.getStatus().equalsIgnoreCase("Arrived")) {
                count++;
                hasRecord = true;
                sb.append("\n------------------------------------------------------\n");
                sb.append(String.format(" %d. FLIGHT %s  (Aircraft: %s)\n",
                        count, flight.getFlightNumber(), flight.getAircraft().getRegistrationNumber()));
                sb.append(String.format(" Route:      %s -> %s\n", flight.getOrigin(), flight.getDestination()));
                sb.append(String.format(" Departure:  %s\n", flight.getDepartureTime().toString().replace("T", " ")));
                sb.append(String.format(" Arrival:    %s\n", flight.getArrivalTime().toString().replace("T", " ")));

                // 延误信息
                if (!flight.getDelayReasons().isEmpty()) {
                    sb.append(" - Delays:\n");
                    for (String reason : flight.getDelayReasons())
                        sb.append("      " + reason + "\n");
                }
                // 事故信息
                if (!flight.getIncidents().isEmpty()) {
                    sb.append(" - Incidents:\n");
                    for (String incident : flight.getIncidents())
                        sb.append("      " + incident + "\n");
                }
            }
        }

        if (!hasRecord)
            sb.append("\n No arrived flights found in history.\n");
        sb.append("\n================ End of Report ================\n");
        return sb.toString();
    }
}