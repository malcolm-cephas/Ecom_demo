CREATE TABLE IF NOT EXISTS chat_metadata (
    conversation_id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(256),
    description VARCHAR(256),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
