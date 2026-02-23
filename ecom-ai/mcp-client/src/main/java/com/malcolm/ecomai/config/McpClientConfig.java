package com.malcolm.ecomai.config;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.client.RestClient;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;

@Configuration
public class McpClientConfig {

    // URL of the MCP Server's SSE endpoint (running on port 9091)
    private static final String SERVER_URL = "http://localhost:9091/sse";

    /**
     * Configures the OAuth2AuthorizedClientManager.
     * This manager is responsible for handling the OAuth2 flow and retrieving
     * tokens.
     */
    @Bean
    public OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientService authorizedClientService) {

        OAuth2AuthorizedClientProvider authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder()
                .clientCredentials()
                .build();

        AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrationRepository, authorizedClientService);
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);

        return authorizedClientManager;
    }

    /**
     * Creates and configures the synchronous MCP Client.
     * This client connects to the server via Server-Sent Events (SSE) and includes
     * the OAuth2 token.
     */
    @Bean
    public McpSyncClient mcpSyncClient(OAuth2AuthorizedClientManager authorizedClientManager) {

        // Build the transport layer with custom token injection
        HttpClientSseClientTransport transport = HttpClientSseClientTransport.builder(SERVER_URL)
                .customizeRequest(builder -> {
                    // Fetch the token for 'mcp-client' registration
                    OAuth2AuthorizeRequest authorizeRequest = OAuth2AuthorizeRequest
                            .withClientRegistrationId("mcp-client")
                            .principal("mcp-client-principal")
                            .build();

                    OAuth2AuthorizedClient authorizedClient = authorizedClientManager.authorize(authorizeRequest);

                    if (authorizedClient != null && authorizedClient.getAccessToken() != null) {
                        String token = authorizedClient.getAccessToken().getTokenValue();
                        builder.header("Authorization", "Bearer " + token);
                        System.out.println("DEBUG: Attached access token to MCP Request.");
                        // System.out.println("DEBUG: Token: " + token); // Uncomment for deep debugging
                        // only
                    } else {
                        System.err.println("ERROR: Failed to obtain Access Token for MCP Client!");
                    }
                })
                .build();

        // Create the client with a 10-second timeout
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(10))
                .build();

        try {
            System.out.println("DEBUG: Initializing McpSyncClient...");
            client.initialize(); // Perform handshake with server
            System.out.println("DEBUG: McpSyncClient initialized successfully.");
        } catch (Exception e) {
            System.err.println("ERROR: Failed to initialize MCP Client. Is the server running and secured?");
            e.printStackTrace();
        }

        return client;
    }

    @Bean
    public SyncMcpToolCallbackProvider mcpSyncToolCallbacks(McpSyncClient mcpSyncClient) {
        return new SyncMcpToolCallbackProvider(List.of(mcpSyncClient));
    }

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
