package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * JAX-RS filter that provides API observability by logging every
 * incoming request and outgoing response.
 *
 * Implementing both ContainerRequestFilter and ContainerResponseFilter
 * in a single class provides a unified view of the request/response
 * lifecycle. Using JAX-RS filters for cross-cutting concerns like logging
 * is far superior to manually inserting Logger.info() calls in every
 * resource method because:
 *
 * - It follows the Single Responsibility Principle (resources focus on
 *   business logic, filters handle infrastructure concerns)
 * - It's applied globally and consistently — no risk of forgetting to
 *   add logging to a new endpoint
 * - It can be enabled/disabled or reconfigured in one place
 * - It's the standard approach for Aspect-Oriented Programming (AOP)
 *   in JAX-RS, similar to servlet filters or Spring interceptors
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Invoked for every incoming HTTP request before it reaches the
     * resource method. Logs the HTTP method and the requested URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info(">>> Incoming Request: " + method + " " + uri);
    }

    /**
     * Invoked for every outgoing HTTP response after the resource method
     * has completed. Logs the final HTTP status code returned to the client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        int status = responseContext.getStatus();
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info("<<< Outgoing Response: " + method + " " + uri + " -> " + status);
    }
}
