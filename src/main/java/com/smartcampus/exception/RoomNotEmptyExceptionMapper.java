package com.smartcampus.exception;

import com.smartcampus.dto.ErrorResponse;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Catches RoomNotEmptyException and sends back a 409 Conflict response
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        ErrorResponse error = new ErrorResponse(
                Response.Status.CONFLICT.getStatusCode(),
                "Conflict",
                exception.getMessage()
        );

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
