import React, { useState } from 'react';
import axios from 'axios';
import ReactMarkdown from 'react-markdown';
import { FaRobot, FaPaperPlane } from 'react-icons/fa';
import useCartStore from '../store/useCartStore';
import useUserStore from '../store/useUserStore';

const AgentUI = () => {
    const [goal, setGoal] = useState("Find me a cheap laptop and check its stock");
    const [budget, setBudget] = useState(50000);
    const [isLoading, setIsLoading] = useState(false);
    const [agentState, setAgentState] = useState(null);
    const [userReply, setUserReply] = useState("");
    const fetchCart = useCartStore(state => state.fetchCart);
    const user = useUserStore(state => state.user);

    const runAgent = async (e) => {
        e.preventDefault();
        setIsLoading(true);
        setAgentState(null);
        try {
            const currentUserId = user?.sub || user?.name || "testUser";
            // Note: Since ecom-agent is on port 8082, we hit it directly.
            // We use standard axios rather than the configured axios instance 
            // because the configured one points to port 8080.
            const response = await axios.post("http://localhost:8082/api/agent/run", {
                goal: goal,
                userId: currentUserId,
                budget: Number(budget)
            }, {
                headers: { "Content-Type": "application/json" }
            });
            setAgentState(response.data);
            fetchCart(); // Fetch updated cart to sync frontend state!
        } catch (error) {
            console.error("Error running agent:", error);
            alert("Error running agent. Is ecom-agent running on port 8082?");
        } finally {
            setIsLoading(false);
        }
    };

    const handleUserReply = async (e) => {
        e.preventDefault();
        if (!agentState || !userReply.trim()) return;

        setIsLoading(true);
        const currentUserId = user?.sub || user?.name || "testUser";
        
        // Create new memory array with user's reply
        const updatedMemory = [...(agentState.memory || []), "USER REPLY: " + userReply];
        
        try {
            const response = await axios.post("http://localhost:8082/api/agent/run", {
                goal: goal,
                userId: currentUserId,
                budget: Number(budget),
                memory: updatedMemory,
                cartItems: agentState.cartItems,
                currentCartTotal: agentState.currentCartTotal
            }, {
                headers: { "Content-Type": "application/json" }
            });
            setAgentState(response.data);
            setUserReply("");
            fetchCart();
        } catch (error) {
            console.error("Error running agent:", error);
            alert("Error sending reply to agent.");
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <div className="container" style={{ marginTop: "100px", marginBottom: "50px" }}>
            <div className="row justify-content-center">
                <div className="col-md-8">
                    <div className="card shadow-lg border-0 rounded-4">
                        <div className="card-header bg-primary text-white p-4 rounded-top-4 d-flex align-items-center">
                            <FaRobot size={30} className="me-3" />
                            <h3 className="mb-0">Autonomous Shopping Agent (ReAct)</h3>
                        </div>
                        <div className="card-body p-4 bg-light">
                            <p className="text-muted mb-4">
                                This agent uses a native Java ReAct loop to iteratively think and interact with backend APIs to achieve your goal.
                            </p>
                            
                            <form onSubmit={runAgent} className="mb-4">
                                <div className="mb-3">
                                    <label className="form-label fw-bold">Goal</label>
                                    <input 
                                        type="text" 
                                        className="form-control form-control-lg" 
                                        value={goal}
                                        onChange={(e) => setGoal(e.target.value)}
                                        required
                                    />
                                </div>
                                <div className="mb-4">
                                    <label className="form-label fw-bold">Budget (₹)</label>
                                    <input 
                                        type="number" 
                                        className="form-control form-control-lg" 
                                        value={budget}
                                        onChange={(e) => setBudget(e.target.value)}
                                        required
                                    />
                                </div>
                                <button 
                                    type="submit" 
                                    className="btn btn-primary btn-lg w-100 d-flex align-items-center justify-content-center"
                                    disabled={isLoading}
                                >
                                    {isLoading ? (
                                        <>
                                            <span className="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                                            Agent is thinking...
                                        </>
                                    ) : (
                                        <>
                                            <FaPaperPlane className="me-2" />
                                            Run Agent
                                        </>
                                    )}
                                </button>
                            </form>

                            {agentState && (
                                <div className="mt-5 animate__animated animate__fadeIn">
                                    <h4 className="fw-bold mb-3 border-bottom pb-2">Final Response</h4>
                                    <div className={`alert ${agentState.requiresUserInput ? 'alert-warning' : 'alert-success'}`}>
                                        <ReactMarkdown>{agentState.finalResponse || "No final response provided."}</ReactMarkdown>
                                    </div>
                                    
                                    {agentState.requiresUserInput && (
                                        <form onSubmit={handleUserReply} className="mb-4 mt-3 p-3 bg-white rounded border shadow-sm">
                                            <label className="form-label fw-bold text-primary">Reply to Agent:</label>
                                            <div className="input-group">
                                                <input 
                                                    type="text" 
                                                    className="form-control" 
                                                    placeholder="Type your answer here..."
                                                    value={userReply}
                                                    onChange={(e) => setUserReply(e.target.value)}
                                                    required
                                                    disabled={isLoading}
                                                />
                                                <button className="btn btn-primary" type="submit" disabled={isLoading}>
                                                    {isLoading ? "Sending..." : "Reply"}
                                                </button>
                                            </div>
                                        </form>
                                    )}
                                    
                                    <h4 className="fw-bold mt-4 mb-3 border-bottom pb-2">Agent Internal Memory (Reasoning Trace)</h4>
                                    <div className="bg-dark text-light p-3 rounded-3" style={{ maxHeight: "400px", overflowY: "auto", fontFamily: "monospace", fontSize: "0.9rem" }}>
                                        {agentState.memory && agentState.memory.length > 0 ? (
                                            agentState.memory.map((mem, index) => (
                                                <div key={index} className="mb-3 border-bottom border-secondary pb-2">
                                                    <span className="text-warning">[{index + 1}]</span> {mem}
                                                </div>
                                            ))
                                        ) : (
                                            <div>No memory trace recorded.</div>
                                        )}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AgentUI;
