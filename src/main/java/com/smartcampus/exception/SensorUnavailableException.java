package com.smartcampus.exception;

/**
 * Thrown when a client attempts to post a new reading to a sensor
 * that is currently in "MAINTENANCE" mode. Sensors in this state
 * are physically disconnected and cannot accept new data.
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}
