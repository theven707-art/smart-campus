package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Sub-resource class for managing sensor readings.
 *
 * This class is NOT annotated with @Path at the class level because it
 * is instantiated and returned by the SensorResource sub-resource locator.
 * The path context (/api/v1/sensors/{sensorId}/readings) is established
 * by the parent resource.
 *
 * This pattern provides several architectural benefits:
 * - Separation of concerns: reading logic is isolated from sensor CRUD logic
 * - Reusability: this class could be mounted under different parent resources
 * - Maintainability: smaller, focused classes are easier to test and modify
 * - Scalability: new sub-resources can be added without bloating the parent
 */
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Constructor receives the sensorId from the parent sub-resource locator.
     * This establishes the context for all reading operations in this instance.
     */
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     *
     * Retrieves the full historical log of readings for the specified sensor.
     * Returns 404 if the parent sensor does not exist.
     */
    @GET
    public Response getAllReadings() {
        // Verify the parent sensor exists
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '"
                            + sensorId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        List<SensorReading> readings = dataStore.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     *
     * Appends a new reading to the specified sensor's history.
     *
     * Business rules:
     * 1. The parent sensor must exist (404 if not found)
     * 2. The sensor must NOT be in "MAINTENANCE" status (403 if it is)
     * 3. A UUID is auto-generated for the reading if not provided
     * 4. The current timestamp is set if not provided
     *
     * Side Effect: A successful POST updates the parent sensor's
     * currentValue field to match the new reading's value. This ensures
     * consistency across the API — querying a sensor always shows
     * the most recent measurement.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Verify the parent sensor exists
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '"
                            + sensorId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // State constraint: maintenance sensors cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot "
                            + "accept new readings. The sensor hardware is physically disconnected. "
                            + "Please wait until the sensor is restored to ACTIVE status."
            );
        }

        // Auto-generate reading ID and timestamp if not provided
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist the reading
        dataStore.addReading(sensorId, reading);

        // Side effect: update the parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        // Return 201 Created
        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }
}
