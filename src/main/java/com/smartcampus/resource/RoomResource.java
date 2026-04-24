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

// Handles all the room-related API endpoints under /api/v1/rooms
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore dataStore = DataStore.getInstance();

    // GET /api/v1/rooms - returns all rooms as a list
    // We return full objects instead of just IDs to save the client from making extra requests
    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(dataStore.getRooms().values());
    }

    // POST /api/v1/rooms - creates a new room
    // Expects a JSON body with id, name, and capacity
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        // Make sure the room has an ID
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Bad Request\", \"message\": \"Room ID is required.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Don't allow duplicate room IDs
        if (dataStore.getRoom(room.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\": \"Conflict\", \"message\": \"A room with ID '"
                            + room.getId() + "' already exists.\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        // Make sure the sensor list is initialized even if the client didn't send one
        if (room.getSensorIds() == null) {
            room.setSensorIds(new ArrayList<>());
        }

        dataStore.addRoom(room);

        // Return 201 with a Location header pointing to the new room
        return Response.created(URI.create("/api/v1/rooms/" + room.getId()))
                .entity(room)
                .build();
    }

    // GET /api/v1/rooms/{roomId} - get a specific room by its ID
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

    // DELETE /api/v1/rooms/{roomId} - removes a room from the system
    // But only if no sensors are still assigned to it
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

        // Can't delete a room if it still has sensors inside it
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                    "Cannot delete room '" + roomId + "' because it still has "
                            + room.getSensorIds().size() + " sensor(s) assigned: "
                            + room.getSensorIds()
                            + ". Please reassign or remove all sensors before deleting this room."
            );
        }

        dataStore.removeRoom(roomId);

        // 204 means it was deleted successfully, no body needed
        return Response.noContent().build();
    }
}
