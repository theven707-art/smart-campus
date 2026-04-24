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

// Sub-resource for managing sensor readings
// This class is created by SensorResource's sub-resource locator, not directly by JAX-RS
// It handles everything under /api/v1/sensors/{sensorId}/readings
@Produces(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore dataStore = DataStore.getInstance();

    // The sensor ID is passed in from the parent resource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET /api/v1/sensors/{sensorId}/readings - returns all historical readings for this sensor
    @GET
    public Response getAllReadings() {
        // First check if the sensor exists
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

    // POST /api/v1/sensors/{sensorId}/readings - adds a new reading
    // Won't work if the sensor is in MAINTENANCE mode (returns 403)
    // Also updates the sensor's currentValue as a side effect
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading reading) {
        // Check the sensor exists
        Sensor sensor = dataStore.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Sensor with ID '"
                            + sensorId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Don't allow readings on sensors that are under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor '" + sensorId + "' is currently in MAINTENANCE mode and cannot "
                            + "accept new readings. The sensor hardware is physically disconnected. "
                            + "Please wait until the sensor is restored to ACTIVE status."
            );
        }

        // Auto-generate an ID and timestamp if the client didn't provide them
        if (reading.getId() == null || reading.getId().trim().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Save the reading
        dataStore.addReading(sensorId, reading);

        // Also update the sensor's current value to match this latest reading
        sensor.setCurrentValue(reading.getValue());

        return Response.created(URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId()))
                .entity(reading)
                .build();
    }
}
