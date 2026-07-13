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
    const addToCart = useCartStore(state => state.addToCart);
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
                recommendations: agentState.recommendations
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

    const parseIntents = (response) => {
        if (!response) return { text: "No final response provided.", intent: null };
        try {
            const jsonMatch = response.match(/\{[\s\S]*?\}/);
            if (jsonMatch) {
                const intent = JSON.parse(jsonMatch[0]);
                const text = response.replace(jsonMatch[0], "").trim();
                return { text, intent };
            }
        } catch (e) {}
        return { text: response, intent: null };
    };

    const parsed = agentState ? parseIntents(agentState.finalResponse) : null;

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
                                        <ReactMarkdown>{parsed?.text || "No final response provided."}</ReactMarkdown>
                                    </div>
                                    
                                    {parsed?.intent?.type === 'checkout_intent' && (
                                        <div className="mt-3 text-center mb-4">
                                            <a href={parsed.intent.url} target="_blank" rel="noreferrer" className="btn btn-dark btn-lg px-5 py-3 rounded-pill fw-bold shadow-lg">
                                                Proceed to Secure Checkout (Shop Pay)
                                            </a>
                                        </div>
                                    )}
                                    
                                    {parsed?.intent?.type === 'catalog_intent' && (
                                        <div className="mt-3 mb-4 d-flex justify-content-center">
                                            <div className="card border-0 shadow-sm" style={{maxWidth: '300px'}}>
                                                <img src="https://placehold.co/300x200?text=Global+Product" className="card-img-top rounded-top" alt="Product" />
                                                <div className="card-body text-center bg-white rounded-bottom border border-top-0">
                                                    <h5 className="card-title fw-bold">Shopify Global Product</h5>
                                                    <p className="card-text text-success fw-bold mb-3">SKU: {parsed.intent.sku}</p>
                                                    <button className="btn btn-outline-primary rounded-pill w-100">View Details</button>
                                                </div>
                                            </div>
                                        </div>
                                    )}
                                    
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

                                    {agentState.recommendations && agentState.recommendations.length > 0 && (
                                        <div className="mb-4 mt-4">
                                            <h4 className="fw-bold mb-3 border-bottom pb-2 text-success">Recommended For You</h4>
                                            <div className="row g-3">
                                                {agentState.recommendations.map((rec, idx) => (
                                                    <div key={idx} className="col-12">
                                                        <div className="card h-100 border-success shadow-sm">
                                                            <div className="card-body d-flex justify-content-between align-items-center">
                                                                <div>
                                                                    <h5 className="card-title fw-bold text-dark">{rec.name}</h5>
                                                                    <p className="card-text text-primary fw-bold mb-1">₹{rec.price}</p>
                                                                    <p className="card-text small text-muted"><FaRobot className="me-1"/> <em>{rec.reason}</em></p>
                                                                </div>
                                                                <button 
                                                                    className="btn btn-success"
                                                                    onClick={() => addToCart(rec.productId, 1)}
                                                                >
                                                                    Add to Cart
                                                                </button>
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
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
