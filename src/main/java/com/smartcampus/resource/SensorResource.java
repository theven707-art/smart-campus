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

// Handles all sensor-related endpoints under /api/v1/sensors
// Also acts as the entry point for the sensor readings sub-resource
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore dataStore = DataStore.getInstance();

    // GET /api/v1/sensors - returns all sensors
    // Can optionally filter by type using ?type=Temperature
    @GET
    public List<Sensor> getAllSensors(@QueryParam("type") String type) {
        List<Sensor> allSensors = new ArrayList<>(dataStore.getSensors().values());

        // If no filter is specified, just return everything
        if (type == null || type.trim().isEmpty()) {
            return allSensors;
        }

        // Filter by type (case-insensitive)
        return allSensors.stream()
                .filter(sensor -> type.equalsIgnoreCase(sensor.getType()))
                .collect(Collectors.toList());
    }

    // POST /api/v1/sensors - registers a new sensor
    // The sensor must reference a valid room, otherwise we throw a 422 error
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor) {
        // Basic validation
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

        // Check that the room actually exists before linking the sensor to it
        Room room = dataStore.getRoom(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                    "Cannot register sensor '" + sensor.getId() + "' because the specified room '"
                            + sensor.getRoomId() + "' does not exist. "
                            + "Please create the room first or provide a valid roomId."
            );
        }

        // Make sure we don't have a duplicate sensor ID
        if (dataStore.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A sensor with ID '"
                            + sensor.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Default to ACTIVE if no status was provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Save the sensor and link it to the room
        dataStore.addSensor(sensor);
        room.getSensorIds().add(sensor.getId());

        return Response.created(URI.create("/api/v1/sensors/" + sensor.getId()))
                .entity(sensor)
                .build();
    }

    // GET /api/v1/sensors/{sensorId} - get details of a specific sensor
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

    // Sub-resource locator: delegates /sensors/{sensorId}/readings to SensorReadingResource
    // This keeps things clean - reading logic stays in its own class
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}
