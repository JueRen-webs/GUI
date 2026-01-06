package fmsGUI;

// Represents an Aircraft entity
public class Aircraft {
    // --- Properties ---
    // Private fields for encapsulation
    private String registrationNumber; // e.g., 9M-ABC
    private String brand;              // e.g., Boeing
    private String model;              // e.g., 737
    private int capacity;              // e.g., 180
    private String status;             // e.g., "Available"

    // --- Constructor ---
    public Aircraft(String registrationNumber, String brand, String model, int capacity, String status) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    // --- Getters ---
    public String getRegistrationNumber() { return registrationNumber; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    
    // --- Setters ---
    // Only status updates are allowed for now
    public void setStatus(String status) { 
        this.status = status; 
    }
}