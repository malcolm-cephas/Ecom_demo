package com.malcolm.ecomagent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class EcomAgentApplication {

    public static void main(String[] args) {
        try {
            Dotenv dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } catch (Exception e) {
            System.err.println("Failed to load .env file: " + e.getMessage());
        }
        SpringApplication.run(EcomAgentApplication.class, args);
    }
}
