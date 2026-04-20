package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global "safety net" exception mapper that catches any unhandled Throwable.
 *
 * This ensures the API never leaks raw Java stack traces to external consumers.
 * Exposing internal implementation details (class names, file paths, dependency
 * versions) in stack traces is a significant security risk — an attacker could
 * use this information to identify specific framework versions with known
 * vulnerabilities, map the internal class structure, or discover database
 * connection details embedded in error messages.
 *
 * Instead, this mapper logs the full stack trace server-side for debugging
 * and returns a generic, safe error message to the client.
 */
@Provider
public class GenericExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GenericExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Log the full exception details server-side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception caught by global mapper", exception);

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
