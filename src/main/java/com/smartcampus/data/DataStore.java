package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton, thread-safe in-memory data store for the Smart Campus API.
 *
 * Uses ConcurrentHashMap to safely handle concurrent read/write operations
 * from multiple request threads without explicit synchronization. This is
 * critical because, by default, JAX-RS creates a new resource class instance
 * per request, so the data store must be shared as a singleton to maintain
 * state across all requests.
 */
public class DataStore {

    // Singleton instance — created once, shared across all resource classes
    private static final DataStore INSTANCE = new DataStore();

    // Core data structures using ConcurrentHashMap for thread safety
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor — prevents external instantiation
    private DataStore() {
        seedData();
    }

    /**
     * Returns the singleton instance of the data store.
     */
    public static DataStore getInstance() {
        return INSTANCE;
    }

    /**
     * Seeds the data store with sample campus data so that the API
     * has meaningful responses immediately upon startup.
     */
    private void seedData() {
        // Create sample rooms
        Room lib301 = new Room("LIB-301", "Library Quiet Study", 40);
        Room eng105 = new Room("ENG-105", "Engineering Lab A", 30);
        Room sci201 = new Room("SCI-201", "Science Lecture Hall", 120);

        rooms.put(lib301.getId(), lib301);
        rooms.put(eng105.getId(), eng105);
        rooms.put(sci201.getId(), sci201);

        // Create sample sensors and link them to rooms
        Sensor temp001 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor co2001 = new Sensor("CO2-001", "CO2", "ACTIVE", 415.0, "LIB-301");
        Sensor occ001 = new Sensor("OCC-001", "Occupancy", "ACTIVE", 18.0, "ENG-105");
        Sensor temp002 = new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "ENG-105");
        Sensor light001 = new Sensor("LIGHT-001", "SmartLighting", "ACTIVE", 75.0, "SCI-201");

        sensors.put(temp001.getId(), temp001);
        sensors.put(co2001.getId(), co2001);
        sensors.put(occ001.getId(), occ001);
        sensors.put(temp002.getId(), temp002);
        sensors.put(light001.getId(), light001);

        // Update room sensor ID lists to reflect the linkages
        lib301.getSensorIds().add("TEMP-001");
        lib301.getSensorIds().add("CO2-001");
        eng105.getSensorIds().add("OCC-001");
        eng105.getSensorIds().add("TEMP-002");
        sci201.getSensorIds().add("LIGHT-001");

        // Create sample readings for TEMP-001
        List<SensorReading> temp001Readings = new ArrayList<>();
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 60000, 21.8));
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 30000, 22.1));
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 22.5));
        readings.put("TEMP-001", temp001Readings);

        // Create sample readings for CO2-001
        List<SensorReading> co2Readings = new ArrayList<>();
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 45000, 410.0));
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 415.0));
        readings.put("CO2-001", co2Readings);
    }

    // ==================== Room Operations ====================

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public Room removeRoom(String id) {
        return rooms.remove(id);
    }

    // ==================== Sensor Operations ====================

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public Sensor removeSensor(String id) {
        return sensors.remove(id);
    }

    // ==================== Reading Operations ====================

    /**
     * Returns the list of readings for a given sensor.
     * Returns an empty list if no readings exist yet.
     */
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    /**
     * Appends a new reading to the specified sensor's history.
     * Automatically creates the readings list if it doesn't exist yet.
     */
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
