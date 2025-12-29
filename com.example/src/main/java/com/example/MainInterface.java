package com.example;

import java.util.Scanner;
import java.time.LocalDateTime;

public class MainInterface {
    public static void main(String[] args) {
        FlightManagementSystem fms = new FlightManagementSystem();
        Scanner scanner = new Scanner(System.in);

        // --- 1. 初始化测试数据 (Sample Data) ---
        Aircraft a1 = new Aircraft("N12345", "Boeing", "737-800", 189, "In Flight");
        Aircraft a2 = new Aircraft("N67890", "Airbus", "A320", 180, "Available"); // 改为 Available 以便测试分配飞机
        fms.addAircraft(a1);
        fms.addAircraft(a2);

        Flight f1 = new Flight("AA101", "New York", "Los Angeles",
                LocalDateTime.of(2024, 12, 1, 10, 30),
                LocalDateTime.of(2024, 12, 1, 13, 45), a1);
        fms.addFlight(f1);

        // --- 2. 主循环 ---
        boolean isRunning = true;
        while (isRunning) {
            System.out.println(
                    "\n===================================================================================================");
            System.out.println("\t\t\t\tFLIGHT MANAGEMENT SYSTEM (FMS)      ");
            System.out.println(
                    "===================================================================================================");
            fms.displayRealTimeFlightStatus(); // 显示大屏

            System.out.println("\n--- MAIN MENU ---");
            System.out.println("[1] Flight Management (Add/Update/Cancel)");
            System.out.println("[2] Flight Report List"); // 原 [3] 变成 [2]
            System.out.println("[3] Aircraft Management"); // 原 [4] 变成 [3]
            System.out.println("[4] Operational Analysis"); // 原 [5] 变成 [4]
            System.out.println("[5] Exit"); // 原 [6] 变成 [5]
            System.out.print(">> Choose an option: ");

            // 防止输入非数字崩溃
            int choice = fms.getValidOption(scanner, 1, 5);

            switch (choice) {
                case 1:
                    fms.flightStatus(scanner); // 进入航班管理子菜单
                    break;
                case 2:
                    fms.printAllArrivedReports(); // ✅ 封装到 FMS 里
                    break;
                case 3:
                    fms.listAllAircraft(); // 进入飞机管理子菜单
                    break;
                case 4:
                    fms.generateOperationalAnalysis();
                    break;
                case 5:
                    System.out.println("Exiting system... Goodbye!");
                    isRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please enter 1-6.");
            }
        }
        scanner.close();
    }
}