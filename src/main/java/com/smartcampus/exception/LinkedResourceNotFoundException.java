package com.smartcampus.exception;

// Thrown when a request body references a resource that doesn't exist
// For example, trying to register a sensor with a roomId that doesn't exist
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
