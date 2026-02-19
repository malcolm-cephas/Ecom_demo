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

    // Spring AI ChatClient for abstracting LLM interactions
    private final ChatClient chatClient;

    // List of available Groq models (loaded from JSON)
    private final List<String> availableModels = new ArrayList<>();

    // Index to track current model for round-robin rotation on failure
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
                You can search for products using the tool `searchProducts`.
                Always use the provided tools to get accurate information about products,
                stock levels, and user activity.
                """;

        // Initialize ChatClient with system prompt and registered tools
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultToolCallbacks(toolCallbackProviders.toArray(new ToolCallbackProvider[0]))
                .build();

        loadAvailableModels();
    }

    /**
     * Loads available AI models from a JSON file (groq_models.json).
     * This avoids hardcoding model names and allows dynamic updates.
     */
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
                        // Filter out audio models like whisper to keep only text chat models
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
     * Executes the chat request with automatic failover.
     * If a model returns a Rate Limit error (429), it switches to the next
     * available model.
     * 
     * @param userMessage The user's input
     * @param model       The preferred model (ignored if we need to rotate)
     * @return The AI response
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
                // Check for rate limit error (429) or related messages ("probation", "rate
                // limit")
                if (e.getMessage() != null
                        && (e.getMessage().contains("429") || e.getMessage().toLowerCase().contains("probation")
                                || e.getMessage().toLowerCase().contains("rate limit"))) {
                    logger.warn("Rate limit exceeded for model: {}. Switching to next model...", currentModel);
                    rotateModel();
                    attempts++;
                } else {
                    // Rethrow other errors (like Auth failures or Network issues)
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
