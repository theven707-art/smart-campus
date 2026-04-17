package com.smartcampus.resource;

import com.smartcampus.data.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * JAX-RS resource class managing the /api/v1/rooms collection.
 *
 * Provides CRUD operations for Room entities on the Smart Campus.
 * Includes safety logic to prevent deletion of rooms that still
 * have active sensors assigned, protecting data integrity.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * GET /api/v1/rooms
     *
     * Returns a comprehensive list of all rooms currently registered
     * in the Smart Campus system. Returns the full room objects (not
     * just IDs) to minimize the number of round-trips a client needs
     * to make — trading slightly higher bandwidth for significantly
     * fewer HTTP requests.
     */
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRooms().values());
    }

    /**
     * POST /api/v1/rooms
     *
     * Creates a new room in the system. The client provides the room
     * details in the JSON request body. On success, returns HTTP 201
     * Created with the created room entity and a Location header
     * pointing to the new resource URI.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        // Validate required fields
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room ID is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Check for duplicate room ID
        if (dataStore.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A room with ID '"
                            + room.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Initialize sensor list if the client didn't provide one
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        dataStore.addRoom(room);

        // Return 201 Created with the Location header and the room entity
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     *
     * Retrieves detailed metadata for a specific room identified by
     * its unique ID. Returns 404 if the room does not exist.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room with ID '"
                            + roomId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     *
     * Decommissions a room from the system. Before deletion, this method
     * checks whether any sensors are still assigned to the room. If sensors
     * exist, the deletion is blocked and a RoomNotEmptyException is thrown,
     * which the corresponding ExceptionMapper converts to a 409 Conflict.
     *
     * Idempotency: If the room has already been deleted (i.e., it doesn't
     * exist), the endpoint returns 404. Strictly speaking, a truly idempotent
     * DELETE would return 204 even for an already-deleted resource. However,
     * returning 404 is a common and practical choice as it clearly communicates
     * the current state of the system to the client.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = dataStore.getRoom(roomId);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\": \"Not Found\", \"message\": \"Room with ID '"
                            + roomId + "' was not found.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Business logic constraint: block deletion if sensors are still assigned
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "' because it still has "
                            + room.getSensorIds().size() + " sensor(s) assigned: "
                            + room.getSensorIds()
                            + ". Please reassign or remove all sensors before deleting this room."
            );
        }

        dataStore.removeRoom(roomId);

        // 204 No Content — successful deletion with no response body
        return Response.noContent().build();
    }
}
