package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JAX-RS resource class managing the /api/v1/sensors collection.
 *
 * Provides operations for sensor registration, retrieval with optional
 * type-based filtering, and acts as a sub-resource locator for sensor
 * readings via the /{sensorId}/readings path.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     *
     * Returns a list of all sensors. If the optional 'type' query parameter
     * is provided, filters the results to only include sensors matching
     * that type (case-insensitive comparison).
     *
     * Using @QueryParam for filtering is the preferred RESTful design because:
     * - It keeps the resource path clean and focused on identifying resources
     * - Query parameters are inherently optional and composable
     * - Multiple filters can be combined without creating complex URL structures
     * - It follows the standard convention for filtering collections
     */
    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());

        // If no type filter is specified, return all sensors
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }

        // Filter sensors by the specified type (case-insensitive)
        return allSensors.stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());
    }

    /**
     * POST /api/v1/sensors
     *
     * Registers a new sensor in the system. Before persisting, this method
     * validates that the roomId specified in the request body actually exists.
     * If the room does not exist, a LinkedResourceNotFoundException is thrown,
     * resulting in an HTTP 422 Unprocessable Entity response.
     *
     * The @Consumes annotation restricts this endpoint to only accept
     * application/json payloads. If a client sends data in a different
     * format (e.g., text/plain or application/xml), JAX-RS will automatically
     * reject the request with a 415 Unsupported Media Type response before
     * this method is even invoked.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Validate required fields
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Sensor ID is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room ID (roomId) is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Validate that the referenced room exists (dependency validation)
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor '" + sensor.getId() + "' because the specified room '"
                            + sensor.getRoomId() + "' does not exist. "
                            + "Please create the room first or provide a valid roomId."
            );
        }

        // Check for duplicate sensor ID
        if (dataStore.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A sensor with ID '"
                            + sensor.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Set default status if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Persist the sensor
        dataStore.addSensor(sensor);

        // Link the sensor to the room by adding its ID to the room's sensorIds list
        room.getSensorIds().add(sensor.getId());

        // Return 201 Created with Location header
        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     *
     * Retrieves detailed information about a specific sensor.
     * Returns 404 if the sensor does not exist.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = dataStore.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '"
                            + sensorId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(sensor).build();
    }

    /**
     * Sub-resource locator for sensor readings.
     *
     * This method delegates all requests matching /api/v1/sensors/{sensorId}/readings
     * to a dedicated SensorReadingResource class. This is the Sub-Resource Locator
     * pattern — it keeps each resource class focused on a single responsibility
     * and prevents this class from becoming a monolithic "god controller."
     *
     * The sensorId is passed to the SensorReadingResource constructor so it
     * knows which sensor's readings to manage.
     *
     * Note: No HTTP method annotation (@GET, @POST, etc.) is used here —
     * that's intentional. A sub-resource locator only returns the sub-resource
     * instance; the actual HTTP method handling is done by the sub-resource class.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
