package com.example;

import java.time.LocalDateTime;

public class CargoFlight extends Flight {
    private double cargoCapacity;

    // 构造函数：必须调用父类 Flight 的构造函数 (super)
    public CargoFlight(String flightNumber, String origin, String destination, 
            LocalDateTime departureTime, LocalDateTime arrivalTime, 
            Aircraft aircraft, double cargoCapacity) {

    	// 1. 调用父类构造函数 (必须传 Aircraft，时间必须是 LocalDateTime)
    	super(flightNumber, origin, destination, departureTime, arrivalTime, aircraft);

    	// 2. 初始化自己的属性
    	this.cargoCapacity = cargoCapacity;
    }

    // Getter
    public double getCargoCapacity() {
        return cargoCapacity;
    }

    // Setter
    public void setCargoCapacity(double cargoCapacity) {
        this.cargoCapacity = cargoCapacity;
    }

    // 重写 toString() 方法，体现多态 (Polymorphism)
    // 这样在打印或报告中，货运航班会显示额外的信息
    @Override
    public String toString() {
        return super.toString() + " | [Cargo Capacity: " + cargoCapacity + "kg]";
    }
}