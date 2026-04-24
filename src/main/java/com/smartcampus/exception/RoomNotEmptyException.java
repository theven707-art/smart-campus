package com.smartcampus.exception;

// Thrown when someone tries to delete a room that still has sensors in it
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}
