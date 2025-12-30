package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class FlightManagementSystem {

    private Map<String, Flight> flights;
    private Map<String, Aircraft> aircrafts;

    public FlightManagementSystem() {
        this.flights = new HashMap<>();
        this.aircrafts = new HashMap<>();
    }

    // --- CRUD Methods ---
    public void addFlight(Flight flight) {
        flights.put(flight.getFlightNumber(), flight);
    }

    public void deleteFlight(String flightNumber) {
        flights.remove(flightNumber);
    }

    public Flight getFlight(String flightNumber) {
        return flights.get(flightNumber);
    }

    public List<Flight> getAllFlights() {
        return new ArrayList<>(flights.values());
    }

    public void updateFlightStatus(String flightNumber, String status) {
        Flight f = flights.get(flightNumber);
        if (f != null) {
            f.setStatus(status);
        }
    }

    public void addAircraft(Aircraft aircraft) {
        aircrafts.put(aircraft.getRegistrationNumber(), aircraft);
    }

    public void deleteAircraft(String regNumber) {
        aircrafts.remove(regNumber);
    }

    public Aircraft getAircraft(String regNumber) {
        return aircrafts.get(regNumber);
    }

    public List<Aircraft> getAllAircrafts() {
        return new ArrayList<>(aircrafts.values());
    }

    // 🔥🔥🔥 新增：检查飞机时间是否冲突 🔥🔥🔥
    public boolean isAircraftAvailable(String regNo, LocalDateTime newDep, LocalDateTime newArr) {
        for (Flight f : flights.values()) {
            // 1. 必须是同一架飞机
            if (f.getAircraft().getRegistrationNumber().equals(regNo)) {
                // 2. 如果航班已经取消，则不占用时间
                if ("Cancelled".equalsIgnoreCase(f.getStatus())) {
                    continue;
                }
                
                // 3. 检查时间重叠
                // 逻辑：(A开始 < B结束) 且 (A结束 > B开始)
                if (f.getDepartureTime().isBefore(newArr) && f.getArrivalTime().isAfter(newDep)) {
                    return false; // 冲突！不可用
                }
            }
        }
        return true; // 没有冲突，可用
    }

    // --- Reports ---
    public String getOperationalAnalysisReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== OPERATIONAL ANALYSIS REPORT ===\n");
        sb.append("Generated on: ").append(LocalDateTime.now()).append("\n\n");

        int total = flights.size();
        long delayed = flights.values().stream().filter(f -> "Delayed".equalsIgnoreCase(f.getStatus())).count();
        long cancelled = flights.values().stream().filter(f -> "Cancelled".equalsIgnoreCase(f.getStatus())).count();
        long completed = flights.values().stream().filter(f -> "Arrived".equalsIgnoreCase(f.getStatus())).count();

        sb.append(String.format("Total Scheduled Flights: %d\n", total));
        sb.append(String.format("Completed Flights:       %d\n", completed));
        sb.append(String.format("Delayed Flights:         %d\n", delayed));
        sb.append(String.format("Cancelled Flights:       %d\n", cancelled));
        
        boolean hasReasons = false;
        StringBuilder reasonsSb = new StringBuilder();
        reasonsSb.append("\n--- Delay Reasons ---\n");
        for (Flight f : flights.values()) {
            if (!f.getDelayReasons().isEmpty()) {
                hasReasons = true;
                reasonsSb.append("Flight ").append(f.getFlightNumber()).append(": ").append(f.getDelayReasons()).append("\n");
            }
        }
        
        if (hasReasons) {
            sb.append(reasonsSb);
        }

        return sb.toString();
    }

    public String getFlightHistoryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== FLIGHT HISTORY LOG ===\n\n");
        for (Flight f : flights.values()) {
            if ("Arrived".equals(f.getStatus())) {
                sb.append(f.toString()).append("\n");
            }
        }
        return sb.toString();
    }

    // --- File I/O ---
    public void saveData() {
        try {
            PrintWriter aircraftWriter = new PrintWriter(new FileWriter("aircrafts.txt"));
            for (Aircraft a : aircrafts.values()) {
                aircraftWriter.println(String.format("%s,%s,%s,%d,%s",
                        a.getRegistrationNumber(), a.getBrand(), a.getModel(), a.getCapacity(), a.getStatus()));
            }
            aircraftWriter.close();

            PrintWriter flightWriter = new PrintWriter(new FileWriter("flights.txt"));
            for (Flight f : flights.values()) {
                String isCargo = (f instanceof CargoFlight) ? "YES" : "NO";
                double cargoCap = (f instanceof CargoFlight) ? ((CargoFlight) f).getCargoCapacity() : 0.0;

                flightWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f",
                        f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                        f.getDepartureTime().toString(), f.getArrivalTime().toString(),
                        f.getStatus(), f.getAircraft().getRegistrationNumber(),
                        isCargo, cargoCap));
            }
            flightWriter.close();
            System.out.println("Data saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadData() {
        File aircraftFile = new File("aircrafts.txt");
        File flightFile = new File("flights.txt");

        if (!aircraftFile.exists() || !flightFile.exists()) {
            return;
        }

        try {
            Scanner sc = new Scanner(aircraftFile);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    Aircraft a = new Aircraft(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]);
                    addAircraft(a);
                }
            }
            sc.close();

            sc = new Scanner(flightFile);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");
                if (parts.length >= 9) {
                    String fNum = parts[0];
                    String org = parts[1];
                    String dest = parts[2];
                    LocalDateTime dep = LocalDateTime.parse(parts[3]);
                    LocalDateTime arr = LocalDateTime.parse(parts[4]);
                    String status = parts[5];
                    String planeReg = parts[6];
                    String isCargo = parts[7];
                    double cargoCap = Double.parseDouble(parts[8]);

                    Aircraft linkedPlane = getAircraft(planeReg);

                    if (linkedPlane != null) {
                        Flight f;
                        if ("YES".equals(isCargo)) {
                            f = new CargoFlight(fNum, org, dest, dep, arr, linkedPlane, cargoCap);
                        } else {
                            f = new Flight(fNum, org, dest, dep, arr, linkedPlane);
                        }
                        f.setStatus(status);
                        addFlight(f);
                    }
                }
            }
            sc.close();
            System.out.println("Data loaded successfully.");

        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
        }
    }
}