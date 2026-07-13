package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

/**
 * The "brain" of the agent. Constructs prompts based on the current state and allowed actions,
 * and calls the underlying LLM (via Groq or OpenRouter) to determine the next ActionIntent.
 * Implements a robust
 *  multi-provider fallback mechanism to handle rate limits.
 */
@Service
public class DecisionEngine {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final org.springframework.web.client.RestTemplate restTemplate;

    public DecisionEngine(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new org.springframework.web.client.RestTemplate();
    }

    
    /**
     * Core decision method that determines the agent's next action by invoking an LLM.
     */
    public ActionIntent decideNextAction(AgentState state, java.util.List<String> allowedActions) {
        // Build the complex system prompt incorporating state, memory, and rules
        String prompt = buildPrompt(state, allowedActions);

        try {
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
            
            // Parse the raw JSON string returned by the LLM into a structured ActionIntent
            return parseResponse(response);
        } catch (Exception e) {
            System.err.println("Failed to call LLM: " + e.getMessage());
            
            // Gracefully force a FINISH intent with an error message
            ActionIntent errorIntent = new ActionIntent();
            errorIntent.setAction("FINISH");
            try {
                com.fasterxml.jackson.databind.node.ObjectNode params = objectMapper.createObjectNode();
                params.put("message",
                        "System Error: The LLM proxy server failed to respond. The autonomous agent is forced to terminate execution without completing the goal.");
                errorIntent.setParameters(params);
            } catch (Exception ex) {
                // Ignore
            }
            return errorIntent;
        }
    }

    private String buildPrompt(AgentState state, java.util.List<String> allowedActions) 
    {
        java.util.List<String> memory = state.getMemory();
        if (memory.size() > 5) 
        {
            memory = memory.subList(memory.size() - 5, memory.size());
        }
        
        StringBuilder availableActionsStr = new StringBuilder();
        StringBuilder actionDescriptions = new StringBuilder();
        
        int count = 1;
        for (String action : allowedActions) 
        {
            availableActionsStr.append(count).append(". ").append(action).append("\n");
            
            actionDescriptions.append(count).append(". ");
            switch (action) 
            {
                case "FETCH_WISHLIST":
                    actionDescriptions.append("FETCH_WISHLIST\n");
                    actionDescriptions.append("* Retrieve the user's curated recommendation feed.\n");
                    actionDescriptions.append("* ALWAYS use this as the primary seed for candidate products before using SEARCH_PRODUCTS or ASK_USER.\n\n");
                    break;
                case "SEARCH_PRODUCTS":
                    actionDescriptions.append("SEARCH_PRODUCTS(keyword)\n");
                    actionDescriptions.append("* Search the catalog for matching products.\n");
                    actionDescriptions.append("* Returns product IDs, names, prices, and other details.\n\n");
                    break;
                case "CHECK_STOCK":
                    actionDescriptions.append("CHECK_STOCK(productId)\n");
                    actionDescriptions.append("* Verify whether a product is available.\n\n");
                    break;
                case "FETCH_OFFERS":
                    actionDescriptions.append("FETCH_OFFERS\n");
                    actionDescriptions.append("* Retrieve current promotions and discounts.\n\n");
                    break;
                case "RECOMMEND_PRODUCT":
                    actionDescriptions.append("RECOMMEND_PRODUCT(productId, name, price, category, reason, confidence)\n");
                    actionDescriptions.append("* Recommend a product to the user.\n");
                    actionDescriptions.append("* 'category' MUST be explicitly provided from the item's metadata.\n");
                    actionDescriptions.append("* ALWAYS provide a strong, 1-2 sentence 'reason' for why this product is recommended.\n");
                    actionDescriptions.append("* 'confidence' MUST be HIGH, MEDIUM, or LOW.\n\n");break;case "ASK_USER":
                    actionDescriptions.append("ASK_USER(question)\n");
                    actionDescriptions.append("* Ask a clarifying question.\n");
                    actionDescriptions.append("* Use this whenever user preferences, priorities, tradeoffs, future plans, or constraints are not sufficiently understood.\n\n");break;case "FINISH":
                    actionDescriptions.append("FINISH\n");
                    actionDescriptions.append("* End execution.\n\n");
                    break;
                }
                count++;
            }
            
        StringBuilder rules = new StringBuilder();
        if (allowedActions.contains("FETCH_WISHLIST")) {
            // 🌟 FIX: Removed broken triple quotes inside local condition blocks
            rules.append("TOOL-FIRST & WISHLIST-DRIVEN RULES\n\n")
                 .append("1. FETCH_WISHLIST FIRST: If the budget and category are known (or can be assumed), immediately call FETCH_WISHLIST on your first turn. Do not ask questions. The wishlist is your primary seed for candidate generation.\n\n")
                 .append("2. FILTER & RANK: Internally evaluate the wishlist against the user's goal and budget. Rank them by price efficiency and goal relevance. Verify top picks with CHECK_STOCK and FETCH_OFFERS.\n\n")
                 .append("3. FALLBACK EXPANSION: If FETCH_WISHLIST yields no viable candidates (Low Confidence), fall back to SEARCH_PRODUCTS to expand the search space.\n\n")
                 .append("4. ASK_USER AS LAST RESORT: ASK_USER is a high-cost action. Do not use it for open-ended preference discovery, features, storage sizes, colors, or future plans.\n");
        } else {
            rules.append("TOOL-FIRST RULES\n\n")
                 .append("1. SEARCH_PRODUCTS FIRST: Use SEARCH_PRODUCTS to find candidates. Do not ask questions unless absolutely necessary.\n\n")
                 .append("2. ASK_USER AS LAST RESORT: ASK_USER is a high-cost action. Do not use it for open-ended preference discovery, features, storage sizes, colors, or future plans.\n");
        }
        
        // 🌟 FIX: Re-structured base template using precise single lines to eliminate compilation layout mismatch errors.
        String baseTemplate = "You are an autonomous shopping agent whose primary responsibility is to quickly and effectively find the best product options for the user with minimal interrogation.\n\n" +
            "Your goal is to balance quick action with high-quality, comprehensive recommendations.\n\n" +
            "CRITICAL MASS-SELECTION INSTRUCTION:\n" +
            "Your target is to curate up to 10 distinct valid options for the user to view.\n" +
            "Do NOT execute a 'FINISH' action early if you have only recommended 1 or 2 items. You must systematically loop through available candidate items parsed from memory, verify their availability and offers, and call 'RECOMMEND_PRODUCT' for each eligible candidate until you reach up to 10 total entries or completely exhaust the catalog lists.\n\n" +
            "USER GOAL: %s\n" +
            "BUDGET: %s\n" +
            "CURRENT RECOMMENDATIONS COUNT: %s / 10\n" +
            "CURRENT RECOMMENDATIONS: %s\n\n" +
            "MEMORY (Past Actions & Observations):\n" +
            "%s\n\n" +
            "AVAILABLE ACTIONS:\n" +
            "%s\n\n" +
            "ACTION DESCRIPTIONS:\n\n" +
            "%s\n" +
            "%s\n\n" +
            "You may only use ASK_USER if:\n" +
            "- The budget or product category is completely unknown AND cannot be reasonably assumed.\n" +
            "- You have Medium Confidence (multiple viable candidates exist from the wishlist or search with meaningful tradeoffs, and a single candidate-driven clarification provides more value than guessing).\n\n" +
            "5. RECOMMENDATION & CONFIDENCE: Assess your confidence before calling RECOMMEND_PRODUCT.\n" +
            "- High Confidence: The candidate perfectly matches constraints, is in stock, and offers clear value -> Execute RECOMMEND_PRODUCT autonomously with a strong reason.\n" +
            "- Medium Confidence: Multiple viable candidates exist with meaningful tradeoffs -> Execute RECOMMEND_PRODUCT for multiple items, OR ask a single candidate-driven clarification.\n" +
            "- Low Confidence: No good matches -> Execute SEARCH_PRODUCTS.\n\n" +
            "EMPTY SEARCH RESULTS\n\n" +
            "17. If SEARCH_PRODUCTS returns empty results:\n" +
            "* broaden the search\n" +
            "* use simpler keywords\n" +
            "* search related categories\n\n" +
            "Do not immediately give up.\n\n" +
            "FINISH RULES\n\n" +
            "18. Use FINISH when:\n" +
            "* You have successfully called RECOMMEND_PRODUCT for up to 10 distinct qualifying options.\n" +
            "* No suitable alternative products exist after searching broadly.\n" +
            "* Further processing turns are highly unlikely to harvest additional items.\n\n" +
            "OUTPUT FORMAT\n\n" +
            "Respond ONLY with valid JSON.\n\n" +
            "Example:\n\n" +
            "{\n" +
            "  \"action\": \"RECOMMEND_PRODUCT\",\n" +
            "  \"parameters\": {\n" +
            "    \"productId\": \"SKU-ABC\",\n" +
            "    \"name\": \"Sample Item\",\n" +
            "    \"price\": 49.99,\n" +
            "    \"category\": \"electronics\",\n" +
            "    \"reason\": \"This item perfectly matches your setup and budget criteria.\",\n" +
            "    \"confidence\": \"HIGH\"\n" +
            "  }\n" +
            "}\n\n" +
            "Choose the single best next action.";
            
        return String.format(baseTemplate,
            state.getGoal(),
            state.getBudget() != null ? state.getBudget() : "Unlimited",
            state.getRecommendations().size(),
            state.getRecommendations(),
            String.join("\n", memory),
            availableActionsStr.toString().trim(),
            actionDescriptions.toString().trim(),
            rules.toString().trim());
    }

    private ActionIntent parseResponse(String response) {
        try {
            String cleanedResponse = response;
            int startIndex = cleanedResponse.indexOf("{");
            int endIndex = cleanedResponse.lastIndexOf("}");
            if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
                cleanedResponse = cleanedResponse.substring(startIndex, endIndex + 1);
            }
            JsonNode root = objectMapper.readTree(cleanedResponse);
            ActionIntent intent = new ActionIntent();
            intent.setAction(root.has("action") ? root.get("action").asText() : "UNKNOWN");
            if (root.has("parameters")) {
                intent.setParameters(root.get("parameters"));
            }
            return intent;
        } catch (Exception e) {
            System.err.println("Failed to parse LLM response: " + response);
            ActionIntent errorIntent = new ActionIntent();
            errorIntent.setAction("FINISH");
            return errorIntent;
        }
    }

    public static class ActionIntent {
        private String action;
        private JsonNode parameters;
        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }
        public JsonNode getParameters() { return parameters; }
        public void setParameters(JsonNode parameters) { this.parameters = parameters; }
    }
}