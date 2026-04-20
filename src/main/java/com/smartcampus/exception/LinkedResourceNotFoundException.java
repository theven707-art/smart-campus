package com.smartcampus.exception;

/**
 * Thrown when a client submits a valid JSON payload that references
 * a linked resource (e.g., roomId) that does not exist in the system.
 * This is semantically distinct from a 404 because the request URI
 * itself is valid — the problem lies within the request body.
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}
