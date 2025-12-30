package com.example;

import java.time.LocalDateTime; // 导入处理日期时间的工具
import java.util.ArrayList;     // 导入动态数组列表
import java.util.List;          // 导入列表接口

public class Flight {
    // --- 航班的属性 ---
    private String flightNumber;        // 航班号 (如 MH-123)
    private String origin;              // 出发地
    private String destination;         // 目的地
    private LocalDateTime departureTime;// 起飞时间 (包含日期和时间)
    private LocalDateTime arrivalTime;  // 降落时间
    private String status;              // 状态 (如 "Scheduled", "Delayed")
    private Aircraft aircraft;          // 指派的飞机 (这是一个对象引用，指向某个 Aircraft)
    private List<String> delayReasons;  // 延误原因列表 (因为可能延误多次)

    // --- 构造方法 ---
    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, Aircraft aircraft) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = "Scheduled";      // [新手修改]: 默认状态是 "Scheduled" (计划中)，你可以改成别的
        this.aircraft = aircraft;
        this.delayReasons = new ArrayList<>(); // 初始化一个空的列表，准备存延误原因
    }

    // --- Getters (获取数据) ---
    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public String getStatus() { return status; }
    public Aircraft getAircraft() { return aircraft; }
    public List<String> getDelayReasons() { return delayReasons; }

    // --- Setters & Logic (修改与逻辑) ---
    public void setStatus(String status) { this.status = status; }
    
    // 添加一条延误原因
    public void addDelayReason(String reason) { 
        this.delayReasons.add(reason); 
    }
    
    public void setArrivalTime(LocalDateTime newTime) {
        this.arrivalTime = newTime;
    }
}