package com.malcolm.ecomagent.model;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentState {
    private String userId;
    private String goal;
    private Double budget;
    private Double currentCartTotal = 0.0;
    private List<String> cartItems =    new ArrayList<>();
    private List<String> memory = new ArrayList<>();
    
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
    private String finalResponse;

    public void addMemory(String observation) {
        this.memory.add(observation);
    }
}
