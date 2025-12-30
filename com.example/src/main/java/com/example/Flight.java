package com.example;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String status; // "Scheduled", "Delayed", etc.
    private Aircraft aircraft;
    private List<String> delayReasons;

    public Flight(String flightNumber, String origin, String destination,
                  LocalDateTime departureTime, LocalDateTime arrivalTime, Aircraft aircraft) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = "Scheduled";
        this.aircraft = aircraft;
        this.delayReasons = new ArrayList<>();
    }

    // Getters & Setters
    public String getFlightNumber() { return flightNumber; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public String getStatus() { return status; }
    public Aircraft getAircraft() { return aircraft; }
    public List<String> getDelayReasons() { return delayReasons; }

    public void setStatus(String status) { this.status = status; }
    public void addDelayReason(String reason) { this.delayReasons.add(reason); }
    
    @Override
    public String toString() {
        return String.format("%s | %s -> %s | %s", flightNumber, origin, destination, status);
    }
}