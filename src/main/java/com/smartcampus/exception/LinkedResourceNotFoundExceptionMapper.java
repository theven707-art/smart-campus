package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Maps LinkedResourceNotFoundException to an HTTP 422 Unprocessable Entity response.
 *
 * HTTP 422 is used here instead of 404 because the request URI is valid —
 * the problem is that a field within the JSON body (e.g., roomId) references
 * a resource that does not exist. The server understands the request format
 * but cannot process it due to a semantic validation failure.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
                422,    // 422 Unprocessable Entity
                "Unprocessable Entity",
                exception.getMessage()
        );

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
