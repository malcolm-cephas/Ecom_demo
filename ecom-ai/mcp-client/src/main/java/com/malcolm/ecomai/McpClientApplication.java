package com.malcolm.ecomai;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpClientApplication {

    public static void main(String[] args) {
        // Load .env variables from the root directory (parent of ecom-ai/mcp-client)
        Dotenv dotenv = Dotenv.configure().directory("../../").ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(McpClientApplication.class, args);
    }

}
