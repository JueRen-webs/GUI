package fmsGUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FlightManagementSystem {

    private Map<String, Flight> flights;
    private Map<String, Aircraft> aircrafts;

    public FlightManagementSystem() {
        this.flights = new HashMap<>();
        this.aircrafts = new HashMap<>();
    }

    // --- 基础 CRUD ---
    public void addFlight(Flight flight) { 
        flights.put(flight.getFlightNumber(), flight); 
        
        // [修复] 防止覆盖掉 "In Flight" 或 "Departed" 这种正在进行的状态
        // 逻辑：只有当飞机当前是 "Available" (完全空闲) 时，才将其标记为 "Scheduled"
        // 如果它已经在飞 (In Flight) 或者已经有排期 (Scheduled)，则保持原状态不变
        if ("Available".equalsIgnoreCase(flight.getAircraft().getStatus())) {
            flight.getAircraft().setStatus("Scheduled"); 
        }
    }
    
    public void deleteFlight(String flightNumber) {
        Flight f = flights.get(flightNumber);
        if (f != null && f.getAircraft() != null) {
            // 删除后，如果飞机后面没事了，就设为 Available
            if (!hasFutureFlights(f.getAircraft().getRegistrationNumber())) {
                f.getAircraft().setStatus("Available");
            }
        }
        flights.remove(flightNumber);
    }
    
    public Flight getFlight(String flightNumber) { return flights.get(flightNumber); }
    public List<Flight> getAllFlights() { return new ArrayList<>(flights.values()); }
    public void addAircraft(Aircraft aircraft) { aircrafts.put(aircraft.getRegistrationNumber(), aircraft); }
    public void deleteAircraft(String regNumber) { aircrafts.remove(regNumber); }
    public Aircraft getAircraft(String regNumber) { return aircrafts.get(regNumber); }
    public List<Aircraft> getAllAircrafts() { return new ArrayList<>(aircrafts.values()); }

    // --- [辅助] 检查飞机未来是否还有任务 ---
    private boolean hasFutureFlights(String regNo) {
        return flights.values().stream()
                .filter(f -> f.getAircraft().getRegistrationNumber().equals(regNo))
                .anyMatch(f -> "Scheduled".equalsIgnoreCase(f.getStatus()) 
                            || "Delayed".equalsIgnoreCase(f.getStatus())
                            || "Boarding".equalsIgnoreCase(f.getStatus()));
    }

    // --- [核心逻辑 1] 互斥锁 ---
    // 修复：只检查当前 "正在进行中" (Departed/In Flight) 的航班。
    // Scheduled 的航班不应该锁死飞机。
    public boolean checkAircraftPhysicalAvailability(String aircraftReg, String currentFlightId) {
        for (Flight f : flights.values()) {
            if (f.getFlightNumber().equals(currentFlightId)) continue; // 排除自己
            
            if (f.getAircraft().getRegistrationNumber().equals(aircraftReg)) {
                String s = f.getStatus();
                // 只有这些状态才表示飞机物理上被占用了
                if ("Departed".equalsIgnoreCase(s) || "In Flight".equalsIgnoreCase(s) || "Boarding".equalsIgnoreCase(s)) {
                    return false; 
                }
            }
        }
        return true; 
    }

    // --- [核心逻辑 2] 多米诺骨牌式排期刷新 ---
    // 当某一班时间变动，自动把后面的航班往后推
    public void refreshScheduleForAircraft(String regNo) {
        // 1. 找出这架飞机的所有航班，按时间排序
        List<Flight> sortedFlights = flights.values().stream()
                .filter(f -> f.getAircraft().getRegistrationNumber().equals(regNo))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());

        // 2. 遍历并处理冲突
        for (int i = 0; i < sortedFlights.size() - 1; i++) {
            Flight current = sortedFlights.get(i);
            Flight next = sortedFlights.get(i+1);

            // 如果 前一班到达时间 > 后一班计划起飞时间
            if (current.getArrivalTime().isAfter(next.getDepartureTime())) {
                
                long diff = Duration.between(next.getDepartureTime(), current.getArrivalTime()).toMinutes();
                
                if (diff > 0) {
                    // 防止重复添加相同的延误原因
                    String reason = "Propagated Delay: Late arrival of " + current.getFlightNumber();
                    boolean exists = next.getDelayReasons().stream().anyMatch(r -> r.startsWith(reason));
                    
                    if (!exists) {
                        next.addPropagatedDelay(reason, diff);
                    } else {
                        // 如果原因已存在，但时间还需要调整，直接改时间不加原因
                        // 这里简化处理：假设每次刷新都是准确的，直接覆盖时间可能有风险，
                        // 但在"规则驱动"模式下，我们只需确保 next 晚于 current 即可。
                        // 简单处理：确保 Next 至少比 Current 晚到达
                        if (next.getDepartureTime().isBefore(current.getArrivalTime())) {
                             long fixDiff = Duration.between(next.getDepartureTime(), current.getArrivalTime()).toMinutes();
                             next.setDepartureTime(next.getDepartureTime().plusMinutes(fixDiff));
                             next.setArrivalTime(next.getArrivalTime().plusMinutes(fixDiff));
                        }
                    }
                }
            }
        }
    }

    // --- 尝试起飞 ---
    public void attemptDeparture(Flight flight) throws Exception {
        // 1. 锁检查
        if (!checkAircraftPhysicalAvailability(flight.getAircraft().getRegistrationNumber(), flight.getFlightNumber())) {
            throw new Exception("Operational Blocked: Aircraft is currently ACTIVE on another flight.");
        }

        // 2. 确保排期是最新的 (处理潜伏的传播延误)
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());

        // 3. 更新状态
        flight.setStatus("Departed");
        flight.getAircraft().setStatus("In Flight");
    }

    // --- 尝试到达 ---
    public void attemptArrival(Flight flight) {
        flight.setStatus("Arrived");
        
        // 更新飞机状态：如果后面还有活，就是 Scheduled；没事了才是 Available
        if (hasFutureFlights(flight.getAircraft().getRegistrationNumber())) {
            flight.getAircraft().setStatus("Scheduled");
        } else {
            flight.getAircraft().setStatus("Available");
        }
        
        // 到达后，可能会影响后面的排期，必须刷新
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());
    }

    // --- 手动延误 ---
    public void manualDelay(Flight flight, String reason) {
        flight.addDelayReason(reason); // 时间 +1 小时
        flight.setStatus("Delayed");
        
        // 关键：一旦我变了，立刻去推后面的航班
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());
    }

    // --- Save / Load (保持不变) ---
    public void saveData() {
        try (PrintWriter aircraftWriter = new PrintWriter(new FileWriter("aircrafts.txt"));
             PrintWriter flightWriter = new PrintWriter(new FileWriter("flights.txt"))) {
            
            for (Aircraft a : aircrafts.values()) {
                aircraftWriter.println(String.format("%s,%s,%s,%d,%s",
                        a.getRegistrationNumber(), a.getBrand(), a.getModel(), a.getCapacity(), a.getStatus()));
            }

            for (Flight f : flights.values()) {
                String isCargo = (f instanceof CargoFlight) ? "YES" : "NO";
                double cargoCap = (f instanceof CargoFlight) ? ((CargoFlight) f).getCargoCapacity() : 0.0;
                String delayString = String.join(";", f.getDelayReasons());
                
                flightWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%d,%s",
                        f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                        f.getDepartureTime().toString(), f.getArrivalTime().toString(),
                        f.getStatus(), f.getAircraft().getRegistrationNumber(),
                        isCargo, cargoCap, f.getBookedPassengers(), delayString));
            }
        } catch (IOException e) { System.out.println("Error saving: " + e.getMessage()); }
    }

    public void loadData() {
        File aircraftFile = new File("aircrafts.txt");
        File flightFile = new File("flights.txt");
        if (!aircraftFile.exists() || !flightFile.exists()) return;

        try {
            Scanner sc = new Scanner(aircraftFile);
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length >= 5) addAircraft(new Aircraft(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]));
            }
            sc.close();

            sc = new Scanner(flightFile);
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",", -1);
                if (parts.length >= 10) { 
                    String fNum = parts[0];
                    String org = parts[1];
                    String dest = parts[2];
                    LocalDateTime dep = LocalDateTime.parse(parts[3]);
                    LocalDateTime arr = LocalDateTime.parse(parts[4]);
                    String status = parts[5];
                    String planeReg = parts[6];
                    String isCargo = parts[7];
                    double cargoCap = Double.parseDouble(parts[8]);
                    int pax = 0;
                    try { pax = Integer.parseInt(parts[9]); } catch (Exception e) {}
                    String delayReasonStr = (parts.length >= 11) ? parts[10] : "";
                    
                    Aircraft linkedPlane = getAircraft(planeReg);
                    if (linkedPlane != null) {
                        Flight f = "YES".equals(isCargo) 
                            ? new CargoFlight(fNum, org, dest, dep, arr, linkedPlane, cargoCap) 
                            : new Flight(fNum, org, dest, dep, arr, linkedPlane, pax);
                        
                        f.setStatus(status);
                        if (!delayReasonStr.isEmpty()) {
                            for (String r : delayReasonStr.split(";")) f.getDelayReasons().add(r);
                        }
                        addFlight(f);
                    }
                }
            }
            sc.close();
        } catch (Exception e) { System.out.println("Error loading: " + e.getMessage()); }
    }
    
    public boolean isAircraftAvailable(String regNo, LocalDateTime newDep, LocalDateTime newArr) {
        for (Flight f : flights.values()) {
            if (f.getAircraft().getRegistrationNumber().equals(regNo)) {
                if ("Cancelled".equalsIgnoreCase(f.getStatus())) continue;
                if (f.getDepartureTime().isBefore(newArr) && f.getArrivalTime().isAfter(newDep)) return false;
            }
        }
        return true;
    }
    
    public void updateFlightStatus(String flightNumber, String status) {
        Flight f = flights.get(flightNumber);
        if (f != null) f.setStatus(status);
    }
}