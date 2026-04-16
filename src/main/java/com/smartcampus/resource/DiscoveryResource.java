package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root discovery endpoint for the Smart Campus API.
 *
 * This endpoint acts as the entry point for the API, providing clients
 * with essential metadata including version information, administrative
 * contact details, and a navigable map of available resource collections.
 *
 * This design follows the HATEOAS (Hypermedia as the Engine of Application
 * State) principle — clients can discover all available resources dynamically
 * rather than relying on out-of-band documentation.
 */
@Path("/")
public class DiscoveryResource {

    /**
     * GET /api/v1
     *
     * Returns API metadata including version, contact, and a resource
     * directory that allows client applications to discover available
     * endpoints at runtime.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getApiInfo() {
        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("title", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0");
        apiInfo.put("description", "RESTful service for managing campus rooms, sensors, and readings");
        apiInfo.put("contact", "admin@smartcampus.westminster.ac.uk");

        // Resource directory — allows clients to navigate the API dynamically
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", resources);

        return apiInfo;
    }
}
