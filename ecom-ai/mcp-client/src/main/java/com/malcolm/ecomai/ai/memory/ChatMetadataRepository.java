package com.malcolm.ecomai.ai.memory;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ChatMetadataRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatMetadataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String createChat(String userId, String description) {
        String chatId = UUID.randomUUID().toString();
        String sql = "INSERT INTO chat_metadata (conversation_id, user_id, description) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, chatId, userId, description);
        return chatId;
    }

    public boolean chatIdExists(String chatId) {
        String sql = "SELECT COUNT(*) FROM chat_metadata WHERE conversation_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, chatId);
        return count != null && count == 1;
    }

    public List<ChatMetadata> getAllChatsForUser(String userId) {
        String sql = "SELECT conversation_id, description FROM chat_metadata WHERE user_id = ? ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new ChatMetadata(rs.getString("conversation_id"), rs.getString("description")), userId);
    }

    public List<ChatMessage> getChatMessages(String chatId) {
        String sql = "SELECT content, type FROM spring_ai_chat_memory WHERE conversation_id = ? ORDER BY timestamp ASC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> 
            new ChatMessage(rs.getString("content"), rs.getString("type")), chatId);
    }
}
