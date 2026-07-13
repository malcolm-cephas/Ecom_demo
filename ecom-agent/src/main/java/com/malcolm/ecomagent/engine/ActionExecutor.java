package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Service responsible for executing external actions against the core e-commerce backend.
 * Uses WebClient to make REST calls for fetching wishlists, checking stock, and searching products.
 */
@SuppressWarnings("unused")
@Service
public class ActionExecutor
{

    private final WebClient webClient;

    public ActionExecutor(WebClient.Builder webClientBuilder, @Value("${app.backend.url}") String backendUrl) 
    {
        this.webClient = webClientBuilder.baseUrl(backendUrl).build();
    }

    /**
     * Returns a predefined list of product categories that are supported by the mock wishlist.
     */
    public java.util.List<String> getSupportedWishlistCategories() 
    {
        return java.util.Arrays.asList("smartphone", "phone", "laptop", "tv", "headphones", "gaming", "console");
    }

    /**
     * Fetches the favorites/wishlist for a specific user from the backend service.
     */
    public String fetchWishlist(String userId) 
    {
        try 
        {
            return webClient.get()
                    .uri("/favorites")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } 
        catch (Exception e) 
        {
            return "Failed to fetch wishlist: " + e.getMessage();
        }
    }

    /**
     * Checks the stock status of a specific product by its ID via the backend service.
     */
    public String checkStock(String productId) 
    {
        try 
        {
            return webClient.get()
                    .uri("/product/" + productId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } 
        catch (Exception e) 
        {
            return "Failed to check stock: " + e.getMessage();
        }
    }

    /**
     * Retrieves currently active global promotional offers.
     */
    public String fetchOffers() 
    {
        return "GLOBAL OFFER: 10% off on all electronics today.";
    }

    /**
     * Searches the backend catalog for products matching a specific keyword.
     */
    public String searchProducts(String keyword) 
    {
        try 
        {
            return webClient.get()
                    .uri("/products/search?keyword=" + keyword)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } 
        catch (Exception e) 
        {
            return "Failed to search products: " + e.getMessage();
        }
    }

    /**
     * Adds a specific product to the user's cart in the backend service.
     */
    public String addToCart(String userId, String productId, double price) 
    {
        try 
        {
            // ecom-proj expects productId and quantity as request params
            return webClient.post()
                    .uri("/cart/add?productId=" + productId + "&quantity=1&targetUserId=" + userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } 
        catch (Exception e) 
        {
            return "Failed to add to cart in backend: " + e.getMessage();
        }
    }
}
