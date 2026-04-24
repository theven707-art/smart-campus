package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

// This sets the base path for all our API endpoints to /api/v1
// So every resource path will start with /api/v1 automatically
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {
    // Jersey handles component scanning through ResourceConfig in Main.java,
    // so we don't need to override anything here
}
