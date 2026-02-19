package com.malcolm.ecomai.mcp;

import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.stereotype.Component;

/**
 * MCP Prompt Provider that exposes reusable prompt templates to AI clients.
 * These prompts provide structured instructions for complex tasks like product
 * comparison.
 */
@Component
public class McpPromptProvider {

        /**
         * Prompt for comparing two products in detail.
         * Provides a professional comparison template for the AI.
         */
        @McpPrompt(name = "compareProducts", description = "Compare two products side-by-side with detailed analysis of price, features, and stock availability")
        public String compareProducts(
                        @McpArg(name = "productId1", description = "First product ID to compare", required = true) String productId1,
                        @McpArg(name = "productId2", description = "Second product ID to compare", required = true) String productId2) {

                // NOTE: We accept Strings and parse them manually to prevent JSON/Type
                // mismatches
                // from the frontend. This ensures robustness when "123" comes as a string.
                long p1;
                long p2;
                try {
                        p1 = Long.parseLong(productId1);
                        p2 = Long.parseLong(productId2);
                } catch (NumberFormatException e) {
                        // Fallback or rethrow gracefully
                        throw new IllegalArgumentException("Product IDs must be valid numbers");
                }

                return String.format(
                                /**
                                 * We use a text block for the prompt template.
                                 * %d placeholders are replaced by the parsed IDs.
                                 */
                                """
                                                You are an expert product comparison analyst for an e-commerce platform.

                                                Your task is to compare two products (IDs: %d and %d) in a professional, customer-friendly format.

                                                INSTRUCTIONS:
                                                1. Use the 'getProductDetails' tool to fetch complete information for both products.
                                                2. Create a side-by-side comparison covering:
                                                   - Product names and descriptions
                                                   - Price comparison (highlight the better value)
                                                   - Stock availability
                                                   - Key features and specifications
                                                3. Provide a clear recommendation based on:
                                                   - Best value for money
                                                   - Availability
                                                   - Feature set

                                                FORMAT YOUR RESPONSE AS:

                                                ## Product Comparison

                                                ### Product A: [Name]
                                                - Price: ₹X.XX
                                                - Stock: [Available/Out of Stock]
                                                - Key Features: [List]

                                                ### Product B: [Name]
                                                - Price: ₹X.XX
                                                - Stock: [Available/Out of Stock]
                                                - Key Features: [List]

                                                ### Recommendation
                                                [Your expert recommendation with reasoning]

                                                Be concise, professional, and customer-focused.
                                                """,
                                p1, p2);
        }

        /**
         * Prompt for acting as a helpful shopping assistant.
         * Provides general guidance and product recommendations.
         */
        @McpPrompt(name = "shoppingAssistant", description = "Act as a helpful shopping assistant to guide customers through product discovery and selection")
        public String shoppingAssistant() {
                return """
                                You are a friendly and knowledgeable shopping assistant for an e-commerce platform.

                                Your role is to:
                                1. Help customers find products that match their needs
                                2. Answer questions about product features, pricing, and availability
                                3. Explain store policies (shipping, returns, warranty) using the 'getStorePolicies' tool
                                4. Provide honest recommendations based on customer requirements
                                5. Use the available tools to search and retrieve accurate product information

                                GUIDELINES:
                                - Always use the 'searchProducts' or 'listAllProducts' tools to get current data
                                - Use 'getStorePolicies' whenever a customer asks about returns, shipping, or business rules
                                - Be conversational and helpful, not robotic
                                - If a product is out of stock, suggest alternatives
                                - Highlight good deals and value propositions
                                - Ask clarifying questions when customer needs are unclear

                                Remember: Your goal is to help customers make informed purchasing decisions!
                                """;
        }

        /**
         * Prompt for finding gift recommendations based on criteria.
         */
        @McpPrompt(name = "giftFinder", description = "Help customers find the perfect gift based on recipient, budget, and occasion")
        public String giftFinder(
                        @McpArg(name = "recipient", description = "Who is the gift for? (e.g., 'tech enthusiast', 'fitness lover')", required = true) String recipient,
                        @McpArg(name = "budget", description = "Maximum budget in rupees", required = false) String budget,
                        @McpArg(name = "occasion", description = "What's the occasion? (e.g., birthday, anniversary)", required = false) String occasion) {

                // Parse the optional budget parameter safely
                Double budgetVal = (budget != null && !budget.isBlank()) ? Double.parseDouble(budget) : null;

                // Build the prompt dynamically based on which optional arguments are present
                StringBuilder prompt = new StringBuilder("""
                                You are a professional gift consultant helping a customer find the perfect gift.

                                CUSTOMER REQUIREMENTS:
                                """);

                // Always add recipient
                prompt.append(String.format("- Recipient: %s\n", recipient));

                // Add budget only if provided
                if (budgetVal != null) {
                        prompt.append(String.format("- Budget: Up to ₹%.2f\n", budgetVal));
                }
                if (occasion != null && !occasion.isBlank()) {
                        prompt.append(String.format("- Occasion: %s\n", occasion));
                }

                prompt.append("""

                                YOUR TASK:
                                1. Use 'searchProducts' or 'listAllProducts' to find suitable gift options
                                2. Consider the recipient's interests and the occasion
                                3. Stay within the budget (if specified)
                                4. Present 2-3 thoughtful recommendations

                                FORMAT YOUR RESPONSE:

                                ## Gift Recommendations for [Recipient]

                                ### Option 1: [Product Name]
                                - Price: ₹X.XX
                                - Why it's perfect: [Reasoning]
                                - Stock: [Available/Limited]

                                ### Option 2: [Product Name]
                                - Price: ₹X.XX
                                - Why it's perfect: [Reasoning]
                                - Stock: [Available/Limited]

                                [Add Option 3 if applicable]

                                ### Final Thoughts
                                [Brief summary and your top pick]

                                Be enthusiastic and help the customer feel confident in their gift choice!
                                """);

                return prompt.toString();
        }

        /**
         * Prompt for providing deep technical specifications and usage advice.
         */
        @McpPrompt(name = "techSpecExpert", description = "Get deep-dive technical insights and specialized advice for high-tech electronics")
        public String techSpecExpert(
                        @McpArg(name = "productName", description = "The name of the electronic device", required = true) String productName) {

                return String.format(
                                """
                                                You are a senior technical specialist for advanced consumer electronics.
                                                Your goal is to provide a comprehensive technical breakdown for: %s.

                                                INSTRUCTIONS:
                                                1. Use 'searchProducts' or 'getProductDetails' to find the specific item.
                                                2. Analyze its hardware specs, performance capabilities, and architectural details.
                                                3. Explain technical jargon (e.g., 'AMOLED', 'Bionic chip', 'latency') in a way that highlights the user benefit.
                                                4. Compare its specs against industry standards or common competitors.
                                                5. Provide a 'Pro Tip' for getting the best performance out of this device.

                                                FORMAT YOUR RESPONSE:
                                                ## Technical Deep Dive: %s

                                                ### Hardware & Performance
                                                [Detailed breakdown]

                                                ### Technology Explained
                                                [Explanation of key proprietary or high-end features]

                                                ### Technical Verdict
                                                [How it stands against the competition]

                                                ### Pro Tip
                                                [Practical usage advice]

                                                Be precise, technical, and authoritative.
                                                """,
                                productName, productName);
        }

        /**
         * Prompt for planning a shopping list within a strict budget.
         */
        @McpPrompt(name = "budgetPlanner", description = "Plan a shopping cart with multiple items while staying under a strict total budget")
        public String budgetPlanner(
                        @McpArg(name = "totalBudget", description = "Total available budget for the entire list", required = true) String totalBudget,
                        @McpArg(name = "categories", description = "Comma-separated list of categories or items needed", required = true) String categories) {

                // Convert string budget to double for formatting
                double budgetVal;
                try {
                        budgetVal = Double.parseDouble(totalBudget);
                } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Budget must be a valid number");
                }

                return String.format(
                                """
                                                You are a financial shopping strategist. Your goal is to maximize value for a customer with a total budget of ₹%.2f.
                                                The customer is looking for items in these categories: %s.

                                                Your Task:
                                                1. Use 'searchProducts' to find multiple items in each category.
                                                2. Select the best combination of items that fits under the ₹%.2f limit.
                                                3. Prioritize high-rated or 'Best Value' items if possible.
                                                4. Suggest where the customer can save money vs where they should spend a bit more.

                                                FORMAT:
                                                ## Budget Shopping Plan (Total: ₹%.2f)

                                                ### Suggested Items
                                                - Category/Item X: [Product Name] - ₹Y.YY
                                                - Category/Item Y: [Product Name] - ₹Z.ZZ

                                                ### Total Projected: ₹Total
                                                ### Remaining: ₹Balance

                                                ### Value Strategy
                                                [Explain why you chose these specific items for the budget]

                                                Ensure your total never exceeds the budget!
                                                """,
                                budgetVal, categories, budgetVal, budgetVal);
        }

        /**
         * Prompt for guiding customers on returns and shipping policies.
         */
        @McpPrompt(name = "returnPolicyGuide", description = "Explain shipping, returns, and warranty policies to customers")
        public String returnPolicyGuide() {
                return """
                                You are a Customer Success Specialist for the Ecommerce platform.
                                Your role is to explain our policies clearly and empathetically.

                                OUR POLICIES (Reference):
                                - Returns: 30-day window for most electronics/clothing. Must be in original packaging.
                                - Shipping: Standard (3-5 days) is ₹500. Free for orders over ₹5000.
                                - Warranty: 1-year limited manufacturer warranty on all major electronics.
                                - Restocking Fee: 10% for opened electronics.

                                YOUR TASK:
                                1. Answer the customer's question about how our service works.
                                2. Be reassuring and professional.
                                3. If they are asking about a specific product they bought, suggest they 'checkStock' or 'getProductDetails' for warranty specifics (if available).
                                4. Provide a step-by-step 'What's Next' section for their query.

                                Be friendly and minimize customer friction!
                                """;
        }
}
