package com.malcolm.ecomagent.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentState 
{
    private String userId;
    private String goal;
    private Double budget;
    private Double currentCartTotal = 0.0;
    private List<java.util.Map<String, Object>> recommendations = new ArrayList<>();
    private List<String> memory = new ArrayList<>();
    private java.util.Set<String> executedActions = new java.util.HashSet<>();
    
    // Status tracking
    /* agent's brain
       goal 
       budget
       cartItems
       memory
       currentCartTotal
       isFinished
       finalResponse 
    */
    private boolean isFinished = false;
    private boolean requiresUserInput = false;
    private String searchState = "ORIGINAL";
    private String finalResponse;

    public void addMemory(String observation) 
    {
        this.memory.add(observation);
    }
}
