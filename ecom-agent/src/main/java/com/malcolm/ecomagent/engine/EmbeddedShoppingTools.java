package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.malcolm.ecomagent.model.ShoppingModels.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides @Tool annotated methods that the LLM can call directly via Spring AI.
 * Connected to the real backend API.
 */
@Component
public class EmbeddedShoppingTools {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.backend.url:http://localhost:8080/api}")
    private String backendUrl;

    @Tool(description = "Fetch all target items currently residing in the user's active shopping wishlist")
    public List<WishlistItem> getWishlist() {
        try {
            ResponseEntity<List<JsonNode>> response = restTemplate.exchange(
                    backendUrl + "/favorites",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<JsonNode>>() {}
            );
            List<JsonNode> products = response.getBody();
            if (products == null) return List.of();
            
            return products.stream()
                    .map(p -> new WishlistItem(
                            String.valueOf(p.get("id").asInt()),
                            p.get("name").asText(),
                            p.get("price").asDouble()
                    ))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching wishlist: " + e.getMessage());
            return List.of();
        }
    }

    @Tool(description = "Verify if a product is physically available in the warehouse inventory")
    public StockStatus checkStock(@ToolParam(description = "The item product SKU (ID), e.g. 1") String sku) {
        try {
            JsonNode product = restTemplate.getForObject(backendUrl + "/product/" + sku, JsonNode.class);
            if (product != null) {
                boolean available = product.get("available").asBoolean() && product.get("stockQuantity").asInt() > 0;
                return new StockStatus(sku, available);
            }
        } catch (Exception e) {
            System.err.println("Error checking stock for " + sku + ": " + e.getMessage());
        }
        return new StockStatus(sku, false);
    }

    @Tool(description = "Check if an item has an active promotional sale discount code applied")
    public OfferStatus checkOffers(@ToolParam(description = "The item product SKU (ID), e.g. 1") String sku) {
        try {
            JsonNode product = restTemplate.getForObject(backendUrl + "/product/" + sku, JsonNode.class);
            if (product != null) {
                double originalPrice = product.get("price").asDouble();
                // Since there is no active promo system in the backend, return original price without active offer
                return new OfferStatus(sku, false, originalPrice);
            }
        } catch (Exception e) {
             System.err.println("Error checking offers for " + sku + ": " + e.getMessage());
        }
        return new OfferStatus(sku, false, 0.0);
    }

    @Tool(description = "Retrieves the current items already placed in the user's shopping cart")
    public String getCart(@ToolParam(description = "The ID of the user, e.g. 1") String userId) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    backendUrl + "/cart?userId=" + userId,
                    HttpMethod.GET,
                    null,
                    String.class
            );
            return response.getBody();
        } catch (Exception e) {
            System.err.println("Error fetching cart: " + e.getMessage());
            return "Cart is empty or could not be retrieved.";
        }
    }

    @Tool(description = "Adds a specific product to the user's shopping cart")
    public String addToCart(
            @ToolParam(description = "The ID of the user, e.g. 1") String userId,
            @ToolParam(description = "The item product SKU (ID), e.g. 1") String productId) {
        try {
            String uri = backendUrl + "/cart/add?productId=" + productId + "&quantity=1&targetUserId=" + userId;
            ResponseEntity<String> response = restTemplate.postForEntity(uri, null, String.class);
            return "Successfully added product " + productId + " to cart.";
        } catch (Exception e) {
            System.err.println("Error adding to cart: " + e.getMessage());
            return "Failed to add product to cart.";
        }
    }

    @Tool(description = "Clears all items from the user's shopping cart to start over")
    public String clearCart(@ToolParam(description = "The ID of the user, e.g. 1") String userId) {
        try {
            String uri = backendUrl + "/cart/clear?userId=" + userId;
            restTemplate.delete(uri);
            return "Successfully cleared the shopping cart.";
        } catch (Exception e) {
             System.err.println("Error clearing cart: " + e.getMessage());
             return "Failed to clear the cart.";
        }
    }

    @Tool(description = "Searches the global product catalog (Shopify) for items not in the wishlist")
    public String searchGlobalCatalog(@ToolParam(description = "The search query or keyword") String query) {
        // Simulating a catalog response that the agent will pass back as a UI intent
        return "Found 1 result in Global Catalog. SKU: 'global-123', Name: 'Shopify Exclusive " + query + "', Price: 500. Instruction: Tell the user about this product and if they want to buy it, return a CATALOG_INTENT.";
    }

    @Tool(description = "Generates a secure checkout URL for the current cart contents using Checkout Kit")
    public String generateCheckoutUrl(@ToolParam(description = "The ID of the user, e.g. 1") String userId) {
        // Simulating the creation of a checkout URL
        String checkoutUrl = "https://checkout.secure-commerce.com/" + userId + "/pay?session=" + java.util.UUID.randomUUID().toString().substring(0, 8);
        return "Checkout URL generated: " + checkoutUrl + ". Instruction: Return a CHECKOUT_INTENT to the user with this URL.";
    }
}
