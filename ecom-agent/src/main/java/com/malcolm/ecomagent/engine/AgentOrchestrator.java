package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.stereotype.Service;

/**
 * Core orchestration engine that manages the autonomous agent loop.
 * It follows a Think-Act-Observe cycle, using the DecisionEngine to determine
 * the next step and ActionExecutor to perform it, maintaining state across iterations.
 */
@SuppressWarnings("unused")
@Service
public class AgentOrchestrator {

    private final DecisionEngine decisionEngine;
    private final ActionExecutor actionExecutor;

    //Initializes the orchestrator with the decision and action execution services.
    public AgentOrchestrator(DecisionEngine decisionEngine, ActionExecutor actionExecutor) {
        this.decisionEngine = decisionEngine;
        this.actionExecutor = actionExecutor;
    }

    // Determines whether the user's goal matches a wishlist-supported category.
    private boolean isWishlistSupported(String goal) {
        if (goal == null || goal.isEmpty())
            return false;
        String lowerGoal = goal.toLowerCase();
        for (String cat : actionExecutor.getSupportedWishlistCategories()) {
            if (lowerGoal.contains(cat))
                return true;
        }
        return false;
    }

    // Extracts a predefined product category from the user's goal using deterministic keyword matching.
    private String extractCategory(String goal) {
        if (goal == null || goal.isEmpty())
            return null;
        String lowerGoal = goal.toLowerCase();
        // A simple deterministic category extractor
        String[] categories = { "laptop", "smartphone", "phone", "tv", "headphones", "watch", "gaming", "console",
                "tablet", "monitor" };
        for (String cat : categories) {
            if (lowerGoal.contains(cat)) {
                return cat;
            }
        }
        return null;
    }

    private String generateBroadenedKeyword(String originalKeyword) {
        // Strip adjectives or extra descriptors
        return originalKeyword.replaceAll("^(cheap|best|nice|good|affordable)\\s+", "").trim();
    }

    private String extractKeywordDeterministically(String goal) {
        if (goal == null || goal.isEmpty())
            return "";
        // Simple stop-word removal for common shopping prefixes
        String keyword = goal.toLowerCase();
        keyword = keyword.replaceAll("^(i want a|find me a|looking for a|buy a|show me a|need a|get me a)\\s+", "");
        keyword = keyword.replaceAll("under \\d+", "");
        keyword = keyword.replaceAll("[^a-zA-Z0-9 ]", "").trim();
        return keyword;
    }

    // Computes the set of valid actions available to the agent for the current execution cycle.
    private java.util.List<String> computeMask(AgentState state, int currentIteration) {
        java.util.List<String> actions = new java.util.ArrayList<>();
        if (isWishlistSupported(state.getGoal())) {
            actions.add("FETCH_WISHLIST");
        }
        actions.add("SEARCH_PRODUCTS");
        actions.add("CHECK_STOCK");
        actions.add("FETCH_OFFERS");
        actions.add("RECOMMEND_PRODUCT");

        if (currentIteration <= 4) {
            actions.add("ASK_USER");
        }
        actions.add("FINISH");
        return actions;
    }


    public AgentState runAgentLoop(AgentState initialState) {
        int maxIterations = 15;
        int currentIteration = 0;

        System.out.println("Starting Autonomous Agent Loop for Goal: " + initialState.getGoal());

        // 1. Deterministic Bypass
        if (!isWishlistSupported(initialState.getGoal())) {
            String keyword = extractKeywordDeterministically(initialState.getGoal());
            System.out.println(
                    "System: Wishlist unsupported. Auto-transitioning to SEARCH_PRODUCTS for keyword: " + keyword);
            initialState.addMemory(
                    "System Observation: Wishlist doesn't support this category. Auto-transitioning to SEARCH_PRODUCTS.");
            String searchResult = actionExecutor.searchProducts(keyword);
            initialState.addMemory("Action Taken: SEARCH_PRODUCTS | Result: " + searchResult);
            // Record this so we don't duplicate it later
            initialState.getExecutedActions().add("SEARCH_PRODUCTS:{\"keyword\":\"" + keyword + "\"}");
        }

        while (!initialState.isFinished()) // main agent engine loop: Think -> Act -> Observe -> Repeat
        {
            // Safety check: Prevent the autonomous loop from running forever
            if (currentIteration >= maxIterations) {
                System.out.println("Agent exceeded maximum steps (" + maxIterations
                        + "). Force finished to prevent infinite loop.");
                initialState.setFinished(true);
                initialState.setFinalResponse(
                        "System Error: I was unable to complete the task within the maximum allowed steps.");
                break;
            }

            try {
                // Throttle execution slightly to avoid tripping rate limits on LLM APIs
                Thread.sleep(1500); // 1.5 second throttle to prevent rate limit spamming
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            currentIteration++;
            System.out.println("Iteration " + currentIteration);

            // ---------------------------------------------------------
            // THINK PHASE: Determine what to do next based on state
            // ---------------------------------------------------------
            
            // Compute the subset of actions the agent is allowed to take at this moment
            java.util.List<String> allowedActions = computeMask(initialState, currentIteration);
            
            // Query the DecisionEngine (LLM) to select the next action from the allowed list
            DecisionEngine.ActionIntent intent = decisionEngine.decideNextAction(initialState, allowedActions);
            System.out.println("Decided Action: " + intent.getAction());

            // ---------------------------------------------------------
            // Parameter-Aware Action Deduplication & Forced Recovery
            // ---------------------------------------------------------
            // Create a unique key for the action and its parameters to prevent looping
            String dedupeKey = intent.getAction() + ":"
                    + (intent.getParameters() != null ? intent.getParameters().toString() : "");
            if (!"FINISH".equals(intent.getAction()) && !"ASK_USER".equals(intent.getAction())
                    && initialState.getExecutedActions().contains(dedupeKey)) {
                
                // If the LLM repeats an action but we already have recommendations, force FINISH
                if (!initialState.getRecommendations().isEmpty()) {
                    System.out.println("LLM stuck in a loop but goal is satisfied. Forcing FINISH.");
                    initialState.setFinished(true);
                    initialState.setFinalResponse("I have found and recommended products that match your request.");
                    break;
                } else {
                    // Force an ASK_USER fallback to break the infinite loop of failed searches
                    System.out.println("LLM repeating failed actions. Forcing ASK_USER.");
                    intent.setAction("ASK_USER");
                    try {
                        intent.setParameters(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode().put("question", "I tried searching but couldn't find exactly what you're looking for. Could you clarify your request or provide different keywords?"));
                    } catch (Exception ignored) {}
                }
            } else {
                initialState.getExecutedActions().add(dedupeKey);
            }

            // ---------------------------------------------------------
            // ACT & OBSERVE PHASE: Execute the chosen action and gather results
            // ---------------------------------------------------------
            String observation = executeAction(intent, initialState);
            System.out.println("Observation: " + observation);

            // Summarize large JSON arrays from SEARCH_PRODUCTS
            if ("SEARCH_PRODUCTS".equals(intent.getAction()) && observation.trim().startsWith("[")) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode array = mapper.readTree(observation);
                    java.util.List<com.fasterxml.jackson.databind.JsonNode> nodes = new java.util.ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        nodes.add(array.get(i));
                    }
                    
                    final String goalLower = initialState.getGoal() != null ? initialState.getGoal().toLowerCase() : "";
                    nodes.sort((n1, n2) -> {
                        String desc1 = n1.has("description") ? n1.get("description").asText().toLowerCase() : "";
                        String name1 = n1.has("name") ? n1.get("name").asText().toLowerCase() : "";
                        String desc2 = n2.has("description") ? n2.get("description").asText().toLowerCase() : "";
                        String name2 = n2.has("name") ? n2.get("name").asText().toLowerCase() : "";
                        
                        int score1 = 0;
                        int score2 = 0;
                        for (String word : goalLower.split("\\s+")) {
                            if (word.length() > 2) {
                                if (name1.contains(word)) score1 += 2;
                                if (desc1.contains(word)) score1 += 1;
                                if (name2.contains(word)) score2 += 2;
                                if (desc2.contains(word)) score2 += 1;
                            }
                        }
                        return Integer.compare(score2, score1);
                    });

                    StringBuilder summary = new StringBuilder("Found " + nodes.size() + " products:\n");
                    for (int i = 0; i < Math.min(nodes.size(), 10); i++) {
                        com.fasterxml.jackson.databind.JsonNode node = nodes.get(i);
                        summary.append("- ").append(node.path("name").asText())
                               .append(" (ID: ").append(node.path("id").asText()).append(") ");
                        if (node.has("price")) summary.append("Price: ").append(node.path("price").asText());
                        if (node.has("description")) summary.append(" | Desc: ").append(node.path("description").asText());
                        summary.append("\n");
                    }
                    if (nodes.size() > 10) summary.append("... and ").append(nodes.size() - 10).append(" more.");
                    observation = summary.toString();
                } catch (Exception e) {
                    // fall back to raw observation if parsing fails
                }
            }

            // ---------------------------------------------------------
            // Empty Search Recovery State Machine
            // ---------------------------------------------------------
            // If the search returned no results, auto-escalate the search strategy
            if ("SEARCH_PRODUCTS".equals(intent.getAction()) && 
                (observation.contains("[]") || observation.contains("Found 0 products:"))) {
                String currentState = initialState.getSearchState();

                if (currentState.equals("ORIGINAL")) {
                    initialState.setSearchState("BROADENED");
                    String broadKeyword = generateBroadenedKeyword(intent.getParameters().get("keyword").asText());
                    String broadResult = actionExecutor.searchProducts(broadKeyword);
                    observation = "System Recovery: Original search failed. Auto-broadened search to: '" + broadKeyword
                            + "'. Result: " + broadResult;

                } else if (currentState.equals("BROADENED")) {
                    initialState.setSearchState("CATEGORY_ONLY");
                    String category = extractCategory(initialState.getGoal());
                    if (category != null) {
                        String catResult = actionExecutor.searchProducts(category);
                        observation = "System Recovery: Broad search failed. Auto-searching by exact category: '"
                                + category + "'. Result: " + catResult;
                    } else {
                        intent.setAction("ASK_USER");
                        observation = "System Forced Action: Broad search failed for vague query. Ask the user for clarification.";
                    }

                } else {
                    initialState.setSearchState("ASK_USER");
                    intent.setAction("ASK_USER");
                    observation = "System Forced Action: Exhaustive search failed. Ask the user for clarification.";
                }
            }

            // Record the final action and its outcome into the agent's context memory for the next loop
            initialState.addMemory("Action Taken: " + intent.getAction() + " | Result: " + observation);

            // Infinite loop fix: Exit once goal is functionally satisfied or max recommendations reached
            if (initialState.getRecommendations().size() >= 10) {
                String goalLower = initialState.getGoal() != null ? initialState.getGoal().toLowerCase() : "";
                boolean needsStockCheck = goalLower.contains("stock") || goalLower.contains("availability");
                boolean hasStockCheck = false;
                for (String memory : initialState.getMemory()) {
                    if (memory.contains("CHECK_STOCK") || memory.contains("Auto-checked stock")) {
                        hasStockCheck = true;
                        break;
                    }
                }
                if (!needsStockCheck || hasStockCheck) {
                    System.out.println("Goal satisfied internally! Forcing FINISH action.");
                    initialState.setFinished(true);
                    initialState.setFinalResponse("I have found and recommended products that match your request.");
                    break;
                }
            }
        }

        // Return the mutated state object once the goal is complete or max iterations are reached
        return initialState;
    }

    private String executeAction(DecisionEngine.ActionIntent intent, AgentState state) {
        switch (intent.getAction()) {
            case "FETCH_WISHLIST":
                String goal = state.getGoal() != null ? state.getGoal().toLowerCase() : "";
                boolean categorySupported = false;
                for (String cat : actionExecutor.getSupportedWishlistCategories()) {
                    if (goal.contains(cat)) {
                        categorySupported = true;
                        break;
                    }
                }
                if (!categorySupported && !goal.isEmpty()) {
                    return "System Observation: FETCH_WISHLIST bypassed because your requested category is not supported by the wishlist tool. Proceed directly to SEARCH_PRODUCTS.";
                }
                return actionExecutor.fetchWishlist(state.getUserId());

            case "SEARCH_PRODUCTS":
                if (intent.getParameters() != null && intent.getParameters().has("keyword")) {
                    String keyword = intent.getParameters().get("keyword").asText();
                    System.out.println("User Goal: " + state.getGoal());
                    System.out.println("Search Query: " + keyword);
                    String result = actionExecutor.searchProducts(keyword);
                    if (result == null || result.trim().equals("[]") || result.contains("\"length\":0")) {
                        String fallback = extractCategory(state.getGoal());
                        if (fallback != null && !fallback.equalsIgnoreCase(keyword)) {
                            System.out.println("Search empty for '" + keyword + "', falling back to '" + fallback + "'");
                            result = actionExecutor.searchProducts(fallback);
                        }
                    }
                    return result;
                }
                return "Error: Missing keyword parameter.";

            case "CHECK_STOCK":
                if (intent.getParameters() != null && intent.getParameters().has("productId")) {
                    return actionExecutor.checkStock(intent.getParameters().get("productId").asText());
                }
                return "Error: Missing productId parameter.";
            case "FETCH_OFFERS":
                return actionExecutor.fetchOffers();
            case "RECOMMEND_PRODUCT":
                if (intent.getParameters() != null && intent.getParameters().has("productId")
                        && intent.getParameters().has("name") && intent.getParameters().has("price")
                        && intent.getParameters().has("category") && intent.getParameters().has("reason")
                        && intent.getParameters().has("confidence")) {
                    String productId = intent.getParameters().get("productId").asText();
                    
                    // Check if already recommended
                    for (java.util.Map<String, Object> rec : state.getRecommendations()) {
                        if (productId.equals(rec.get("productId"))) {
                            return "Error: Product " + productId + " has already been recommended. Choose a different product to recommend, or call FINISH if you have evaluated all candidates.";
                        }
                    }
                    
                    String name = intent.getParameters().get("name").asText();
                    double price = intent.getParameters().get("price").asDouble();
                    String productCategory = intent.getParameters().get("category").asText();
                    String reason = intent.getParameters().get("reason").asText();
                    String confidence = intent.getParameters().get("confidence").asText().toUpperCase();
                    if (!confidence.equals("HIGH") && !confidence.equals("MEDIUM") && !confidence.equals("LOW")) {
                        return "Error: confidence MUST be HIGH, MEDIUM, or LOW.";
                    }
                    // Category leakage check removed to allow the LLM to dynamically match product names and descriptions
                    String goalLower = state.getGoal() != null ? state.getGoal().toLowerCase() : "";
                    if (goalLower.contains("stock") || goalLower.contains("availability")) {
                        boolean stockChecked = false;
                        for (String mem : state.getMemory()) {
                            if (mem.contains("CHECK_STOCK") && mem.contains(productId)) {
                                stockChecked = true;
                                break;
                            }
                        }
                        if (!stockChecked) {
                            String stockResult = actionExecutor.checkStock(productId);
                            if (stockResult.toLowerCase().contains("out of stock")
                                    || stockResult.toLowerCase().contains("unavailable")) {
                                return "System Intercept: We automatically checked stock for " + productId
                                        + " and it is OUT OF STOCK. Recommendation rejected. Find another product.";
                            } else {
                                state.addMemory("System Intercept: Auto-checked stock for " + productId + ". Result: "
                                        + stockResult);
                            }
                        }
                    }
                    java.util.Map<String, Object> rec = new java.util.HashMap<>();
                    rec.put("productId", productId);
                    rec.put("name", name);
                    rec.put("price", price);
                    rec.put("category", productCategory);
                    rec.put("reason", reason);
                    rec.put("confidence", confidence);
                    state.getRecommendations().add(rec);
                    return "Successfully added product " + productId + " to recommendations with " + confidence
                            + " confidence.";
                }
                return "Error: Missing productId, name, price, category, reason, or confidence parameter.";
            case "ASK_USER":
                if (intent.getParameters() != null && intent.getParameters().has("question")) {
                    state.setFinished(true);
                    state.setRequiresUserInput(true);
                    state.setFinalResponse(intent.getParameters().get("question").asText());
                    return "Asked user for clarification: " + state.getFinalResponse();
                }
                return "Error: Missing question parameter.";
            case "FINISH":
                boolean hasRecommendations = !state.getRecommendations().isEmpty();
                boolean hasMessage = intent.getParameters() != null && intent.getParameters().has("message");
                String messageStr = hasMessage ? intent.getParameters().get("message").asText().trim() : "";
                boolean hasValidMessage = messageStr.split("\s+").length > 15;
                boolean hasSearchFailure = false;
                for (String memory : state.getMemory()) {
                    if (memory.contains("SEARCH_PRODUCTS") && memory.contains("0 results")) {
                        hasSearchFailure = true;
                        break;
                    }
                }
                if (!hasMessage || messageStr.isEmpty()) {
                    state.setFinished(false);
                    return "System Error: FINISH rejected. You MUST provide a 'message' parameter summarizing the recommendation or explaining the failure.";
                }
                if (!hasRecommendations && !(hasValidMessage && hasSearchFailure)
                        && !messageStr.startsWith("System Error:")) {
                    state.setFinished(false);
                    return "System Error: FINISH rejected. You have not recommended any products. You MUST call SEARCH_PRODUCTS, or if search repeatedly failed, provide an exhaustive explanation (>15 words).";
                }
                state.setFinished(true);
                if (intent.getParameters() != null && intent.getParameters().has("message")) {
                    state.setFinalResponse(intent.getParameters().get("message").asText());
                    return "Finished with message: " + state.getFinalResponse();
                }
                state.setFinalResponse("Goal complete.");
                return "Finished without message.";
            default:
                return "Unknown action: " + intent.getAction() + ". Please use one of the available actions.";
        }
    }
}