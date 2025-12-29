package com.example;

// 需要确保status不会直接copy而已
// compare estimate arrival time和actual time ，if > appear delayReasons
// incidents 和delay reasons要改
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Flight {
    private String flightNumber;
    private String origin;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String status;
    private Aircraft aircraft;
    private String captainNotes;
    private List<String> delayReasons;
    private List<String> incidents;

    public Flight(String flightNumber, String origin, String destination,
            LocalDateTime departureTime, LocalDateTime arrivalTime, Aircraft aircraft) {
        this.flightNumber = flightNumber;
        this.origin = origin;
        this.destination = destination;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.status = "Scheduled";
        this.aircraft = aircraft;
        this.captainNotes = "";
        this.delayReasons = new ArrayList<>();
        this.incidents = new ArrayList<>();
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public String getStatus() {
        return status;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public String getCaptainNotes() {
        return captainNotes;
    }

    public List<String> getDelayReasons() {
        return delayReasons;
    }

    public List<String> getIncidents() {
        return incidents;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCaptainNotes(String notes) {
        this.captainNotes = notes;
    }

    public void addDelayReason(String reason) {
        this.delayReasons.add(reason);
    }

    public void addIncident(String incident) {
        this.incidents.add(incident);
    }

    public void setDepartureTime(LocalDateTime time) {
        this.departureTime = time;
    }

    public void setArrivalTime(LocalDateTime time) {
        this.arrivalTime = time;
    }

    @Override
    public String toString() {
        // Explanation of the format:
        // %-10s : Reserve 10 characters space, align LEFT
        // %-25s : Reserve 25 characters space, align LEFT (for the route)
        // %-12s : Reserve 12 characters space, align LEFT (for status)

        return String.format("| %-10s | %-30s | %-15s | %-15s | %s",
                flightNumber,
                origin + " -> " + destination, // Combine route into one column
                status,
                aircraft.getModel(),
                departureTime.toString().replace("T", " ") // Remove 'T' from date to look nicer
        );
    }
}