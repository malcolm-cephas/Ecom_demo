package com.malcolm.ecomai.config;

import com.malcolm.ecomai.mcp.DataAnalyticsTools;
import com.malcolm.ecomai.mcp.ProductMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class to register tools with the MCP (Model Context Protocol)
 * server.
 * This class ensures that Spring AI can discover and expose the methods
 * annotated with @Tool.
 */
@Configuration
public class McpConfig {

    /**
     * ToolCallbackProvider is a central bean that holds references to all tool
     * objects.
     * The MCP server automatically looks for this bean to populate its tool list.
     */
    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider(ProductMcpTools productMcpTools,
            DataAnalyticsTools dataAnalyticsTools) {
        // MethodToolCallbackProvider scan the provided beans for @Tool annotations
        // and creates the necessary metadata for the MCP protocol.
        return MethodToolCallbackProvider.builder()
                .toolObjects(productMcpTools, dataAnalyticsTools)
                .build();
    }
}
