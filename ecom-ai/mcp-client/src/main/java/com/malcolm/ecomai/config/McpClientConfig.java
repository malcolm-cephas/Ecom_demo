package com.malcolm.ecomai.config;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Configuration
public class McpClientConfig {

    private static final String SERVER_URL = "http://localhost:9091/sse";
    private static final String USERNAME = "client-01";
    private static final String API_KEY = "YOUR_SECRET_KEY";

    @Bean
    public McpSyncClient mcpSyncClient() {
        String auth = USERNAME + ":" + API_KEY;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(SERVER_URL)
                .customizeRequest(request -> request.header("Authorization", "Basic " + encodedAuth))
                .build();

        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        System.out.println("DEBUG: Initializing McpSyncClient...");
        client.initialize();
        System.out.println("DEBUG: McpSyncClient initialized successfully.");

        return client;
    }

    @Bean
    public SyncMcpToolCallbackProvider mcpSyncToolCallbacks(McpSyncClient mcpSyncClient) {
        return new SyncMcpToolCallbackProvider(List.of(mcpSyncClient));
    }

    @Bean
    public org.springframework.web.client.RestClient.Builder restClientBuilder() {
        return org.springframework.web.client.RestClient.builder();
    }
}
