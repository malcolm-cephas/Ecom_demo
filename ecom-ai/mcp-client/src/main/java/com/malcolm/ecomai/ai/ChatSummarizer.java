package com.malcolm.ecomai.ai;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to summarize conversation history when it exceeds token limits.
 * This helps the AI maintain long-term context compactly.
 */
@Service
public class ChatSummarizer {

    private final ChatModel chatModel;

    public ChatSummarizer(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String summarize(List<Message> messages) {
        // Construct raw conversation text for the summarizer
        String conversation = messages.stream()
                .map(m -> m.getMessageType().name() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        String summaryPrompt = """
                Summarize the following conversation history concisely while preserving core context.
                Focus on:
                - Key facts discussed
                - User preferences and intents
                - Decisions or conclusions made
                Ensure the summary is under 150 words.
                """;

        // Call the AI model directly to generate the summary
        var response = chatModel.call(new Prompt(List.of(
                new SystemMessage(summaryPrompt),
                new SystemMessage("CONVERSATION TO SUMMARIZE:\n" + conversation)
        )));

        return response.getResult().getOutput().getText();
    }
}
