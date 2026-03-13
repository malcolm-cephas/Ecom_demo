import React, { useState, useRef, useEffect } from 'react';
import axios from 'axios';
import { FaRobot, FaPaperPlane, FaTimes, FaComments, FaUser, FaMagic, FaChevronLeft } from 'react-icons/fa';
import ReactMarkdown from 'react-markdown';
import './ChatBox.css';

/**
 * ChatBox Component
 * Provides a floating chat interface for users to interact with the AI Assistant.
 * Supports standard chat and specialized MCP Prompt execution.
 */
const ChatBox = () => {
    const [isOpen, setIsOpen] = useState(false); // Validates if the chat window is visible

    // Stores the history of chat messages (user + bot)
    const [messages, setMessages] = useState([
        { text: "Hi there! I'm your AI assistant. How can I help you today?", isUser: false }
    ]);

    const [inputValue, setInputValue] = useState("");
    const [isLoading, setIsLoading] = useState(false); // Shows "Thinking..." state

    // State for MCP Prompts (templates)
    const [showPrompts, setShowPrompts] = useState(false);
    const [prompts, setPrompts] = useState([]);
    const [selectedPrompt, setSelectedPrompt] = useState(null);
    const [promptArgs, setPromptArgs] = useState({}); // Stores user input for prompt arguments

    // State for chat history persistence
    const [conversationId, setConversationId] = useState(() => {
        // Retrieve existing session or create a new one
        const savedId = localStorage.getItem('chat_session_id');
        if (savedId) return savedId;
        const newId = 'session-' + Math.random().toString(36).substring(2, 11);
        localStorage.setItem('chat_session_id', newId);
        return newId;
    });

    const messagesEndRef = useRef(null); // Used for auto-scrolling to bottom

    /**
     * Toggles the visibility of the chat window and fetches the list of available
     * MCP prompts from the backend if the chat window is opened and the list is
     * empty.
     */
    const toggleChat = () => {
        setIsOpen(!isOpen);
        if (!isOpen && prompts.length === 0) {
            fetchPrompts();
        }
    };

    // Fetches the list of available MCP prompts from our backend
    const fetchPrompts = async () => {
        try {
            // This endpoint proxies the McpSyncClient.listPrompts() call in Java
            const response = await axios.get('http://localhost:9090/api/ai/prompts');
            if (response.data && response.data.prompts) {
                setPrompts(response.data.prompts);
            }
        } catch (error) {
            console.error("Error fetching prompts:", error);
        }
    };

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages, isOpen]);

    /**
     * Sends a message to the AI backend and appends the response.
     * @param {string} text - Optional text to send (overrides inputValue if provided)
     */
    const handleSendMessage = async (text = null) => {
        const messageText = text || inputValue;
        if (!messageText.trim()) return;

        // Add user message to UI immediately
        const userMessage = { text: messageText, isUser: true };
        setMessages(prev => [...prev, userMessage]);
        setInputValue("");
        setIsLoading(true);

        try {
            // Send request to Spring AI Client (Proxy)
            const response = await axios.post('http://localhost:9090/api/ai/chat', {
                message: messageText,
                model: "llama-3.3-70b-versatile",
                conversationId: conversationId
            });

            if (response.data && response.data.response) {
                const botMessage = { text: response.data.response, isUser: false };
                setMessages(prev => [...prev, botMessage]);
            } else {
                setMessages(prev => [...prev, { text: "Sorry, I didn't get a valid response.", isUser: false }]);
            }
        } catch (error) {
            console.error("Chat error:", error);
            setMessages(prev => [...prev, { text: "Network error. Please make sure the AI service is running on port 9090.", isUser: false }]);
        } finally {
            setIsLoading(false);
        }
    };

    // Called when a user clicks a prompt in the UI
    // Sets up the form fields based on the arguments the prompt requires
    const handlePromptSelect = (prompt) => {
        setSelectedPrompt(prompt);
        const initialArgs = {};
        if (prompt.arguments) {
            prompt.arguments.forEach(arg => {
                initialArgs[arg.name] = "";
            });
        }
        setPromptArgs(initialArgs);
    };

    // Executes the selected prompt with the filled-in arguments
    const handleUsePrompt = async () => {
        setIsLoading(true);
        try {
            // Construct query string for args (e.g., ?recipient=Dad&budget=5000)
            const queryParams = new URLSearchParams(promptArgs).toString();

            // Call the execution endpoint
            const response = await axios.get(`http://localhost:9090/api/ai/prompts/${selectedPrompt.name}?${queryParams}`);

            // The result comes back as a list of messages (text content)
            // We take that content and "send" it as if the user typed it
            if (response.data && response.data.messages && response.data.messages[0]) {
                const promptContent = response.data.messages[0].content.text;

                // Reset UI state
                setShowPrompts(false);
                setSelectedPrompt(null);
                setPromptArgs({});

                // Submit message to chat
                handleSendMessage(promptContent);
            }
        } catch (error) {
            console.error("Error using prompt:", error);
            setMessages(prev => [...prev, { text: "Error executing prompt logic.", isUser: false }]);
        } finally {
            setIsLoading(false);
        }
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleSendMessage();
        }
    };

    return (
        <div className="chat-widget-container">
            <button
                className={`chat-toggle-btn ${isOpen ? 'open' : ''}`}
                onClick={toggleChat}
                aria-label="Toggle chat"
            >
                {isOpen ? <FaTimes size={24} /> : <FaComments size={28} />}
            </button>

            {isOpen && (
                <div className="chat-window card shadow-lg">
                    <div className="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                        <div className="d-flex align-items-center gap-2">
                            {showPrompts ? (
                                <button className="btn btn-link text-white p-0" onClick={() => { setShowPrompts(false); setSelectedPrompt(null); }}>
                                    <FaChevronLeft size={18} />
                                </button>
                            ) : <FaRobot size={20} />}
                            <span className="fw-bold fs-5">{showPrompts ? 'MCP Prompts' : 'AI Assistant'}</span>
                        </div>
                        <span className="badge bg-success rounded-pill">Online</span>
                    </div>

                    <div className="card-body chat-messages p-3">
                        {showPrompts ? (
                            <div className="prompts-list">
                                {!selectedPrompt ? (
                                    <>
                                        <p className="text-muted small mb-3">Choose a specialized prompt template:</p>
                                        {prompts.map(p => (
                                            <div key={p.name} className="prompt-item card mb-2 p-2 shadow-sm" onClick={() => handlePromptSelect(p)}>
                                                <div className="d-flex justify-content-between align-items-center">
                                                    <strong>{p.name}</strong>
                                                    <FaMagic className="text-primary small" />
                                                </div>
                                                <small className="text-muted">{p.description}</small>
                                            </div>
                                        ))}
                                    </>
                                ) : (
                                    <div className="prompt-form">
                                        <h6 className="mb-3">{selectedPrompt.name}</h6>
                                        {selectedPrompt.arguments?.map(arg => (
                                            <div key={arg.name} className="mb-3">
                                                <label className="form-label small">{arg.name} {arg.required && <span className="text-danger">*</span>}</label>
                                                <input
                                                    type="text"
                                                    className="form-control form-control-sm"
                                                    placeholder={arg.description}
                                                    value={promptArgs[arg.name] || ""}
                                                    onChange={(e) => setPromptArgs(prev => ({ ...prev, [arg.name]: e.target.value }))}
                                                />
                                            </div>
                                        ))}
                                        <button
                                            className="btn btn-primary btn-sm w-100 mt-2"
                                            onClick={handleUsePrompt}
                                            disabled={selectedPrompt.arguments?.some(a => a.required && !promptArgs[a.name])}
                                        >
                                            Generate with AI
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <>
                                {messages.map((msg, index) => (
                                    <div key={index} className={`message-wrapper ${msg.isUser ? 'user' : 'bot'} mb-3`}>
                                        <div className={`message-bubble ${msg.isUser ? 'bg-primary text-white' : 'bg-light text-dark border'}`}>
                                            <ReactMarkdown>{msg.text}</ReactMarkdown>
                                        </div>
                                        <div className="message-icon mt-1">
                                            {msg.isUser ? <FaUser size={12} className="text-muted" /> : <FaRobot size={12} className="text-primary" />}
                                        </div>
                                    </div>
                                ))}
                                {isLoading && (
                                    <div className="message-wrapper bot mb-3">
                                        <div className="message-bubble bg-light text-muted fst-italic">
                                            <span className="typing-dots">Thinking...</span>
                                        </div>
                                    </div>
                                )}
                                <div ref={messagesEndRef} />
                            </>
                        )}
                    </div>

                    <div className="card-footer p-3 bg-white border-top">
                        <div className="input-group">
                            <button
                                className={`btn btn-outline-secondary border-end-0 ${showPrompts ? 'active' : ''}`}
                                onClick={() => setShowPrompts(!showPrompts)}
                                title="Use MCP Prompts"
                            >
                                <FaMagic />
                            </button>
                            <input
                                type="text"
                                className="form-control border-start-0 border-end-0"
                                placeholder="Ask about products..."
                                value={inputValue}
                                onChange={(e) => setInputValue(e.target.value)}
                                onKeyPress={handleKeyPress}
                                disabled={isLoading || showPrompts}
                            />
                            <button
                                className="btn btn-primary border-start-0"
                                type="button"
                                onClick={() => handleSendMessage()}
                                disabled={isLoading || showPrompts || !inputValue.trim()}
                            >
                                <FaPaperPlane />
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default ChatBox;

