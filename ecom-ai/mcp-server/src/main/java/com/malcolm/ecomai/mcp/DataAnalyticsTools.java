package com.malcolm.ecomai.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Component that defines tools for data analytics and system utilities.
 * These methods allow the AI to perform complex queries and retrieve system
 * state.
 */
@Component
public class DataAnalyticsTools {

    private static final Logger logger = LoggerFactory.getLogger(DataAnalyticsTools.class);

    public DataAnalyticsTools() {
    }

    @Tool(description = "Get user activity summary from database")
    public UserActivitySummary getUserActivity(String userId, String startDate, String endDate) {
        logger.info("Getting user activity for: {}", userId);

        // Mock implementation since the backend doesn't have an activity API yet
        // In a real scenario, this would call backendClient.getUserActivity(userId,
        // startDate, endDate)
        return new UserActivitySummary(userId, 5, 100);
    }

    @Tool(description = "Get current system time. Optional: provide a format like 'yyyy-MM-dd HH:mm:ss'")
    public String getCurrentTime(String format) {
        try {
            String pattern = (format == null || format.isBlank() || format.equals("format")) ? "yyyy-MM-dd HH:mm:ss"
                    : format;
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    + " (Note: fall back to default as there was an issue with format '" + format + "': "
                    + e.getMessage() + ")";
        }
    }

    public static class UserActivitySummary {
        private String userId;
        private int totalActions;
        private int totalScore;

        public UserActivitySummary(String userId, int totalActions, int totalScore) {
            this.userId = userId;
            this.totalActions = totalActions;
            this.totalScore = totalScore;
        }

        public String getUserId() {
            return userId;
        }

        public int getTotalActions() {
            return totalActions;
        }

        public int getTotalScore() {
            return totalScore;
        }
    }
}
