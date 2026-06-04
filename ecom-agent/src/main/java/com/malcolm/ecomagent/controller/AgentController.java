package com.malcolm.ecomagent.controller;

import com.malcolm.ecomagent.engine.AgentOrchestrator;
import com.malcolm.ecomagent.model.AgentState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin // Allows the React frontend to hit this API
public class AgentController {

    private final AgentOrchestrator orchestrator;

    public AgentController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")  //entry point for the agentic AI logic
    public ResponseEntity<AgentState> runAgent(@RequestBody Map<String, Object> request) {
        String goal = (String) request.get("goal");
        String userId = (String) request.getOrDefault("userId", "anonymous");
        Double budget = request.containsKey("budget") ? Double.valueOf(request.get("budget").toString()) : null;
        //inputs for the agent testing

        AgentState state = new AgentState();
        state.setGoal(goal);
        state.setUserId(userId);
        state.setBudget(budget);
        
        if (request.containsKey("memory")) {
            state.setMemory((java.util.List<String>) request.get("memory"));
        }
        if (request.containsKey("cartItems")) {
            state.setCartItems((java.util.List<String>) request.get("cartItems"));
        }
        if (request.containsKey("currentCartTotal")) {
            state.setCurrentCartTotal(Double.valueOf(request.get("currentCartTotal").toString()));
        }

        AgentState finalState = orchestrator.runAgentLoop(state);  //runs the main agent loop

        return ResponseEntity.ok(finalState);
    }
}
