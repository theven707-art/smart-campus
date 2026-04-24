package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Catches SensorUnavailableException and returns a 403 Forbidden response
@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        ErrorResponse error = new ErrorResponse(
                Response.Status.FORBIDDEN.getStatusCode(),
                "Forbidden",
                exception.getMessage()
        );

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
