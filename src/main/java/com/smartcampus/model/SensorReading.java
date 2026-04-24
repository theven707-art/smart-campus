package com.smartcampus.model;

// Represents a single measurement captured by a sensor at a specific time
public class SensorReading {

    private String id;          // unique ID for this reading
    private long timestamp;     // when the reading was taken (epoch milliseconds)
    private double value;       // the actual recorded value

    // Empty constructor needed for JSON deserialization
    public SensorReading() {
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
