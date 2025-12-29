package com.example;

//done
public class Aircraft {
    private String registrationNumber;
    private String brand;
    private String model;
    private int capacity;
    private String status;

    public Aircraft() {
    }

    public Aircraft(String registrationNumber, String brand, String model,
            int capacity, String status) {
        this.registrationNumber = registrationNumber;
        this.brand = brand;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getCapacity() {
        return capacity;
    }

    public String getStatus() {
        return status;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("%-8s| %-10s | %-10s | Cap: %-4d | Status: %-12s |",
                registrationNumber, brand, model, capacity, status);
    }
}