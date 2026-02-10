package com.malcolm.ecomproj;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Ecommerce Backend (REST API).
 * This service handles products, orders, and user data.
 */
@SpringBootApplication
public class EcomProjApplication {

    public static void main(String[] args) {
        // Starts the Spring Boot backend
        SpringApplication.run(EcomProjApplication.class, args);
    }

}
