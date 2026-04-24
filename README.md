# Smart Campus Sensor & Room Management API (JAX-RS)

## Project Overview
This project implements a RESTful web service for a Smart Campus management system using JAX-RS (Jakarta EE). The system is designed to manage Rooms, Sensors, and Sensor Readings through a set of structured REST endpoints. Data persistence is handled using in-memory data structures (ConcurrentHashMap), as per coursework constraints, without the use of any external database.

## Technology Stack
- Java 11
- JAX-RS (Jakarta RESTful Web Services) / Jersey
- Grizzly HTTP Server
- ConcurrentHashMap (in-memory data storage)
- Jackson (for JSON serialization)

## Base URL
http://localhost:8080/api/v1

## How to Run the Project
This project includes a Maven Wrapper, so you do not need Maven installed globally.

1. Open your terminal in the project root directory.
2. Build the project into a single executable JAR file:
   `.\mvnw.cmd clean package -DskipTests`
3. Run the compiled JAR file:
   `java -jar target/smart-campus-api-1.0-SNAPSHOT.jar`
4. The server will start on port 8080.
5. Open Postman or a browser and access the API using: `http://localhost:8080/api/v1/`
6. Press `Enter` in the terminal to stop the server.

---

## API Endpoints

### Discovery Endpoint
`GET http://localhost:8080/api/v1/`
Provides API metadata including version information and available resource links (HATEOAS).

### Room Management
`GET http://localhost:8080/api/v1/rooms`
`POST http://localhost:8080/api/v1/rooms`
`GET http://localhost:8080/api/v1/rooms/LIB-301`
`DELETE http://localhost:8080/api/v1/rooms/LIB-301`

**Example Room Representation:**
`{ "id": "LIB-301", "name": "Library Quiet Study", "capacity": 40 }`

### Sensor Management
`GET http://localhost:8080/api/v1/sensors`
`GET http://localhost:8080/api/v1/sensors?type=Temperature`
`POST http://localhost:8080/api/v1/sensors`
`GET http://localhost:8080/api/v1/sensors/TEMP-001`
`DELETE http://localhost:8080/api/v1/sensors/TEMP-001`

**Example Sensor Representation:**
`{ "id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "roomId": "LIB-301", "currentValue": 22.5 }`

### Sensor Readings (Nested Resource Design)
`GET http://localhost:8080/api/v1/sensors/TEMP-001/readings`
`POST http://localhost:8080/api/v1/sensors/TEMP-001/readings`

**Example Sensor Reading Representation:**
`{ "id": "8b5a743b-4861-46ab-8a4e-123456789abc", "timestamp": 1713898200000, "value": 22.5 }`

---

## Sample CURL Commands

**1. Create Room**
`curl -X POST http://localhost:8080/api/v1/rooms -H "Content-Type: application/json" -d '{"id":"CAFE-101","name":"Student Cafeteria","capacity":200}'`

**2. Retrieve All Rooms**
`curl http://localhost:8080/api/v1/rooms`

**3. Create Sensor**
`curl -X POST http://localhost:8080/api/v1/sensors -H "Content-Type: application/json" -d '{"id":"TEMP-003","type":"Temperature","status":"ACTIVE","roomId":"LIB-301"}'`

**4. Retrieve Sensors with Filtering**
`curl "http://localhost:8080/api/v1/sensors?type=Temperature"`

**5. Add Sensor Reading (Nested Resource Interaction)**
`curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings -H "Content-Type: application/json" -d '{"value":23.1}'`

---

## Exception Handling Strategy
The API implements a structured exception handling mechanism to ensure consistent and meaningful error responses:

- `RoomNotEmptyException` → **HTTP 409 Conflict** (resource constraint violation when deleting a room with active sensors).
- `LinkedResourceNotFoundException` → **HTTP 422 Unprocessable Entity** (invalid logical reference when assigning a sensor to a non-existent room).
- `SensorUnavailableException` → **HTTP 403 Forbidden** (operation not permitted due to sensor being in MAINTENANCE state).
- `GenericExceptionMapper` → **HTTP 500 Internal Server Error** (unexpected runtime failures caught to prevent stack trace leaks).

---

## Logging and Observability
Logging is implemented using a JAX-RS `LoggingFilter` that records all incoming requests and outgoing responses.

The `ContainerRequestFilter` logs the HTTP method and request URI (e.g., `>>> Incoming Request: GET /api/v1/rooms`), while the `ContainerResponseFilter` logs the response status code (e.g., `<<< Outgoing Response: GET /api/v1/rooms -> 200`).

This approach centralizes logging, reduces code duplication, and ensures consistent tracking of all API activity, improving debugging and system monitoring.

---

## Project Structure

```
smart-campus/
│
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── smartcampus/
│
│                   ├── Main.java
│                   ├── SmartCampusApplication.java
│                   │
│                   ├── data/
│                   │   └── DataStore.java
│                   │
│                   ├── dto/
│                   │   └── ErrorResponse.java
│                   │
│                   ├── exception/
│                   │   ├── GenericExceptionMapper.java
│                   │   ├── LinkedResourceNotFoundException.java
│                   │   ├── LinkedResourceNotFoundExceptionMapper.java
│                   │   ├── RoomNotEmptyException.java
│                   │   ├── RoomNotEmptyExceptionMapper.java
│                   │   ├── SensorUnavailableException.java
│                   │   └── SensorUnavailableExceptionMapper.java
│                   │
│                   ├── filter/
│                   │   └── LoggingFilter.java
│                   │
│                   ├── model/
│                   │   ├── Room.java
│                   │   ├── Sensor.java
│                   │   └── SensorReading.java
│                   │
│                   └── resource/
│                       ├── DiscoveryResource.java
│                       ├── RoomResource.java
│                       ├── SensorResource.java
│                       └── SensorReadingResource.java
│
├── .mvn/
├── pom.xml
├── mvnw.cmd
├── coursework_report.md
└── README.md
```

---

## Answers to Coursework Questions

**Question 1**
In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures(maps/lists) to prevent data loss or race conditions.

**Answer**
In this project, JAX-RS resource classes such as `RoomResource` and `SensorResource` are request-scoped by default. This means a new instance is created for each incoming HTTP request rather than being treated as a singleton. Because of this, instance variables inside resource classes are not shared between requests.

To maintain shared application state, the project uses a centralized `DataStore` singleton with thread-safe `ConcurrentHashMap` structures for rooms, sensors, and readings. This ensures that all requests access the same data safely.

Since multiple requests may access or modify these maps at the same time, `ConcurrentHashMap` is used to provide thread safety and prevent race conditions or silent data loss. This design ensures safe concurrent access in a multi-request environment.

---

**Question 2**
Why is the provision of “Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

**Answer**
In the `DiscoveryResource` class, the API returns links to available resources such as `/rooms` and `/sensors`. This follows the HATEOAS (Hypermedia As The Engine Of Application State) principle, where responses include navigational information.

This approach allows clients to discover available endpoints dynamically without needing external documentation. It improves flexibility because if endpoint paths change, clients can still rely on the provided links. This reduces coupling between client and server and makes the API much easier to evolve seamlessly.

---

**Question 3**
When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

**Answer**
In `RoomResource`, the `GET /rooms` endpoint returns full `Room` objects from `DataStore`.

Returning full objects provides complete information in a single request, reducing the number of API calls required. However, it increases response size and consumes more bandwidth.

If only IDs were returned, it would result in a very small initial payload, saving network bandwidth. However, this creates an "N+1 problem" for the client, which would need to make numerous subsequent HTTP requests to fetch full room details. Therefore, returning full objects improves usability and reduces HTTP handshake overhead, while returning IDs is better for strict bandwidth preservation.

---

**Question 4**
Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

**Answer**
Yes, the DELETE operation in `RoomResource` is strictly idempotent.

When `DELETE /rooms/{id}` is called the first time, the room is removed from the `DataStore`, and the server returns a `204 No Content`. If the exact same request is sent again, the room no longer exists, and the system responds with a `404 Not Found`.

Crucially, the server's internal state does not change after the first request. Whether the client sends one request or fifty, the final state of the campus infrastructure remains identical: the room is absent. This confirms the operation is idempotent.

---

**Question 5**
We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

**Answer**
In `SensorResource` and `RoomResource`, POST methods are annotated with `@Consumes(MediaType.APPLICATION_JSON)`. This means the API only accepts JSON input.

If a client sends data in `text/plain` or `application/xml` format, JAX-RS will intercept the request before it reaches the application logic. The framework will automatically reject the request and return an HTTP `415 Unsupported Media Type` status code.

This guarantees payload contracts and relieves developers from having to write boilerplate code to manually parse, validate, or gracefully fail on unrecognized data formats.

---

**Question 6**
You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

**Answer**
In `SensorResource`, filtering is implemented using `@QueryParam("type")` in `GET /sensors`.

This approach is highly flexible because query parameters are inherently optional, composable, and order-independent. Multiple filters can be added in the future (e.g., `?type=CO2&status=ACTIVE`) without modifying the URL design.

Using path variables like `/sensors/type/CO2` is less flexible and is better suited for fixed resource identification. Attempting to filter by multiple criteria using path parameters would lead to complex, convoluted, and brittle routing structures.

---

**Question 7**
Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

**Answer**
In this project, `SensorResource` uses a sub-resource locator method: `/sensors/{sensorId}/readings` → `SensorReadingResource`.

This design prevents resource classes from swelling into monolithic "god controllers." It enforces the Single Responsibility Principle by delegating reading-related logic strictly to the `SensorReadingResource`, while the parent handles only sensor-level operations.

This reduces complexity, improves code readability, and makes the system easier to test and maintain. It also enhances code reusability across the API.

---

**Question 8**
Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

**Answer**
In `SensorResource`, when a sensor references a non-existent `roomId` in its JSON body, a `LinkedResourceNotFoundException` is thrown and mapped to HTTP `422 Unprocessable Entity`.

HTTP 404 means the target URL itself is not found, which could mislead a client developer into thinking they hit the wrong endpoint. 

HTTP 422 accurately communicates that the server understands the content type and the JSON syntax is correct, but it was unable to process the instructions due to a semantic failure (the missing logical reference).

---

**Question 9**
From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

**Answer**
The project avoids exposing raw stack traces using a global `GenericExceptionMapper`, which returns a safe, controlled `500 Internal Server Error` response.

Exposing stack traces is an Information Disclosure vulnerability. When an attacker views a raw trace, they can see specific framework versions (allowing them to look up known CVEs), internal package names, class structures, and potentially sensitive file paths or configurations. 

This gives attackers a blueprint of the server's internal environment, making it easier to craft targeted logic-flow exploits.

---

**Question 10**
Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

**Answer**
The project uses a `LoggingFilter` implementing `ContainerRequestFilter` and `ContainerResponseFilter` to capture incoming and outgoing HTTP traffic.

This centralizes logging for all requests and responses globally. It guarantees consistency, as there is no risk of a developer forgetting to add logging statements to a new endpoint. 

Furthermore, resource classes remain clean and focused entirely on core business logic. If the logging format needs to be updated, the change only needs to be made in one centralized file rather than in dozens of separate methods across the codebase.
