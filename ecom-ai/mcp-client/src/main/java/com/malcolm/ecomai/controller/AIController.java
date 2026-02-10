package com.malcolm.ecomai.controller;

import com.malcolm.ecomai.ai.AIAssistantService;
import com.malcolm.ecomai.ai.ChatRequest;
import com.malcolm.ecomai.ai.ChatResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for AI-related operations.
 * Exposes endpoints for the user to chat with the AI assistant.
 */
@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final AIAssistantService aiAssistantService;

    public AIController(AIAssistantService aiAssistantService) {
        this.aiAssistantService = aiAssistantService;
    }

    /**
     * Endpoint for user interaction. Accepts a message and returns an AI-generated
     * response.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        try {
            // Use the provided model or fallback to a default one
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
     * Simple help endpoint to guide users on how to use the POST endpoint.
     */
    @GetMapping("/chat")
    public String chatHelp() {
        return "This is the AI Chat endpoint! To use it, please send a **POST** request with a JSON body. <br><br>" +
                "Example: <code>{ \"message\": \"Hello\", \"model\": \"gemini\" }</code>";
    }
}
