package com.smartcampus.dto;

// A standard error response structure that all our exception mappers use
// This way every error from the API looks the same, making it easier for clients to handle
public class ErrorResponse {

    private int status;       // the HTTP status code like 409, 422, 500
    private String error;     // short label like "Conflict" or "Forbidden"
    private String message;   // detailed explanation of what went wrong

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
