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
    
    // 这些时间会随着 Delay 而变动 (9:00 -> 10:00)
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

    // [手动延误]: 每次调用，时间自动 +1 小时
    public void addDelayReason(String reason) {
        this.delayReasons.add(reason);
        this.departureTime = this.departureTime.plusHours(1); 
        this.arrivalTime = this.arrivalTime.plusHours(1);     
    }
    
    // [系统自动延误]: 用于传播延误，精确到分钟
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