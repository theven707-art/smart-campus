package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

// Root endpoint of the API - provides metadata and links to available resources
// This follows the HATEOAS principle so clients can discover endpoints dynamically
@Path("/")
public class DiscoveryResource {

    // Returns API info and links to the rooms and sensors collections
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getApiInfo() {
        Map<String, Object> apiInfo = new LinkedHashMap<>();
        apiInfo.put("title", "Smart Campus Sensor & Room Management API");
        apiInfo.put("version", "1.0");
        apiInfo.put("description", "RESTful service for managing campus rooms, sensors, and readings");
        apiInfo.put("contact", "admin@smartcampus.westminster.ac.uk");

        // These links let clients navigate to the available resource collections
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        apiInfo.put("resources", resources);

        return apiInfo;
    }
}
