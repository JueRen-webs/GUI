package com.example;

public class Aircraft {
    private String registrationNumber;
    private String brand;
    private String model;
    private int capacity;
    private String status; // "Available", "Scheduled", "Maintenance"

    public Aircraft(String registrationNumber, String brand, String model, int capacity, String status) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getCapacity() { return capacity; }
    public String getStatus() { return status; }
    
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return registrationNumber + " (" + model + ")";
    }
}