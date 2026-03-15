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
    const [isOpen, setIsOpen] = useState(false);
    const [view, setView] = useState('chat'); // 'chat', 'history', 'prompts'
    
    // Stores the history of chat messages for the current session
    const [messages, setMessages] = useState([
        { text: "Hi there! I'm your AI assistant. How can I help you today?", isUser: false }
    ]);

    const [inputValue, setInputValue] = useState("");
    const [isLoading, setIsLoading] = useState(false);

    // Chat History states
    const [chats, setChats] = useState([]);
    const [currentChatId, setCurrentChatId] = useState(null);

    // MCP Prompts states
    const [prompts, setPrompts] = useState([]);
    const [selectedPrompt, setSelectedPrompt] = useState(null);
    const [promptArgs, setPromptArgs] = useState({});

    const messagesEndRef = useRef(null);

    const toggleChat = () => {
        setIsOpen(!isOpen);
        if (!isOpen) {
            fetchChats();
        }
    };

    const fetchChats = async () => {
        try {
            const response = await axios.get('http://localhost:9090/api/ai/memory/chats');
            if (response.data) {
                setChats(response.data);
            }
        } catch (error) {
            console.error("Error fetching chats:", error);
        }
    };

    const fetchChatHistory = async (chatId) => {
        setIsLoading(true);
        try {
            const response = await axios.get(`http://localhost:9090/api/ai/memory/chat/${chatId}`);
            if (response.data) {
                // Map backend ChatMessage to frontend format
                const history = response.data.map(m => ({
                    text: m.content,
                    isUser: m.type === 'USER'
                }));
                setMessages(history);
                setCurrentChatId(chatId);
                setView('chat');
            }
        } catch (error) {
            console.error("Error fetching history:", error);
        } finally {
            setIsLoading(false);
        }
    };

    const fetchPrompts = async () => {
        try {
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

    const handleSendMessage = async (text = null) => {
        const messageText = text || inputValue;
        if (!messageText.trim()) return;

        const userMessage = { text: messageText, isUser: true };
        setMessages(prev => [...prev, userMessage]);
        setInputValue("");
        setIsLoading(true);

        try {
            let response;
            if (!currentChatId) {
                // If no chat is active, start a new one
                response = await axios.post('http://localhost:9090/api/ai/memory/start', {
                    message: messageText,
                    model: "llama-3.3-70b-versatile"
                });
                if (response.data && response.data.chatId) {
                    setCurrentChatId(response.data.chatId);
                    setMessages(prev => [...prev, { text: response.data.message, isUser: false }]);
                    // Refresh chat list to include the new one
                    fetchChats();
                }
            } else {
                // Continue existing chat
                response = await axios.post('http://localhost:9090/api/ai/chat', {
                    message: messageText,
                    model: "llama-3.3-70b-versatile",
                    conversationId: currentChatId
                });
                if (response.data && response.data.response) {
                    setMessages(prev => [...prev, { text: response.data.response, isUser: false }]);
                }
            }
        } catch (error) {
            console.error("Chat error:", error);
            setMessages(prev => [...prev, { text: "Network error. AI service might be down.", isUser: false }]);
        } finally {
            setIsLoading(false);
        }
    };

    const startNewChat = () => {
        setCurrentChatId(null);
        setMessages([{ text: "New conversation started. How can I help you?", isUser: false }]);
        setView('chat');
    };

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

    const handleUsePrompt = async () => {
        setIsLoading(true);
        try {
            const queryParams = new URLSearchParams(promptArgs).toString();
            const response = await axios.get(`http://localhost:9090/api/ai/prompts/${selectedPrompt.name}?${queryParams}`);
            if (response.data && response.data.messages && response.data.messages[0]) {
                const promptContent = response.data.messages[0].content.text;
                setView('chat');
                setSelectedPrompt(null);
                setPromptArgs({});
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
                            {view !== 'chat' ? (
                                <button className="btn btn-link text-white p-0" onClick={() => setView('chat')}>
                                    <FaChevronLeft size={18} />
                                </button>
                            ) : <FaRobot size={20} />}
                            <span className="fw-bold fs-5">
                                {view === 'history' ? 'Chat History' : view === 'prompts' ? 'MCP Prompts' : 'AI Assistant'}
                            </span>
                        </div>
                        <div className="d-flex gap-2">
                            <button className="btn btn-sm btn-outline-light border-0" onClick={() => { setView('history'); fetchChats(); }} title="History">
                                <FaComments />
                            </button>
                            <button className="btn btn-sm btn-outline-light border-0" onClick={startNewChat} title="New Chat">
                                <FaMagic />
                            </button>
                        </div>
                    </div>

                    <div className="card-body chat-messages p-3">
                        {view === 'history' ? (
                            <div className="history-list">
                                <p className="text-muted small mb-3">Your recent conversations:</p>
                                {chats.length === 0 ? <p className="text-center py-4">No recent chats found.</p> : (
                                    chats.map(chat => (
                                        <div key={chat.id} 
                                             className={`history-item card mb-2 p-2 shadow-sm ${currentChatId === chat.id ? 'active-chat' : ''}`} 
                                             onClick={() => fetchChatHistory(chat.id)}>
                                            <div className="d-flex justify-content-between align-items-start">
                                                <strong>{chat.description || 'Untitled Chat'}</strong>
                                                <small className="text-muted" style={{fontSize: '0.7rem'}}>ID: {chat.id.substring(0, 8)}</small>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        ) : view === 'prompts' ? (
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
                                className={`btn btn-outline-secondary border-end-0 ${view === 'prompts' ? 'active' : ''}`}
                                onClick={() => { if (view === 'prompts') setView('chat'); else { setView('prompts'); fetchPrompts(); } }}
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
                                disabled={isLoading || view !== 'chat'}
                            />
                            <button
                                className="btn btn-primary border-start-0"
                                type="button"
                                onClick={() => handleSendMessage()}
                                disabled={isLoading || view !== 'chat' || !inputValue.trim()}
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

