# Smart Campus API Technical Report

## Part 1: Service Architecture & Setup

### Question 1: JAX-RS Resource Lifecycle and Data Synchronization
By default, the JAX-RS runtime manages resource classes using a **per-request lifecycle**. This means that a new instance of the resource class (e.g., `RoomResource`) is instantiated for every incoming HTTP request and is destroyed immediately after the response is sent. 

Because instances are ephemeral, we cannot use instance variables (non-static class fields) to maintain state across multiple requests. This architectural decision requires that shared, in-memory data structures be managed externally to the resource classes. In this project, this is achieved using a Singleton pattern (`DataStore.getInstance()`).

Because multiple incoming requests (and therefore multiple threads) can invoke the Singleton simultaneously, the underlying data structures must be thread-safe. If standard collections like `HashMap` or `ArrayList` were used, concurrent read/write operations could lead to race conditions, `ConcurrentModificationException`s, or silent data loss. To prevent this, the data store must employ synchronization mechanisms or utilize thread-safe concurrent collections (e.g., `ConcurrentHashMap`, `CopyOnWriteArrayList`).

### Question 2: HATEOAS and Hypermedia
HATEOAS (Hypermedia As The Engine Of Application State) is considered a hallmark of advanced RESTful design (Maturity Level 3 on the Richardson Maturity Model) because it allows an API to be self-documenting and highly adaptable. By providing links to related resources within the API responses, clients can navigate the API dynamically based on the current state.

This approach significantly benefits client developers because it decouples the client from the server's specific URI routing structure. Unlike static documentation—which requires clients to hardcode URLs and breaks when those URLs change—HATEOAS allows the server to change resource locations seamlessly. As long as the client follows the provided hypermedia links (like clicking links on a web page), the client application will continue to function without requiring code updates.

---

## Part 2: Room Management

### Question 1: Returning IDs vs. Full Room Objects
When designing the `GET /api/v1/rooms` endpoint, there is a trade-off between payload size and network latency.

Returning **only IDs** results in a very small initial JSON payload, saving network bandwidth. However, this creates an "N+1 problem" for the client. If the client needs to display a dashboard of rooms, it must make one initial request for the IDs, followed by `N` subsequent HTTP requests to fetch the details for each specific room. This introduces significant latency and overhead due to multiple HTTP handshakes.

Returning **full room objects** increases the initial payload size and consumes more bandwidth per request. However, it provides the client with all the necessary data in a single round-trip. For a campus dashboard application, minimizing latency and HTTP overhead is generally prioritized, making the return of full objects the more efficient and performant approach.

### Question 2: Idempotency of the DELETE Operation
Yes, the `DELETE` operation in this implementation is strictly **idempotent**. 

Idempotency guarantees that executing the same request multiple times has the exact same effect on the server's state as executing it once. If a client mistakenly sends the exact same `DELETE /api/v1/rooms/LIB-301` request multiple times, the following occurs:
1. **First Request:** The room is successfully deleted, and the server returns a `204 No Content`.
2. **Subsequent Requests:** The server checks the data store, sees that the room does not exist, and returns a `404 Not Found`.

Crucially, the server's internal state does not change after the first request. Whether the client sends one request or fifty, the final state of the campus infrastructure remains identical: the room is absent.

---

## Part 3: Sensor Operations & Linking

### Question 1: @Consumes Annotation and Media Type Mismatches
The `@Consumes(MediaType.APPLICATION_JSON)` annotation on the `POST` method acts as a strict gateway for incoming requests. 

If a client attempts to send data in a different format—such as `text/plain` or `application/xml`—the JAX-RS runtime will intercept the request before it ever reaches the application's Java logic. The framework will automatically reject the request and return an **HTTP 415 Unsupported Media Type** status code to the client.

The technical consequence of this is highly beneficial: it guarantees payload contracts and relieves developers from having to write boilerplate code to manually parse, validate, or gracefully fail on unrecognized data formats.

### Question 2: Query Parameters vs. Path Parameters for Filtering
The query parameter approach (`/api/v1/sensors?type=CO2`) is vastly superior to the path parameter approach (`/api/v1/sensors/type/CO2`) for filtering collections.

Path parameters are designed to identify specific resources within a strict, hierarchical structure. Using them for filtering creates rigid URIs that are difficult to scale. Conversely, query parameters are inherently optional, composable, and order-independent. 

If the application later needs to filter sensors by multiple criteria (e.g., type and status), query parameters handle this cleanly (`?type=CO2&status=ACTIVE`). Attempting to achieve the same result with path parameters would lead to complex, convoluted, and brittle routing structures. Query parameters align perfectly with standard RESTful conventions for filtering, sorting, and pagination.

---

## Part 4: Deep Nesting with Sub-Resources

### Question 1: Benefits of the Sub-Resource Locator Pattern
The Sub-Resource Locator pattern provides significant architectural benefits by preventing resource classes from swelling into monolithic "god controllers."

By delegating the logic for `/sensors/{id}/readings` to a dedicated `SensorReadingResource` class, we enforce the **Single Responsibility Principle**. The parent `SensorResource` is responsible only for sensor-level operations, while the sub-resource handles exclusively reading-level logic. 

This separation of concerns makes large APIs significantly easier to navigate, test, and maintain. Furthermore, it enhances code reusability—the `SensorReadingResource` could theoretically be attached to different parts of the API without modification, as its internal logic is completely isolated from the parent's routing context.

---

## Part 5: Advanced Error Handling, Exception Mapping & Logging

### Question 1: HTTP 422 vs. HTTP 404 for Dependency Validation
When a client submits a valid JSON payload to `POST /api/v1/sensors`, but the `roomId` inside that payload references a room that does not exist, an **HTTP 422 (Unprocessable Entity)** is the most semantically accurate response.

Returning an `HTTP 404 (Not Found)` in this scenario is misleading. A 404 implies that the target URI itself (`/api/v1/sensors`) does not exist, which could cause a client developer to assume they have the wrong endpoint. 

A 422 Unprocessable Entity accurately communicates that the server understands the content type of the request and the syntax is correct, but it was unable to process the contained instructions due to a semantic failure—in this case, a failed foreign key constraint validation.

### Question 2: Security Risks of Exposing Stack Traces
Exposing internal Java stack traces to external API consumers is a critical vulnerability known as **Information Disclosure**.

When an attacker views a raw stack trace, they gain a blueprint of the server's internal environment. Specific information leaked includes:
- **Framework and Dependency Versions:** Attackers can see exactly which versions of libraries (e.g., Jersey, Jackson) are running, allowing them to cross-reference databases for known Common Vulnerabilities and Exposures (CVEs).
- **Internal Architecture:** Package names, class structures, and file paths reveal how the application is built, making it easier to craft targeted logic-flow exploits.
- **Sensitive Configuration:** Stack traces stemming from database or filesystem errors frequently leak connection strings, internal IP addresses, or database schema names.

By implementing a global `ExceptionMapper<Throwable>`, we ensure that all unhandled exceptions are logged securely on the server while presenting a safe, generic `500 Internal Server Error` to the outside world.

### Question 3: Advantages of JAX-RS Filters for Logging
Utilizing JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` interfaces for logging demonstrates the use of Aspect-Oriented Programming (AOP). This approach is far superior to manually inserting `Logger.info()` statements inside every resource method for several reasons:

1. **Guaranteed Consistency:** A filter applies globally. There is no risk of a developer forgetting to add logging statements when creating a new API endpoint.
2. **Separation of Concerns:** Resource classes remain clean and focused entirely on core business logic. Infrastructure and cross-cutting concerns (like logging, authentication, or CORS headers) are abstracted away.
3. **Maintainability:** If the logging format needs to be updated (e.g., logging response times or adding request IDs), the change only needs to be made in one centralized file rather than in dozens of separate methods across the codebase.
