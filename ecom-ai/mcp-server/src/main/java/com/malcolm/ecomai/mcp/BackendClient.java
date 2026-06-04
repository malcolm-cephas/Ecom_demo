package com.malcolm.ecomai.mcp;

import com.malcolm.ecomai.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Client service that communicates with the main E-commerce Backend
 * (ecom-proj).
 * <p>
 * This class uses Spring WebClient to perform REST calls to the backend API
 * running
 * on a different port (typically 8080). It acts as a data fetcher for the MCP
 * tools.
 * </p>
 */
@Service
public class BackendClient {

        // Non-blocking, reactive web client for making HTTP requests
        private final WebClient webClient;

        public BackendClient(WebClient.Builder webClientBuilder, @Value("${app.backend.url}") String backendUrl) {
                // Initialize WebClient with the base URL from application.properties
                // and increase memory buffer to handle large JSON responses (10MB)
                this.webClient = webClientBuilder
                                .baseUrl(backendUrl)
                                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                                .build();
        }

        /**
         * Fetches the list of all available products.
         * 
         * @return List of Product objects, or empty list if none found.
         */
        public List<Product> getAllProducts() {
                // GET /products
                return webClient.get()
                                .uri("/products")
                                .retrieve()
                                .bodyToFlux(Product.class) // Convert stream of JSON objects to Flux<Product>
                                .collectList() // Collect all items into a List
                                .block(); // Block execution to return synchronous result (needed for MCP tools)
        }

        public Product getProduct(int id) {
                return webClient.get()
                                .uri("/product/{id}", id)
                                .retrieve()
                                .bodyToMono(Product.class)
                                .block();
        }

        /**
         * Searches for products matching a keyword.
         * 
         * @param keyword The search term (e.g., "laptop", "shoes")
         * @return List of matching products
         */
        public List<Product> searchProducts(String keyword) {
                // GET /products/search?keyword={keyword}
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/products/search")
                                                .queryParam("keyword", keyword)
                                                .build())
                                .retrieve()
                                .bodyToFlux(Product.class)
                                .collectList()
                                .block();
        }

        public void toggleFavorite(int id) {
                webClient.put()
                                .uri("/product/{id}/favorite", id)
                                .retrieve()
                                .toBodilessEntity()
                                .block();
        }

        /**
         * Adds a product to the user's cart.
         * 
         * @param productId The ID of the product
         * @param quantity  The number of items to add
         */
        public void addToCart(int productId, int quantity) {
                // POST /cart/add?productId={id}&quantity={qty}
                webClient.post()
                                .uri(uriBuilder -> uriBuilder.path("/cart/add")
                                                .queryParam("productId", productId)
                                                .queryParam("quantity", quantity)
                                                .build())
                                .retrieve()
                                .toBodilessEntity() // We don't expect a response body, just 200 OK
                                .block();
        }

        public List<Product> getLowStockProducts(int threshold) {
                return webClient.get()
                                .uri(uriBuilder -> uriBuilder.path("/products/low-stock")
                                                .queryParam("threshold", threshold)
                                                .build())
                                .retrieve()
                                .bodyToFlux(Product.class)
                                .collectList()
                                .block();
        }

        public List<Product> getOutOfStockProducts() {
                return webClient.get()
                                .uri("/products/out-of-stock")
                                .retrieve()
                                .bodyToFlux(Product.class)
                                .collectList()
                                .block();
        }
}
