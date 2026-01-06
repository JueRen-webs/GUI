package fmsGUI;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class FlightManagementSystem {

    private Map<String, Flight> flights;
    private Map<String, Aircraft> aircrafts;

    public FlightManagementSystem() {
        this.flights = new HashMap<>();
        this.aircrafts = new HashMap<>();
    }

    // --- Basic CRUD Operations ---
    public void addFlight(Flight flight) { 
        flights.put(flight.getFlightNumber(), flight); 
        
        // Logic fix: Only set status to "Scheduled" if currently "Available"
        // Prevents overwriting "In Flight" or active states
        if ("Available".equalsIgnoreCase(flight.getAircraft().getStatus())) {
            flight.getAircraft().setStatus("Scheduled"); 
        }
    }
    
    public void deleteFlight(String flightNumber) {
        Flight f = flights.get(flightNumber);
        if (f != null && f.getAircraft() != null) {
            // Set aircraft to Available if no future tasks remain
            if (!hasFutureFlights(f.getAircraft().getRegistrationNumber())) {
                f.getAircraft().setStatus("Available");
            }
        }
        flights.remove(flightNumber);
    }
    
    public Flight getFlight(String flightNumber) { return flights.get(flightNumber); }
    public List<Flight> getAllFlights() { return new ArrayList<>(flights.values()); }
    public void addAircraft(Aircraft aircraft) { aircrafts.put(aircraft.getRegistrationNumber(), aircraft); }
    public void deleteAircraft(String regNumber) { aircrafts.remove(regNumber); }
    public Aircraft getAircraft(String regNumber) { return aircrafts.get(regNumber); }
    public List<Aircraft> getAllAircrafts() { return new ArrayList<>(aircrafts.values()); }

    // --- Helper: Check for future flights ---
    private boolean hasFutureFlights(String regNo) {
        return flights.values().stream()
                .filter(f -> f.getAircraft().getRegistrationNumber().equals(regNo))
                .anyMatch(f -> "Scheduled".equalsIgnoreCase(f.getStatus()) 
                            || "Delayed".equalsIgnoreCase(f.getStatus())
                            || "Boarding".equalsIgnoreCase(f.getStatus()));
    }

    // --- Core Logic 1: Mutex / Availability Check ---
    // Checks if the aircraft is physically occupied by another active flight
    public boolean checkAircraftPhysicalAvailability(String aircraftReg, String currentFlightId) {
        for (Flight f : flights.values()) {
            if (f.getFlightNumber().equals(currentFlightId)) continue; 
            
            if (f.getAircraft().getRegistrationNumber().equals(aircraftReg)) {
                String s = f.getStatus();
                // Block if active
                if ("Departed".equalsIgnoreCase(s) || "In Flight".equalsIgnoreCase(s) || "Boarding".equalsIgnoreCase(s)) {
                    return false; 
                }
            }
        }
        return true; 
    }

    // --- Core Logic 2: Cascade Schedule Updates (Domino Effect) ---
    // Propagate delays to subsequent flights if timing overlaps
    public void refreshScheduleForAircraft(String regNo) {
        // 1. Get all flights for aircraft, sorted by time
        List<Flight> sortedFlights = flights.values().stream()
                .filter(f -> f.getAircraft().getRegistrationNumber().equals(regNo))
                .sorted(Comparator.comparing(Flight::getDepartureTime))
                .collect(Collectors.toList());

        // 2. Check for conflicts and propagate delays
        for (int i = 0; i < sortedFlights.size() - 1; i++) {
            Flight current = sortedFlights.get(i);
            Flight next = sortedFlights.get(i+1);

            // If Arrival Time > Next Departure Time
            if (current.getArrivalTime().isAfter(next.getDepartureTime())) {
                
                long diff = Duration.between(next.getDepartureTime(), current.getArrivalTime()).toMinutes();
                
                if (diff > 0) {
                    // Avoid duplicate delay reasons
                    String reason = "Propagated Delay: Late arrival of " + current.getFlightNumber();
                    boolean exists = next.getDelayReasons().stream().anyMatch(r -> r.startsWith(reason));
                    
                    if (!exists) {
                        next.addPropagatedDelay(reason, diff);
                    } else {
                        // Adjust time only if needed
                        if (next.getDepartureTime().isBefore(current.getArrivalTime())) {
                             long fixDiff = Duration.between(next.getDepartureTime(), current.getArrivalTime()).toMinutes();
                             next.setDepartureTime(next.getDepartureTime().plusMinutes(fixDiff));
                             next.setArrivalTime(next.getArrivalTime().plusMinutes(fixDiff));
                        }
                    }
                }
            }
        }
    }

    // --- Attempt Departure ---
    public void attemptDeparture(Flight flight) throws Exception {
        // 1. Check availability
        if (!checkAircraftPhysicalAvailability(flight.getAircraft().getRegistrationNumber(), flight.getFlightNumber())) {
            throw new Exception("Operational Blocked: Aircraft is currently ACTIVE on another flight.");
        }

        // 2. Refresh schedule to handle latent delays
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());

        // 3. Update status
        flight.setStatus("Departed");
        autoUpdateAircraftStatus(flight.getAircraft().getRegistrationNumber());
    }

    public void autoUpdateAircraftStatus(String regNo) {
        Aircraft a = aircrafts.get(regNo);
        if (a == null) return;

     // Check if there are any incomplete flights linked to this aircraft
        // This covers all active states: Scheduled, Boarding, Departed, In Flight, Delayed
        boolean isBusy = flights.values().stream()
                .filter(f -> f.getAircraft().getRegistrationNumber().equals(regNo))
                .anyMatch(f -> {
                    String s = f.getStatus();
                    // If the flight is NOT 'Arrived' and NOT 'Cancelled', it means it is still active.
                    return !"Arrived".equalsIgnoreCase(s) && !"Cancelled".equalsIgnoreCase(s);
                });

        if (isBusy) {
            a.setStatus("Scheduled");
        } else {
            a.setStatus("Available");
        }
    }

    public void attemptArrival(Flight flight) {
        flight.setStatus("Arrived");
        
        autoUpdateAircraftStatus(flight.getAircraft().getRegistrationNumber());
        
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());
    }
    // --- Manual Delay ---
    public void manualDelay(Flight flight, String reason) {
        flight.addDelayReason(reason); // Adds 1 hour
        flight.setStatus("Delayed");
        
        // Critical: Refresh subsequent flights immediately
        refreshScheduleForAircraft(flight.getAircraft().getRegistrationNumber());
    }

    // --- Save / Load ---
    public void saveData() {
        try (PrintWriter aircraftWriter = new PrintWriter(new FileWriter("aircrafts.txt"));
             PrintWriter flightWriter = new PrintWriter(new FileWriter("flights.txt"))) {
            
            for (Aircraft a : aircrafts.values()) {
                aircraftWriter.println(String.format("%s,%s,%s,%d,%s",
                        a.getRegistrationNumber(), a.getBrand(), a.getModel(), a.getCapacity(), a.getStatus()));
            }

            for (Flight f : flights.values()) {
                String isCargo = (f instanceof CargoFlight) ? "YES" : "NO";
                double cargoCap = (f instanceof CargoFlight) ? ((CargoFlight) f).getCargoCapacity() : 0.0;
                String delayString = String.join(";", f.getDelayReasons());
                
                flightWriter.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%d,%s",
                        f.getFlightNumber(), f.getOrigin(), f.getDestination(),
                        f.getDepartureTime().toString(), f.getArrivalTime().toString(),
                        f.getStatus(), f.getAircraft().getRegistrationNumber(),
                        isCargo, cargoCap, f.getBookedPassengers(), delayString));
            }
        } catch (IOException e) { System.out.println("Error saving: " + e.getMessage()); }
    }

    public void loadData() {
        File aircraftFile = new File("aircrafts.txt");
        File flightFile = new File("flights.txt");
        if (!aircraftFile.exists() || !flightFile.exists()) return;

        try {
            Scanner sc = new Scanner(aircraftFile);
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",");
                if (parts.length >= 5) addAircraft(new Aircraft(parts[0], parts[1], parts[2], Integer.parseInt(parts[3]), parts[4]));
            }
            sc.close();

            sc = new Scanner(flightFile);
            while (sc.hasNextLine()) {
                String[] parts = sc.nextLine().split(",", -1);
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
                    int pax = 0;
                    try { pax = Integer.parseInt(parts[9]); } catch (Exception e) {}
                    String delayReasonStr = (parts.length >= 11) ? parts[10] : "";
                    
                    Aircraft linkedPlane = getAircraft(planeReg);
                    if (linkedPlane != null) {
                        Flight f = "YES".equals(isCargo) 
                            ? new CargoFlight(fNum, org, dest, dep, arr, linkedPlane, cargoCap) 
                            : new Flight(fNum, org, dest, dep, arr, linkedPlane, pax);
                        
                        f.setStatus(status);
                        if (!delayReasonStr.isEmpty()) {
                            for (String r : delayReasonStr.split(";")) f.getDelayReasons().add(r);
                        }
                        addFlight(f);
                    }
                }
            }
            sc.close();
            
            for (String reg : aircrafts.keySet()) {
                autoUpdateAircraftStatus(reg);
            }

        } catch (Exception e) { System.out.println("Error loading: " + e.getMessage()); }
    }
    
    public boolean isAircraftAvailable(String regNo, LocalDateTime newDep, LocalDateTime newArr) {
        for (Flight f : flights.values()) {
            if (f.getAircraft().getRegistrationNumber().equals(regNo)) {
                if ("Cancelled".equalsIgnoreCase(f.getStatus())) continue;
                if (f.getDepartureTime().isBefore(newArr) && f.getArrivalTime().isAfter(newDep)) return false;
            }
        }
        return true;
    }
    
    public void updateFlightStatus(String flightNumber, String status) {
        Flight f = flights.get(flightNumber);
        if (f != null) f.setStatus(status);
    }
}