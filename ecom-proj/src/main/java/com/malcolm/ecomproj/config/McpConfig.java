package com.malcolm.ecomproj.config;

import com.malcolm.ecomproj.mcp.ProductMcpTools;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpConfig {

    @Bean
    public List<ToolCallback> productTools(ProductMcpTools productMcpTools) {
        // Registers all @Tool annotated methods in the ProductMcpTools bean
        return List.of(ToolCallbacks.from(productMcpTools));
    }
}
