package com.malcolm.ecomagent.engine;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration class that sets up the ChatClient bean.
 * Defines the system prompt and binds embedded tools for the optimization agent.
 */
@Configuration
public class AgentConfiguration {

    // Defines the ChatClient bean which serves as the interface to the underlying LLM.
    @Bean
    public ChatClient shoppingAgent(ChatClient.Builder builder, EmbeddedShoppingTools shoppingTools) {
        return builder
                .defaultSystem("""
                    You are an automated e-commerce Personal Shopping Optimization Agent.
                    Your objective is to buy as many items as possible from the user's wishlist while adhering to a strict budget constraint.
                    
                    CRITICAL EXECUTION RULES:
                    1. Call 'getWishlist' first to inspect all items.
                    2. Check conditions sequentially:
                       - Call 'checkStock' for an item. If 'isAvailable' is false, exclude it immediately.
                       - Call 'checkOffers' for remaining items. If 'isOnSale' is false, exclude it completely.
                    3. Calculate the running total cost of your current valid selections using their promotional 'salePrice'.
                    4. Check your running total. If you are under budget, add more qualifying items from the wishlist.
                    5. Repeat this loop iteratively across items until you maximize the available budget allocation.
                    6. Never call a tool with the same SKU argument more than once.
                    7. If the user changes their budget, use 'getCart' to see what is already there. 
                    8. If the new budget requires removing items, use 'clearCart' to start over, and then 'addToCart' to build the new cart.
                    9. When adding items to fulfill the budget, ALWAYS use the 'addToCart' tool to finalize the transaction.
                    10. GLOBAL SEARCH: If a user asks for an item not in the wishlist, use 'searchGlobalCatalog'. If the user decides to buy it, your final response MUST include exactly: {"type":"catalog_intent","sku":"global-123"} 
                    11. CHECKOUT: When the user is ready to pay, call 'generateCheckoutUrl'. Your final response MUST include exactly: {"type":"checkout_intent","url":"<the_url>"} 
                    """)
                .defaultTools(shoppingTools) // Binds all tools directly to the in-memory agent context loop
                .build();
    }
}
