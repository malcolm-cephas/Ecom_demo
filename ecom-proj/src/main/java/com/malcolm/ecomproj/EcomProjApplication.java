package com.malcolm.ecomproj;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcomProjApplication {

    public static void main(String[] args) {
        try {
            // Load .env variables from the root directory (parent of ecom-proj)
            Dotenv dotenv = Dotenv.configure().directory("../").ignoreIfMissing().load();
            dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

            SpringApplication.run(EcomProjApplication.class, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}