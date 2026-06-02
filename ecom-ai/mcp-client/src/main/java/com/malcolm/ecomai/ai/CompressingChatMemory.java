package com.malcolm.ecomai.ai;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom ChatMemory implementation that compresses history when it exceeds a threshold.
 * It summarizes old messages into a single SystemMessage while keeping recent messages raw.
 */
public class CompressingChatMemory implements ChatMemory {

    private final JdbcChatMemoryRepository repository;
    private final ChatSummarizer summarizer;
    private final int threshold;

    public CompressingChatMemory(JdbcChatMemoryRepository repository, ChatSummarizer summarizer, int threshold) {
        this.repository = repository;
        this.summarizer = summarizer;
        this.threshold = threshold;
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // Retrieve current history
        List<Message> history = new ArrayList<>(repository.findByConversationId(conversationId));
        
        // Append new messages
        history.addAll(messages);

        if (history.size() >= threshold) {
            // Keep the last 5 messages for immediate context (continuity)
            List<Message> recentMessages = history.subList(history.size() - 5, history.size());
            
            // Summarize all messages except for the most recent 5
            List<Message> toSummarize = history.subList(0, history.size() - 5);
            String summaryText = summarizer.summarize(toSummarize);
            
            Message summaryMessage = new SystemMessage("PREVIOUS CONVERSATION CONTEXT (Compressed):\n" + summaryText);

            // Replace history with [Summary] + [Recent 5]
            List<Message> newHistory = new ArrayList<>();
            newHistory.add(summaryMessage);
            newHistory.addAll(recentMessages);
            
            repository.saveAll(conversationId, newHistory);
        } else {
            repository.saveAll(conversationId, history);
        }
    }

    @Override
    public List<Message> get(String conversationId) {
        return repository.findByConversationId(conversationId);
    }

    @Override
    public void clear(String conversationId) {
        repository.deleteByConversationId(conversationId);
    }
}
