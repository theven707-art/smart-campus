package com.smartcampus.exception;

// Thrown when trying to post a reading to a sensor that's in MAINTENANCE mode
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
