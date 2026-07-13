package com.malcolm.ecomagent.controller;

import com.malcolm.ecomagent.engine.AgentOrchestrator;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.ai.chat.client.ChatClient;     //ChatClient is a Spring AI abstraction for interacting with chat-based LLMs
import org.springframework.ai.openai.OpenAiChatOptions;   //OpenAiChatOptions is a Spring AI abstraction for configuring OpenAI chat model parameters
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller exposing endpoints for the AI Shopping Agent.
 * Handles both the autonomous multi-step agent loop (/api/agent/run)
 * and single-turn budget optimization requests (/api/agent/optimize).
 */
@CrossOrigin(origins = "*")
@RestController
public class ShoppingAgentController {

    private final ChatClient shoppingAgent;   //Stores the Chatclient instance for interacting with the shopping agent LLM
    private final AgentOrchestrator agentOrchestrator;   //orchestrater instance for managing the agent's execution flow and state transitions

    public ShoppingAgentController(ChatClient shoppingAgent, AgentOrchestrator agentOrchestrator) 
    {
        this.shoppingAgent = shoppingAgent;
        this.agentOrchestrator = agentOrchestrator;
    }

// Run the shopping agent workflow and return the updated agent state.
    @PostMapping("/api/agent/run")
    public AgentState runAgent(@RequestBody AgentState state) {
        return agentOrchestrator.runAgentLoop(state);
    }


    /**
     * Maps GET requests to /api/agent/optimize.
     * This is a simple, single-turn LLM call that submits a budget-aware prompt 
     * to the configured ChatClient, and returns the LLM response content.
     */
    @GetMapping("/api/agent/optimize")
    public String optimizePurchase(@RequestParam double budget) {
        // Initialize an empty options object (could be used to override temperature, etc.)
        OpenAiChatOptions options = OpenAiChatOptions.builder().build();

        // Construct the prompt, send it to the LLM, and extract the text content of the response.
        return this.shoppingAgent.prompt()
                .user("Please optimize my checkout cart list. My total spending cap budget limit is $" + budget)
                .options(options)
                .call()
                .content();
    }
}
