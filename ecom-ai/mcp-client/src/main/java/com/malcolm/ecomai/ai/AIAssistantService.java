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

import com.malcolm.ecomai.ai.memory.*;
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

    private final ChatMetadataRepository chatMetadataRepository;

    private static final String DEFAULT_USER_ID = "anonymous";
    private static final String DESCRIPTION_PROMPT = "Generate a chat description based on the message, limiting the description to 30 characters: ";

    public AIAssistantService(ChatClient.Builder chatClientBuilder,
            List<ToolCallbackProvider> toolCallbackProviders,
            org.springframework.ai.chat.memory.ChatMemory chatMemory,
            ChatMetadataRepository chatMetadataRepository) {

        this.chatMetadataRepository = chatMetadataRepository;

        System.out.println("Discovered " + toolCallbackProviders.size() + " ToolCallbackProviders");
        for (ToolCallbackProvider provider : toolCallbackProviders) {
            System.out.println("Provider: " + provider.getClass().getName());
            System.out.println("Tool count: " + provider.getToolCallbacks().length);
        }
        String systemPrompt = """
                You are a helpful AI assistant for an Ecommerce platform.
                You have access to product search and data analysis tools.
                You can search for products using the tool `searchProducts`.
                Always use the provided tools to get accurate information about products,
                stock levels, and user activity.
                """;

        // Initialize ChatClient with system prompt, memory advisor and registered tools
        this.chatClient = chatClientBuilder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor.builder(chatMemory).build())
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
                availableModels.add("llama-3.3-70b");
            }
        } catch (IOException e) {
            logger.error("Failed to load models from file", e);
            availableModels.add("llama-3.3-70b");
        }
    }

    /**
     * Executes the chat request with automatic failover.
     * If a model returns a Rate Limit error (429), it switches to the next
     * available model.
     * 
     * @param userMessage    The user's input
     * @param model          The preferred model (ignored if we need to rotate)
     * @param conversationId The unique identifier for the chat memory session
     * @return The AI response
     */
    public String chat(String userMessage, String model, String conversationId) {
        int maxRetries = availableModels.isEmpty() ? 1 : availableModels.size();
        int attempts = 0;

        // Ensure we always have a conversationId for the chat memory
        String activeConversationId = (conversationId != null && !conversationId.isBlank())
                ? conversationId
                : "default_session";

        // Validate if conversationId exists in metadata (except for default_session)
        if (!activeConversationId.equals("default_session")
                && !chatMetadataRepository.chatIdExists(activeConversationId)) {
            logger.warn("Chat ID {} does not exist in metadata. It might be an old session.", activeConversationId);
        }

        while (attempts < maxRetries) {
            String currentModel = availableModels.isEmpty() ? "llama-3.3-70b-versatile"
                    : availableModels.get(currentModelIndex);

            try {
                logger.info("Attempting chat with model: {} for conversation: {}", currentModel, activeConversationId);
                return chatClient.prompt()
                        .user(Objects.requireNonNull(userMessage))
                        .options(OpenAiChatOptions.builder().model(currentModel).build())
                        // Add the conversation ID to the advisors parameters
                        .advisors(a -> a.param(org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID,
                                activeConversationId))
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

    public ChatStartResponse createChatWithResponse(String message, String model) {
        String description = this.generateDescription(message, model);
        String chatId = this.chatMetadataRepository.createChat(DEFAULT_USER_ID, description);
        String response = this.chat(message, model, chatId);
        return new ChatStartResponse(chatId, response, description);
    }

    public List<ChatMetadata> getAllChats() {
        return this.chatMetadataRepository.getAllChatsForUser(DEFAULT_USER_ID);
    }

    public List<ChatMessage> getChatMessages(String chatId) {
        return this.chatMetadataRepository.getChatMessages(chatId);
    }

    public void updateChatDescription(String chatId, String description) {
        this.chatMetadataRepository.updateChatDescription(chatId, description);
    }

    public void deleteChat(String chatId) {
        this.chatMetadataRepository.deleteChat(chatId);
    }

    private String generateDescription(String message, String model) {
        try {
            return this.chatClient.prompt()
                    .user(DESCRIPTION_PROMPT + message)
                    .options(OpenAiChatOptions.builder().model(model).build())
                    .call()
                    .content();
        } catch (Exception e) {
            logger.error("Failed to generate description", e);
            return "New Chat";
        }
    }

    private synchronized void rotateModel() {
        if (!availableModels.isEmpty()) {
            currentModelIndex = (currentModelIndex + 1) % availableModels.size();
            logger.info("Rotated to model index {}: {}", currentModelIndex, availableModels.get(currentModelIndex));
        }
    }
}
