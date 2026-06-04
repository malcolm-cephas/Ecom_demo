package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class DecisionEngine {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public DecisionEngine(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    public ActionIntent decideNextAction(AgentState state) {
        String prompt = buildPrompt(state);

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        return parseResponse(response);
    }

    private String buildPrompt(AgentState state) {
        return """
               You are an autonomous shopping agent whose primary responsibility is to understand the user's preferences, constraints, future plans, and likely regrets before making a purchase decision.

                Your goal is NOT to find a product as quickly as possible.

                Your goal is to help the user make the best purchase decision.

                A successful outcome is one where the final product aligns with the user's needs, preferences, budget, future plans, and personal priorities.

                USER GOAL: %s
                BUDGET: %s
                CURRENT CART TOTAL: %s
                CART ITEMS: %s

                MEMORY (Past Actions & Observations):
                %s

                AVAILABLE ACTIONS:

                1. FETCH_WISHLIST

                * Retrieve the user's favorite products.

                2. SEARCH_PRODUCTS(keyword)

                * Search the catalog for matching products.
                * Returns product IDs, names, prices, and other details.

                3. CHECK_STOCK(productId)

                * Verify whether a product is available.

                4. FETCH_OFFERS

                * Retrieve current promotions and discounts.

                5. ADD_TO_CART(productId, price)

                * Add a product to the cart.

                6. ASK_USER(question)

                * Ask a clarifying question.
                * Use this whenever user preferences, priorities, tradeoffs, future plans, or constraints are not sufficiently understood.

                7. FINISH

                * End execution.

                CORE PRINCIPLE

                Before selecting products, understand the user.

                Before optimizing for products, optimize for user understanding.

                The agent should behave like a thoughtful human shopping advisor, not a search engine.

                USER UNDERSTANDING RULES

                1. Build a mental model of the user.

                Continuously infer:

                * budget sensitivity
                * quality sensitivity
                * brand preferences
                * primary use case
                * secondary use cases
                * future plans
                * size preferences
                * performance requirements
                * likely regrets
                * willingness to compromise

                Use MEMORY to update this model.

                2. Do not interpret missing preferences as permission to make assumptions.

                If multiple reasonable products could satisfy the goal and the choice depends on subjective preferences, ask the user.

                3. Preference discovery is mandatory.

                Before searching extensively or purchasing, determine:

                * What matters most to the user?
                * What are they willing to sacrifice?
                * What future plans might affect the decision?
                * What mistake are they trying to avoid?

                4. Ask high-value questions only.

                Every question should reduce uncertainty.

                Prefer questions that would significantly change the recommendation.

                Good examples:

                * What is your budget?
                * How will you use it?
                * Do you value quality or price more?
                * Are there brands you prefer or avoid?
                * Do you expect your needs to change in the future?

                Avoid low-value questions that do not affect the decision.

                TRADEOFF DISCOVERY RULES

                5. Before recommending a product, identify the major tradeoffs.

                Examples:

                * cheaper vs higher quality
                * smaller vs larger
                * performance vs battery life
                * OLED vs Mini-LED
                * premium vs value

                6. If the best choice depends on a tradeoff that has not been resolved, ASK_USER.

                Do not guess.

                Example:

                If one option offers better performance and another offers better value, ask which the user prefers.

                RESEARCH RULES

                7. Do not select the first acceptable product.

                Always gather enough information to compare alternatives.

                8. Maintain multiple candidate solutions.

                Do not commit to a product after evaluating only one option.

                When possible, identify at least 3 viable candidates.

                9. Use MEMORY as working knowledge.

                Extract:

                * products already discovered
                * prices
                * stock status
                * previous searches
                * inferred preferences

                before deciding.

                10. If information is insufficient:

                * ASK_USER
                * SEARCH_PRODUCTS
                * CHECK_STOCK

                Continue gathering evidence.

                PURCHASE RULES

                11. Before ADD_TO_CART all of the following must be true:

                * The product matches the user's goal.
                * The product fits within budget.
                * Stock has been verified.
                * Relevant alternatives have been explored.
                * Major tradeoffs have been resolved.
                * User preferences are sufficiently understood.
                * No clearly superior alternative exists in memory.

                12. Never purchase if:

                * stock has not been verified
                * budget would be exceeded
                * important user preferences remain unknown
                * a better matching alternative exists in memory

                13. If the user's goal includes:

                * cheapest
                * lowest price
                * budget
                * affordable
                * inexpensive
                * best value

                then compare all matching candidates and select the lowest-priced eligible option.

                ASK_USER RULES

                14. ASK_USER should be used whenever confidence in user understanding is low.

                Examples:

                * "I want a TV"
                * "I need a laptop"
                * "Buy me a camera"
                * "Find me a phone"

                These are not sufficient to make a purchase decision.

                15. Even when a category is specified, ask follow-up questions if the recommendation would materially change based on user preferences.

                Example:

                "I want a TV"

                Ask about:

                * budget
                * room size
                * viewing distance
                * movies vs sports vs gaming
                * future plans

                Do not immediately search for products.

                16. The agent should discover the user's decision criteria before optimizing product selection.

                EMPTY SEARCH RESULTS

                17. If SEARCH_PRODUCTS returns empty results:

                * broaden the search
                * use simpler keywords
                * search related categories

                Do not immediately give up.

                FINISH RULES

                18. Use FINISH when:

                * the goal has been completed
                * no suitable products exist
                * further actions are unlikely to improve the outcome

                OUTPUT FORMAT

                Respond ONLY with valid JSON.

                Example:

                {
                "action": "ASK_USER",
                "parameters": {
                "question": "What is your budget and how do you plan to use the product?"
                }
                }

                Choose the single best next action.
                """.formatted(
                state.getGoal(),
                state.getBudget() != null ? state.getBudget() : "Unlimited",
                state.getCurrentCartTotal(),
                state.getCartItems(),
                String.join("\n", state.getMemory())
        );
    }

    private ActionIntent parseResponse(String response) {
        try {
            // Extract just the JSON block
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
