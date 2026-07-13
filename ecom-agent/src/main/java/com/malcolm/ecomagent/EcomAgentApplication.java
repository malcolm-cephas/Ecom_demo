package com.malcolm.ecomagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

/**
 * Main entry point for the Ecom Agent application.
 * Bootstraps the Spring Boot context and loads environment variables
 * from the parent directory's .env file.
 */
@SpringBootApplication
public class EcomAgentApplication {

    public static void main(String[] args) {
        // Attempt to load environment variables from the parent directory's .env file
        try {
            Dotenv dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            
            // Iterate over all entries in the .env file and set them as system properties
            // so that Spring Boot can access them during initialization.
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
        }
        SpringApplication.run(EcomAgentApplication.class, args);
    }
}
