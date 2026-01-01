package fmsGUI;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Serializable {
    private static final long serialVersionUID = 1L; // 推荐加上，防止序列化警告

    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Aircraft aircraft;
    private String status; // "Scheduled", "Active", "Landed", "Cancelled", "Delayed"
    
    // 仅用于客运航班 (CargoFlight 会忽略这个)
    private int bookedPassengers;
    
    // 存储延误原因的列表
    private List<String> delayReasons = new ArrayList<>();

    // --- 构造函数 ---
    public Flight(String flightNumber, String origin, String destination, 
                  LocalDateTime departureTime, LocalDateTime arrivalTime, 
                  Aircraft aircraft, int bookedPassengers) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.aircraft = aircraft;
        this.bookedPassengers = bookedPassengers;
        this.status = "Scheduled"; // 默认为 Scheduled
    }

    // --- 核心修改：延误处理 ---
    public void addDelayReason(String reason) {
        this.delayReasons.add(reason);
        
        // [修改点] 
        // 之前的逻辑：只推迟到达时间 (Arrival + 1hr)
        // 现在的逻辑：迟出发 (Late Departure) -> 起飞和到达都顺延 1 小时
        
        this.departureTime = this.departureTime.plusHours(1); // 起飞晚了1小时
        this.arrivalTime = this.arrivalTime.plusHours(1);     // 自然到达也晚1小时
    }

    // --- Getters & Setters ---
    public String getFlightNumber() { return flightNumber; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }

    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }

    public Aircraft getAircraft() { return aircraft; }
    public void setAircraft(Aircraft aircraft) { this.aircraft = aircraft; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getBookedPassengers() { return bookedPassengers; }
    public void setBookedPassengers(int bookedPassengers) { this.bookedPassengers = bookedPassengers; }

    public List<String> getDelayReasons() { return delayReasons; }
    
    // 简单的显示用字符串
    @Override
    public String toString() {
        return flightNumber + " (" + origin + " -> " + destination + ")";
    }
}