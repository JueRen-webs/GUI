package fmsGUI;

// 只导入用到的 IO 类
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

// 只导入用到的 Time 类
import java.time.LocalDateTime;

// 只导入用到的 Util 类 (注意 Scanner 在 util 包里)
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

    // --- 增删改查 (CRUD) ---
    public void addFlight(Flight flight) { flights.put(flight.getFlightNumber(), flight); }
    
    // 修改后的删除航班方法：自动释放飞机
    public void deleteFlight(String flightNumber) {
        Flight f = flights.get(flightNumber); // 1. 先找到这个航班对象
        if (f != null) {
            // 2. 找到执行这个航班的飞机
            Aircraft linkedAircraft = f.getAircraft();
            
            // 3. 如果飞机存在，把它的状态改回 "Available"
            if (linkedAircraft != null) {
                linkedAircraft.setStatus("Available");
            }
            
            // 4. 最后才从系统里删除航班
            flights.remove(flightNumber);
        }
    }
    public Flight getFlight(String flightNumber) { return flights.get(flightNumber); }
    public List<Flight> getAllFlights() { return new ArrayList<>(flights.values()); }

    public void updateFlightStatus(String flightNumber, String status) {
        Flight f = flights.get(flightNumber);
        if (f != null) {
            // 1. 处理延误时间逻辑 (之前加的)
            if ("Delayed".equalsIgnoreCase(status)) {
                LocalDateTime currentArr = f.getArrivalTime();
                f.setArrivalTime(currentArr.plusHours(1));
            }
            
            // 2. [新增] 自动更新飞机状态逻辑
            // 如果航班到了 (Arrived) 或者取消了 (Cancelled)，飞机就自由了 (Available)
            if ("Arrived".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) {
                f.getAircraft().setStatus("Available");
            } else {
                // 如果状态是 Scheduled, Boarding, Departed, Delayed...
                // 说明飞机还在忙，状态应该是 "Scheduled" (或者你可以叫 "In Use")
                // 这一步是为了防止用户不小心点了 Arrived 后又改回来，飞机却没变回忙碌状态
                f.getAircraft().setStatus("Scheduled");
            }

            // 3. 最后更新航班状态
            f.setStatus(status);
        }
    }

    public void addAircraft(Aircraft aircraft) { aircrafts.put(aircraft.getRegistrationNumber(), aircraft); }
    public void deleteAircraft(String regNumber) { aircrafts.remove(regNumber); }
    public Aircraft getAircraft(String regNumber) { return aircrafts.get(regNumber); }
    public List<Aircraft> getAllAircrafts() { return new ArrayList<>(aircrafts.values()); }

    // --- 冲突检查 ---
    public boolean isAircraftAvailable(String regNo, LocalDateTime newDep, LocalDateTime newArr) {
        for (Flight f : flights.values()) {
            if (f.getAircraft().getRegistrationNumber().equals(regNo)) {
                if ("Cancelled".equalsIgnoreCase(f.getStatus())) continue;
                if (f.getDepartureTime().isBefore(newArr) && f.getArrivalTime().isAfter(newDep)) {
                    return false;
                }
            }
        }
        return true;
    }

    // --- [核心修改]: 保存数据 (包含延误记录 + 乘客数量) ---
    public void saveData() {
        try {
            // 1. 保存飞机
            PrintWriter aircraftWriter = new PrintWriter(new FileWriter("aircrafts.txt"));
            for (Aircraft a : aircrafts.values()) {
                aircraftWriter.println(String.format("%s,%s,%s,%d,%s",
                        a.getRegistrationNumber(), a.getBrand(), a.getModel(), a.getCapacity(), a.getStatus()));
            }
            aircraftWriter.close();

            // 2. 保存航班
            PrintWriter flightWriter = new PrintWriter(new FileWriter("flights.txt"));
            for (Flight f : flights.values()) {
                String isCargo = (f instanceof CargoFlight) ? "YES" : "NO";
                double cargoCap = (f instanceof CargoFlight) ? ((CargoFlight) f).getCargoCapacity() : 0.0;
                
                String delayString = String.join(";", f.getDelayReasons());

                // [修改] 格式: ..., 载货量, 乘客量, 延误原因串
                flightWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%d,%s",
                        f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                        f.getDepartureTime().toString(), f.getArrivalTime().toString(),
                        f.getStatus(), f.getAircraft().getRegistrationNumber(),
                        isCargo, cargoCap, 
                        f.getBookedPassengers(), // <--- 保存乘客数
                        delayString));
            }
            flightWriter.close();
            System.out.println("Data saved successfully.");

        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    // --- [核心修改]: 读取数据 (解析延误记录 + 乘客数量) ---
    public void loadData() {
        File aircraftFile = new File("aircrafts.txt");
        File flightFile = new File("flights.txt");

        if (!aircraftFile.exists() || !flightFile.exists()) return;

        try {
            // 1. 读取飞机
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

            // 2. 读取航班
            sc = new Scanner(flightFile);
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                // [注意]: 使用 -1 参数，防止split忽略末尾的空字符串
                String[] parts = line.split(",", -1); 
                
                // [修改] 检查长度为 10 (因为加了一列)
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
                    
                    // [新增] 读取乘客数 (index 9)
                    int pax = 0;
                    try {
                        pax = Integer.parseInt(parts[9]);
                    } catch (NumberFormatException e) { pax = 0; }

                    // 延误原因变成了 index 10
                    String delayReasonStr = (parts.length >= 11) ? parts[10] : "";

                    Aircraft linkedPlane = getAircraft(planeReg);
                    if (linkedPlane != null) {
                        Flight f;
                        if ("YES".equals(isCargo)) {
                            // Cargo 构造函数内部会自动把 pax 设为 0
                            f = new CargoFlight(fNum, org, dest, dep, arr, linkedPlane, cargoCap);
                        } else {
                            // 普通航班，传入读取到的 pax
                            f = new Flight(fNum, org, dest, dep, arr, linkedPlane, pax);
                        }
                        f.setStatus(status);
                        
                        if (!delayReasonStr.isEmpty()) {
                            for (String reason : delayReasonStr.split(";")) {
                                f.addDelayReason(reason);
                            }
                        }
                        addFlight(f);
                    }
                }
            }
            sc.close();
            System.out.println("Data loaded successfully.");

        } catch (Exception e) {
            System.out.println("Error loading data: " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}