package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

// Global safety net that catches any unhandled exception
// This prevents the API from ever leaking raw stack traces to the client,
// which would be a security risk (exposing class names, versions, etc.)
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full error on the server side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", exception);

        // Send a generic message to the client - no internal details
        ErrorResponse error = new ErrorResponse(
                Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact the system administrator."
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
