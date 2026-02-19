package com.malcolm.ecomai.controller;

import com.malcolm.ecomai.ai.AIAssistantService;
import com.malcolm.ecomai.ai.ChatRequest;
import com.malcolm.ecomai.ai.ChatResponse;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-related operations.
 * Exposes endpoints for the user to chat with the AI assistant.
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // Enable CORS for all origins for simplicity in dev
public class AIController {

    // Service to handle complex AI logic and state management
    private final AIAssistantService aiAssistantService;

    // Client to communicate with the MCP Server (ecom-ai/mcp-server)
    private final McpSyncClient mcpSyncClient;

    public AIController(AIAssistantService aiAssistantService, McpSyncClient mcpSyncClient) {
        this.aiAssistantService = aiAssistantService;
        this.mcpSyncClient = mcpSyncClient;
    }

    /**
     * Main Chat Endpoint.
     * Receives a user message, forwards it to the AI service, and returns the
     * response.
     * 
     * @param request The chat request containing message and optional model
     *                selection
     * @return The AI's response text
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // Use the provided model or fallback to a default one (Llama 3.3)
            String model = request.getModel() != null ? request.getModel() : "llama-3.3-70b-versatile";

            // Delegate the chat logic to the AIAssistantService
            String response = aiAssistantService.chat(request.getMessage(), model);

            return ResponseEntity.ok(new ChatResponse(response, "success", model));
        } catch (Exception e) {
            // Detailed error handling to assist in debugging
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " | Details: " + e.getCause().getMessage();
            }
            return ResponseEntity.internalServerError()
                    .body(new ChatResponse("Error: " + errorMessage, "error", "unknown"));
        }
    }

    /**
     * List all available prompts from the MCP server.
     * This acts as a proxy, fetching capabilities from the MCP server and
     * sending them to the frontend.
     */
    @GetMapping("/prompts")
    public ResponseEntity<?> listPrompts() {
        try {
            // Fetch list of prompts (templates) from the connected MCP Server
            var prompts = mcpSyncClient.listPrompts(null);
            return ResponseEntity.ok(prompts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error listing prompts: " + e.getMessage());
        }
    }

    /**
     * Get a specific prompt from the MCP server.
     */
    @GetMapping("/prompts/{name}")
    public ResponseEntity<?> getPrompt(@PathVariable String name,
            @RequestParam java.util.Map<String, String> allParams) {
        try {
            // Convert Map<String, String> to Map<String, Object> for the MCP client
            java.util.Map<String, Object> args = new java.util.HashMap<>(allParams);

            var prompt = mcpSyncClient
                    .getPrompt(new io.modelcontextprotocol.spec.McpSchema.GetPromptRequest(name, args));
            return ResponseEntity.ok(prompt);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error getting prompt '" + name + "': " + e.getMessage());
        }
    }

    /**
     * Simple help endpoint to guide users on how to use the POST endpoint.
     */
    @GetMapping("/chat")
    public String chatHelp() {
        return "This is the AI Chat endpoint! To use it, please send a **POST** request with a JSON body. <br><br>" +
                "Example: <code>{ \"message\": \"Hello\", \"model\": \"gemini\" }</code>";
    }
}
