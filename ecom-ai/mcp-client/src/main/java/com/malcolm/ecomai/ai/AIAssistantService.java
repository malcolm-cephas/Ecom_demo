package com.malcolm.ecomai.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Service that handles interactions with the AI model.
 * It uses the MCP Client tools via auto-configuration.
 */
@Service
public class AIAssistantService {

    private final ChatClient chatClient;

    public AIAssistantService(ChatClient.Builder chatClientBuilder,
            List<ToolCallbackProvider> toolCallbackProviders) {

        System.out.println("Discovered " + toolCallbackProviders.size() + " ToolCallbackProviders");
        for (ToolCallbackProvider provider : toolCallbackProviders) {
            System.out.println("Provider: " + provider.getClass().getName());
            System.out.println("  Tool count: " + provider.getToolCallbacks().length);
        }
        String systemPrompt = """
                You are a helpful AI assistant for an Ecommerce platform.
                You have access to product search and data analysis tools.

                Always use the provided tools to get accurate information about products,
                stock levels, and user activity.
                """;

        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(toolCallbackProviders.toArray(new ToolCallbackProvider[0]))
                .build();
    }

    /**
     * Chat entry point used by controllers.
     */
    public String chat(String userMessage, String model) {
        return chatClient.prompt()
                .user(Objects.requireNonNull(userMessage))
                .call()
                .content();
    }
}
