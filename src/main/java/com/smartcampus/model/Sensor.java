package com.smartcampus.model;

// Represents a sensor device installed in a campus room
// Each sensor has a type (like Temperature or CO2), a status, and a current reading
public class Sensor {

    private String id;
    private String type;           // e.g. "Temperature", "CO2", "Occupancy"
    private String status;         // "ACTIVE" or "MAINTENANCE"
    private double currentValue;   // most recent reading value
    private String roomId;         // which room this sensor belongs to

    // Empty constructor needed for JSON deserialization
    public Sensor() {
    }

    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
