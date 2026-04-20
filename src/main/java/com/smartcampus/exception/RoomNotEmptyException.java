package com.smartcampus.exception;

/**
 * Thrown when a client attempts to delete a Room that still has
 * active sensors assigned to it. This protects against creating
 * orphaned sensor records with no parent room reference.
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
