package com.malcolm.ecomagent.engine;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ActionExecutor {

    private final WebClient webClient;

    public ActionExecutor(WebClient.Builder webClientBuilder, @Value("${app.backend.url}") String backendUrl) {
        this.webClient = webClientBuilder.baseUrl(backendUrl).build();
    }

    public String fetchWishlist(String userId) {
        try {
            // Need to pass user context somehow. For now, since it's an internal agent,
            // we'll simulate passing the user context or just hitting a public endpoint.
            // In a real system, the agent might have a service token.
            // For the demo, we assume the backend has an endpoint or we mock it.
            
            // To prevent blocking forever in the prototype, we'll try hitting the endpoint
            // but return a fallback string if it fails (e.g. auth required).
            return webClient.get()
                    .uri("/favorites")
                    // .header("Authorization", "Bearer " + getAgentToken()) // Not implemented
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "Failed to fetch wishlist: " + e.getMessage() + ". Suggest checking DB directly or bypassing auth for agent.";
        }
    }

    public String checkStock(String productId) {
        try {
            return webClient.get()
                    .uri("/product/" + productId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "Failed to check stock: " + e.getMessage();
        }
    }

    public String fetchOffers() {
        return "GLOBAL OFFER: 10% off on all electronics today.";
    }

    public String searchProducts(String keyword) {
        try {
            return webClient.get()
                    .uri("/products/search?keyword=" + keyword)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "Failed to search products: " + e.getMessage();
        }
    }

    public String addToCart(String userId, String productId, double price) {
        try {
            // ecom-proj expects productId and quantity as request params
            return webClient.post()
                    .uri("/cart/add?productId=" + productId + "&quantity=1&targetUserId=" + userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "Failed to add to cart in backend: " + e.getMessage();
        }
    }
}
