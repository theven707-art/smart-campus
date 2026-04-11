package com.smartcampus.model;

/**
 * Represents a single measurement reading captured by a sensor at a specific
 * point in time. Each reading has a unique UUID-based ID, a timestamp in
 * epoch milliseconds, and the actual value recorded by the sensor hardware.
 */
public class SensorReading {

    private String id;          // Unique reading event ID (UUID recommended)
    private long timestamp;     // Epoch time (ms) when the reading was captured
    private double value;       // The actual metric value recorded by the hardware

    // Default no-arg constructor (required for JSON deserialization)
    public SensorReading() {
    }

    // Parameterized constructor for convenience
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
