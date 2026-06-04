package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.stereotype.Service;

@Service
public class AgentOrchestrator 
{

    private final DecisionEngine decisionEngine;
    private final ActionExecutor actionExecutor;

    public AgentOrchestrator(DecisionEngine decisionEngine, ActionExecutor actionExecutor) 
    {
        this.decisionEngine = decisionEngine;
        this.actionExecutor = actionExecutor;
    }

    public AgentState runAgentLoop(AgentState initialState) 
    {
        int maxIterations = 10;
        int currentIteration = 0;
        
        System.out.println("Starting Autonomous Agent Loop for Goal: " + initialState.getGoal());

        while (!initialState.isFinished() && currentIteration < maxIterations) //main agent engine loop Think-->Act-->Observe-->Repeat
        {
            currentIteration++;
            System.out.println("Iteration " + currentIteration);
            
            // THINK
            DecisionEngine.ActionIntent intent = decisionEngine.decideNextAction(initialState);
            System.out.println("Decided Action: " + intent.getAction());

            // ACT & OBSERVE
            String observation = executeAction(intent, initialState);
            System.out.println("Observation: " + observation);
            
            initialState.addMemory("Action Taken: " + intent.getAction() + " | Result: " + observation);
        }

        if (!initialState.isFinished()) {
            initialState.setFinalResponse("Agent stopped due to reaching maximum iterations.");
        }

        return initialState;
    }

    private String executeAction(DecisionEngine.ActionIntent intent, AgentState state) 
    {
        switch (intent.getAction()) 
        {
            case "FETCH_WISHLIST":
                return actionExecutor.fetchWishlist(state.getUserId());
                
            case "SEARCH_PRODUCTS":
                if (intent.getParameters() != null && intent.getParameters().has("keyword")) 
                {
                    return actionExecutor.searchProducts(intent.getParameters().get("keyword").asText());
                }
                return "Error: Missing keyword parameter.";
                
            case "CHECK_STOCK":
                if (intent.getParameters() != null && intent.getParameters().has("productId")) 
                {
                    return actionExecutor.checkStock(intent.getParameters().get("productId").asText());
                }
                return "Error: Missing productId parameter.";
                
            case "FETCH_OFFERS":
                return actionExecutor.fetchOffers();
                
            case "ADD_TO_CART":
                if (intent.getParameters() != null && intent.getParameters().has("productId") && intent.getParameters().has("price")) 
                {
                    String productId = intent.getParameters().get("productId").asText();
                    double price = intent.getParameters().get("price").asDouble();
                    state.getCartItems().add(productId);
                    state.setCurrentCartTotal(state.getCurrentCartTotal() + price);
                    
                    // Call backend to persist
                    String backendResponse = actionExecutor.addToCart(state.getUserId(), productId, price);
                    
                    return "Successfully added product " + productId + " to cart. New cart total: " + state.getCurrentCartTotal() + ". Backend status: " + backendResponse;
                }
                return "Error: Missing productId or price parameter.";
                
            case "ASK_USER":
                if (intent.getParameters() != null && intent.getParameters().has("question")) 
                {
                    state.setFinished(true);
                    state.setRequiresUserInput(true);
                    state.setFinalResponse(intent.getParameters().get("question").asText());
                    return "Asked user for clarification: " + state.getFinalResponse();
                }
                return "Error: Missing question parameter.";
                
            case "FINISH":
                state.setFinished(true);
                if (intent.getParameters() != null && intent.getParameters().has("message")) 
                {
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
