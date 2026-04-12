package com.smartcampus.dto;

/**
 * Standardized JSON error response body used by all exception mappers.
 *
 * Every error returned by the API follows this consistent structure,
 * making it easy for client developers to parse and handle errors
 * programmatically without guessing the response format.
 */
public class ErrorResponse {

    private int status;         // HTTP status code (e.g., 409, 422, 500)
    private String error;       // Short error label (e.g., "Conflict", "Unprocessable Entity")
    private String message;     // Detailed human-readable explanation

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    // Getters and Setters

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
