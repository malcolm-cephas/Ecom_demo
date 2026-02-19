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

    // URL of the MCP Server's SSE endpoint (running on port 9091)
    private static final String SERVER_URL = "http://localhost:9091/sse";

    // Credentials for Basic Auth (must match McpSecurityConfig in server)
    private static final String USERNAME = System.getenv("MCP_CLIENT_USER") != null ? System.getenv("MCP_CLIENT_USER")
            : "client-01";
    private static final String API_KEY = System.getenv("MCP_API_KEY") != null ? System.getenv("MCP_API_KEY")
            : "ecom-secret-key-123";

    /**
     * Creates and configures the synchronous MCP Client.
     * This client connects to the server via Server-Sent Events (SSE) and
     * converts method calls into JSON-RPC messages.
     */
    @Bean
    public McpSyncClient mcpSyncClient() {
        // Prepare Basic Auth header
        String auth = USERNAME + ":" + API_KEY;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        // Build the transport layer with custom headers
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(SERVER_URL)
                .customizeRequest(request -> request.header("Authorization", "Basic " + encodedAuth))
                .build();

        // Create the client with a 10-second timeout
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        System.out.println("DEBUG: Initializing McpSyncClient...");
        client.initialize(); // Perform handshake with server
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
