package com.malcolm.ecomai.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service that handles interactions with the AI model.
 * It uses the MCP Client tools via auto-configuration.
 */
@Service
public class AIAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);
    private final ChatClient chatClient;
    private final List<String> availableModels = new ArrayList<>();
    private int currentModelIndex = 0;

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

        loadAvailableModels();
    }

    private void loadAvailableModels() {
        try {
            ClassPathResource resource = new ClassPathResource("groq_models.json");
            if (resource.exists()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(resource.getInputStream());
                JsonNode data = root.get("data");
                if (data != null && data.isArray()) {
                    for (JsonNode node : data) {
                        String modelId = node.get("id").asText();
                        // Filter out audio models like whisper
                        if (modelId != null && !modelId.contains("whisper")) {
                            availableModels.add(modelId);
                        }
                    }
                }
                logger.info("Loaded {} available models from groq_models.json", availableModels.size());
            } else {
                logger.warn("groq_models.json not found in resources");
                // Fallback default
                availableModels.add("llama-3.3-70b-versatile");
            }
        } catch (IOException e) {
            logger.error("Failed to load models from file", e);
            availableModels.add("llama-3.3-70b-versatile");
        }
    }

    /**
     * Chat entry point used by controllers.
     * Implements fallback logic for rate limits.
     */
    public String chat(String userMessage, String model) {
        int maxRetries = availableModels.isEmpty() ? 1 : availableModels.size();
        int attempts = 0;

        while (attempts < maxRetries) {
            String currentModel = availableModels.isEmpty() ? "llama-3.3-70b-versatile"
                    : availableModels.get(currentModelIndex);

            try {
                logger.info("Attempting chat with model: {}", currentModel);
                return chatClient.prompt()
                        .user(Objects.requireNonNull(userMessage))
                        .options(OpenAiChatOptions.builder().model(currentModel).build())
                        .call()
                        .content();
            } catch (Exception e) {
                // Check for rate limit error (429)
                if (e.getMessage() != null
                        && (e.getMessage().contains("429") || e.getMessage().toLowerCase().contains("probation")
                                || e.getMessage().toLowerCase().contains("rate limit"))) {
                    logger.warn("Rate limit exceeded for model: {}. Switching to next model...", currentModel);
                    rotateModel();
                    attempts++;
                } else {
                    // Rethrow other errors
                    throw e;
                }
            }
        }
        throw new RuntimeException("All available models failed due to rate limits.");
    }

    private synchronized void rotateModel() {
        if (!availableModels.isEmpty()) {
            currentModelIndex = (currentModelIndex + 1) % availableModels.size();
            logger.info("Rotated to model index {}: {}", currentModelIndex, availableModels.get(currentModelIndex));
        }
    }
}
