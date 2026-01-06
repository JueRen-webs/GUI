package fmsGUI;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight implements Serializable {
    private static final long serialVersionUID = 1L;

    private String flightNumber;
    private String origin;
    private String destination;
    
    // Timestamps (subject to change due to delays)
    private LocalDateTime departureTime; 
    private LocalDateTime arrivalTime;   
    
    private Aircraft aircraft;
    private String status; 
    private int bookedPassengers;
    
    private List<String> delayReasons = new ArrayList<>();

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
        this.status = "Scheduled"; 
    }

    // Manual delay: adds 1 hour
    public void addDelayReason(String reason) {
        this.delayReasons.add(reason);
        this.departureTime = this.departureTime.plusHours(1); 
        this.arrivalTime = this.arrivalTime.plusHours(1);     
    }
    
    // System propagation delay: adds specific minutes
    public void addPropagatedDelay(String reason, long minutes) {
        this.delayReasons.add(reason);
        this.departureTime = this.departureTime.plusMinutes(minutes);
        this.arrivalTime = this.arrivalTime.plusMinutes(minutes);
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
    
    @Override
    public String toString() { return flightNumber + " (" + origin + " -> " + destination + ")"; }
}