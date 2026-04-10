package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application configuration class.
 *
 * The @ApplicationPath annotation establishes the versioned base URI for
 * all resources in this application. Every resource path will be prefixed
 * with "/api/v1", providing a clean versioning strategy that allows future
 * API versions (e.g., "/api/v2") to coexist without breaking existing clients.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // No need to override getClasses() or getSingletons() — Jersey's
    // ResourceConfig handles component scanning via the packages() method
    // configured in Main.java. This class primarily serves as the
    // application path anchor.
}
