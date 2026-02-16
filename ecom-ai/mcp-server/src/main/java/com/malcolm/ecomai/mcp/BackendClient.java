package com.malcolm.ecomai.mcp;

import com.malcolm.ecomai.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class BackendClient {

    private final WebClient webClient;

    public BackendClient(WebClient.Builder webClientBuilder, @Value("${app.backend.url}") String backendUrl) {
        this.webClient = webClientBuilder
                .baseUrl(backendUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }

    public List<Product> getAllProducts() {
        return webClient.get()
                .uri("/products")
                .retrieve()
                .bodyToFlux(Product.class)
                .collectList()
                .block();
    }

    public Product getProduct(int id) {
        return webClient.get()
                .uri("/product/{id}", id)
                .retrieve()
                .bodyToMono(Product.class)
                .block();
    }

    public List<Product> searchProducts(String keyword) {
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

    public void addToCart(int productId, int quantity) {
        webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/cart/add")
                        .queryParam("productId", productId)
                        .queryParam("quantity", quantity)
                        .build())
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
