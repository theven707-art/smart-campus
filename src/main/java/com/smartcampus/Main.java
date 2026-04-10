package com.smartcampus;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the Smart Campus API.
 *
 * This class bootstraps an embedded Grizzly HTTP server and configures
 * Jersey to scan the com.smartcampus package for resource classes,
 * exception mappers, and filters. The server listens on port 8080
 * and serves all endpoints under the /api/v1 base path.
 */
public class Main {

    // Base URI that the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    /**
     * Creates and configures the Grizzly HTTP server with Jersey.
     *
     * @return a configured and started HttpServer instance
     */
    public static HttpServer startServer() {
        // ResourceConfig scans the entire com.smartcampus package tree
        // to auto-discover @Path resources, @Provider exception mappers, and filters
        final ResourceConfig config = new ResourceConfig()
                .packages("com.smartcampus")
                .register(JacksonFeature.class);

        // Create and start the embedded Grizzly server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    /**
     * Application entry point. Starts the server and waits for user input
     * to gracefully shut down.
     */
    public static void main(String[] args) throws IOException {
        final HttpServer server = startServer();

        LOGGER.log(Level.INFO, "============================================");
        LOGGER.log(Level.INFO, " Smart Campus API started successfully!");
        LOGGER.log(Level.INFO, " Base URI: {0}", BASE_URI);
        LOGGER.log(Level.INFO, " Stop the server by pressing Enter...");
        LOGGER.log(Level.INFO, "============================================");

        System.out.println("\n>>> Smart Campus API is running at " + BASE_URI);
        System.out.println(">>> Press Enter to stop the server...\n");

        // Block until the user presses Enter, then shut down gracefully
        System.in.read();
        server.shutdownNow();
        LOGGER.log(Level.INFO, "Server stopped.");
    }
}
