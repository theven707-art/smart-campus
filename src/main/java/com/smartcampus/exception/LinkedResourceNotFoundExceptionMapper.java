package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Catches LinkedResourceNotFoundException and sends back a 422 Unprocessable Entity
// We use 422 instead of 404 because the URL itself is fine - the problem is in the request body
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
                422,
                "Unprocessable Entity",
                exception.getMessage()
        );

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
