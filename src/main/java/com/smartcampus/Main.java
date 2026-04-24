package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

// This is the main class that starts the whole application
public class Main {

    // The URL where our API will be available
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    // Sets up the Jersey configuration and starts the Grizzly server
    public static HttpServer startServer() {
        // Tell Jersey to scan our package for resources, filters, and exception mappers
        // Also register Jackson so it can convert Java objects to JSON and back
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);

        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    // Entry point - starts the server and waits for the user to press Enter to stop it
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        LOGGER.log(Level.INFO, "============================================");
        LOGGER.log(Level.INFO, " Smart Campus API started successfully!");
        LOGGER.log(Level.INFO, " Base URI: {0}", BASE_URI);
        LOGGER.log(Level.INFO, " Stop the server by pressing Enter...");
        LOGGER.log(Level.INFO, "============================================");

        System.out.println("\n>>> Smart Campus API is running at " + BASE_URI);
        System.out.println(">>> Press Enter to stop the server...\n");

        // Wait for user input before shutting down
        System.in.read();
        server.shutdownNow();
        LOGGER.log(Level.INFO, "Server stopped.");
    }
}
