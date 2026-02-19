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
    /**
     * Registers the MCP Tools with the Spring AI framework.
     * 
     * @param productMcpTools    The bean containing product-related tools
     * @param dataAnalyticsTools The bean containing analytics tools
     * @return A provider that lists all available tools for the MCP server
     */
    @Bean
    public ToolCallbackProvider mcpToolCallbackProvider(ProductMcpTools productMcpTools,
            DataAnalyticsTools dataAnalyticsTools) {

        // MethodToolCallbackProvider scans the provided beans (productMcpTools,
        // dataAnalyticsTools)
        // looking for methods annotated with @Tool. It then generates the necessary
        // metadata (JSON Schema) that the Claude Desktop client needs to understand
        // how to call these tools.
        return MethodToolCallbackProvider.builder()
                .toolObjects(productMcpTools, dataAnalyticsTools)
                .build();
    }

    // Assuming there might be a need for PromptCallbackProvider, but if not found,
    // we just ensure the bean is scanned.
    // However, for MCP, usually we need to export it.
    // Let's try to add a generic method for prompts if available or just ensure the
    // component is there.
    // For now, I'll leave it as @Component in the provider class, but I will inject
    // it here to ensure it's loaded.

}
