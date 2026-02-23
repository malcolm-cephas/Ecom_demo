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
 */
@Configuration
public class McpConfig {

    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider(ProductMcpTools productMcpTools,
            DataAnalyticsTools dataAnalyticsTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(productMcpTools, dataAnalyticsTools)
                .build();
    }

    // TODO: Register prompts from McpPromptProvider once the correct Registration
    // class is identified for Spring AI 1.1.0-M1
}
