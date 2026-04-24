package com.smartcampus.data;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Singleton data store that holds all our rooms, sensors, and readings in memory
// We use ConcurrentHashMap because multiple API requests can hit this at the same time
public class DataStore {

    // Only one instance of this class exists throughout the application
    private static final DataStore INSTANCE = new DataStore();

    // Using ConcurrentHashMap instead of regular HashMap for thread safety
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Private constructor so nobody can create another instance
    private DataStore() {
        seedData();
    }

    // Returns the single shared instance
    public static DataStore getInstance() {
        return INSTANCE;
    }

    // Populates some sample data so the API has something to show right away
    private void seedData() {
        // Setting up some rooms
        Room lib301 = new Room("LIB-301", "Library Quiet Study", 40);
        Room eng105 = new Room("ENG-105", "Engineering Lab A", 30);
        Room sci201 = new Room("SCI-201", "Science Lecture Hall", 120);

        rooms.put(lib301.getId(), lib301);
        rooms.put(eng105.getId(), eng105);
        rooms.put(sci201.getId(), sci201);

        // Setting up sensors and linking them to rooms
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

        // Linking each sensor ID to its room
        lib301.getSensorIds().add("TEMP-001");
        lib301.getSensorIds().add("CO2-001");
        eng105.getSensorIds().add("OCC-001");
        eng105.getSensorIds().add("TEMP-002");
        sci201.getSensorIds().add("LIGHT-001");

        // Adding some sample reading history for TEMP-001
        List<SensorReading> temp001Readings = new ArrayList<>();
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 60000, 21.8));
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 30000, 22.1));
        temp001Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 22.5));
        readings.put("TEMP-001", temp001Readings);

        // Adding some sample reading history for CO2-001
        List<SensorReading> co2Readings = new ArrayList<>();
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis() - 45000, 410.0));
        co2Readings.add(new SensorReading(UUID.randomUUID().toString(), System.currentTimeMillis(), 415.0));
        readings.put("CO2-001", co2Readings);
    }

    // --- Room operations ---

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

    // --- Sensor operations ---

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

    // --- Reading operations ---

    // Gets all readings for a sensor, returns empty list if none exist
    public List<SensorReading> getReadings(String sensorId) {
        return readings.getOrDefault(sensorId, new ArrayList<>());
    }

    // Adds a new reading and creates the list if this is the first reading for that sensor
    public void addReading(String sensorId, SensorReading reading) {
        readings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }
}
