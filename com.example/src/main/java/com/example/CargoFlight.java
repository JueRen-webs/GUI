package com.example;

import java.time.LocalDateTime;

// ✅ Inheritance: 继承自 Flight
public class CargoFlight extends Flight {
    private double cargoCapacity;

    public CargoFlight(String flightNumber, String origin, String destination, 
                       LocalDateTime departureTime, LocalDateTime arrivalTime, 
                       Aircraft aircraft, double cargoCapacity) {
        super(flightNumber, origin, destination, departureTime, arrivalTime, aircraft);
        this.cargoCapacity = cargoCapacity;
    }

    public double getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(double cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    // ✅ Polymorphism: 重写 toString
    @Override
    public String toString() {
        return super.toString() + String.format(" | [Cargo: %.0fkg]", cargoCapacity);
    }
}