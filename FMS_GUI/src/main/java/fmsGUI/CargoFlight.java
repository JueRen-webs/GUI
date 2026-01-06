package fmsGUI;

import java.time.LocalDateTime;

// Extends Flight to inherit common properties
public class CargoFlight extends Flight {
    private double cargoCapacity; // Cargo-specific property (kg)

    // Constructor
    public CargoFlight(String flightNumber, String origin, String destination, 
                       LocalDateTime departureTime, LocalDateTime arrivalTime, 
                       Aircraft aircraft, double cargoCapacity) {
        // Call parent constructor
    	super(flightNumber, origin, destination, departureTime, arrivalTime, aircraft, 0);
        this.cargoCapacity = cargoCapacity; 
    }

    public double getCargoCapacity() { return cargoCapacity; }
    public void setCargoCapacity(double cargoCapacity) { this.cargoCapacity = cargoCapacity; }

    // Override toString to include cargo details
    @Override
    public String toString() {
        return super.toString() + String.format(" | [Cargo: %.0fkg]", cargoCapacity);
    }
}