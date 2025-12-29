package com.example;

// ✅ 1. 这里是您要求的 Specific Imports (没有 *)
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class FlightManagementSystem {
    // ==========================================
    // 1. DATA & CONSTRUCTOR
    // ==========================================
    private Map<String, Flight> flights;
    private Map<String, Aircraft> aircrafts;

    public FlightManagementSystem() {
        flights = new HashMap<>();
        aircrafts = new HashMap<>();
    }

    // ==========================================
    // 2. AIRCRAFT MANAGEMENT
    // ==========================================

    public void addAircraft(Aircraft aircraft) {
        aircrafts.put(aircraft.getRegistrationNumber(), aircraft);
        // System.out.println 可以保留用于后台调试，但在GUI主要看界面反馈
        System.out.println("DEBUG: Aircraft added: " + aircraft.getRegistrationNumber());
    }

    public void deleteAircraft(String regNum) {
        if (aircrafts.remove(regNum) != null) {
            System.out.println("DEBUG: Aircraft deleted.");
        }
    }

    public Aircraft getAircraft(String regNum) {
        return aircrafts.get(regNum);
    }

    // ==========================================
    // 3. FLIGHT MANAGEMENT
    // ==========================================

    public void addFlight(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
        System.out.println("DEBUG: Flight added: " + flight.getFlightNumber());
    }

    public void updateFlightStatus(String flightNum, String status) {
        Flight flight = flights.get(flightNum);
        if (flight != null) {
            flight.setStatus(status);
            System.out.println("DEBUG: Flight status updated.");
        }
    }

    public void deleteFlight(String flightNum) {
        if (flights.remove(flightNum) != null) {
            System.out.println("DEBUG: Flight deleted.");
        }
    }

    // ❌ 已删除 displayRealTimeFlightStatus()，GUI 使用 TableView 显示，不需要控制台画表格

    // ==========================================
    // 4. GUI DATA SUPPORT (给界面提供数据列表)
    // ==========================================

    public Collection<Aircraft> getAllAircrafts() {
        return aircrafts.values();
    }

    public Collection<Flight> getAllFlights() {
        return flights.values();
    }

    // ==========================================
    // 5. SEARCH LOGIC (已重写：纯逻辑，无Scanner)
    // ==========================================

    // ✅ 重写：只负责逻辑，不负责和用户对话
    // GUI 控制器会调用这个方法，拿到结果后自己决定怎么显示
    public List<Aircraft> searchAircraft(String keyword) {
        List<Aircraft> results = new ArrayList<>();
        String upperKey = keyword.toUpperCase();

        for (Aircraft a : aircrafts.values()) {
            // 只要注册号包含关键词，就算找到
            if (a.getRegistrationNumber().toUpperCase().contains(upperKey)) {
                results.add(a);
            }
        }
        return results;
    }

    // ✅ 重写：只负责逻辑
    public List<Flight> searchFlight(String keyword) {
        List<Flight> results = new ArrayList<>();
        String upperKey = keyword.toUpperCase();

        for (Flight f : flights.values()) {
            // 只要航班号包含关键词，就算找到
            if (f.getFlightNumber().toUpperCase().contains(upperKey)) {
                results.add(f);
            }
        }
        return results;
    }

    // ==========================================
    // 6. REPORTS (生成文字报告供 GUI 弹窗使用)
    // ==========================================

    public String getOperationalAnalysisReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("      OPERATIONAL ANALYSIS DASHBOARD    \n");
        sb.append("========================================\n\n");

        sb.append("[1] OVERVIEW\n");
        sb.append(String.format("    %-20s: %d\n", "Total Flights", flights.size()));
        sb.append(String.format("    %-20s: %d\n", "Total Aircraft", aircrafts.size()));

        sb.append("\n[2] FLIGHT STATUS DISTRIBUTION\n");
        Map<String, Integer> statusCounts = new HashMap<>();
        for (Flight f : flights.values()) {
            statusCounts.put(f.getStatus(), statusCounts.getOrDefault(f.getStatus(), 0) + 1);
        }
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            sb.append(String.format("    %-20s: %d\n", entry.getKey(), entry.getValue()));
        }

        sb.append("\n[3] OPERATIONAL HEALTH\n");
        int totalDelays = 0;
        int totalIncidents = 0;
        for (Flight f : flights.values()) {
            if (!f.getDelayReasons().isEmpty()) totalDelays++;
            if (!f.getIncidents().isEmpty()) totalIncidents++;
        }
        sb.append(String.format("    %-20s: %d\n", "Flights w/ Delays", totalDelays));
        sb.append(String.format("    %-20s: %d\n", "Flights w/ Incidents", totalIncidents));

        return sb.toString();
    }

    public String getFlightHistoryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("======================================\n");
        sb.append("   FULL FLIGHT HISTORY REPORT (ARRIVED) \n");
        sb.append("======================================\n");

        boolean hasRecord = false;
        int count = 0;

        for (Flight flight : flights.values()) {
            if (flight.getStatus().equalsIgnoreCase("Arrived")) {
                count++;
                hasRecord = true;
                sb.append("\n--------------------------------------\n");
                sb.append(String.format(" %d. FLIGHT %s  (Aircraft: %s)\n",
                        count, flight.getFlightNumber(), flight.getAircraft().getRegistrationNumber()));
                sb.append(String.format(" Route:       %s -> %s\n", flight.getOrigin(), flight.getDestination()));
                sb.append(String.format(" Departure:   %s\n", flight.getDepartureTime().toString().replace("T", " ")));
                sb.append(String.format(" Arrival:     %s\n", flight.getArrivalTime().toString().replace("T", " ")));

                if (!flight.getDelayReasons().isEmpty()) {
                    sb.append(" - Delays:\n");
                    for (String reason : flight.getDelayReasons())
                        sb.append("       " + reason + "\n");
                }
                if (!flight.getIncidents().isEmpty()) {
                    sb.append(" - Incidents:\n");
                    for (String incident : flight.getIncidents())
                        sb.append("       " + incident + "\n");
                }
            }
        }

        if (!hasRecord)
            sb.append("\n No arrived flights found in history.\n");
        
        return sb.toString();
    }
}