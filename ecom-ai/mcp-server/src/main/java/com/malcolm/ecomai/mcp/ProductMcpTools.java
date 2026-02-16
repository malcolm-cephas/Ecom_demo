package com.malcolm.ecomai.mcp;

import com.malcolm.ecomai.model.Product;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Component that defines tools for product management.
 * These methods are exposed to the AI via the Model Context Protocol (MCP).
 * Now connects to the Spring Boot backend via REST.
 */
@Component
public class ProductMcpTools {

    @Autowired
    private BackendClient backendClient;

    public record ProductInfo(
            int id,
            String name,
            String description,
            String brand,
            BigDecimal price,
            String category,
            boolean available,
            int stockQuantity,
            Date releaseDate,
            boolean favorite) {
    }

    private ProductInfo mapToInfo(Product product) {
        return new ProductInfo(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getPrice(),
                product.getCategory(),
                product.isAvailable(),
                product.getStockQuantity(),
                product.getReleaseDate(),
                product.isFavorite());
    }

    @Tool(description = "Search for products by a keyword or phrase")
    public List<ProductInfo> searchProducts(String keyword) {
        return backendClient.searchProducts(keyword)
                .stream()
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    @Tool(description = "Get details of a specific product by its ID")
    public ProductInfo getProductDetails(int id) {
        Product product = backendClient.getProduct(id);
        if (product != null) {
            return mapToInfo(product);
        }
        return null;
    }

    @Tool(description = "List all available products")
    public List<ProductInfo> listAllProducts() {
        return backendClient.getAllProducts()
                .stream()
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    @Tool(description = "Toggle favorite status of a product by its ID")
    public String toggleFavorite(int id) {
        try {
            backendClient.toggleFavorite(id);
            return "Successfully toggled favorite for product with ID: " + id;
        } catch (Exception e) {
            return "Failed to toggle favorite: " + e.getMessage();
        }
    }

    @Tool(description = "Check stock availability for a product by name. Returns the available quantity.")
    public String checkStock(String productName) {
        List<Product> products = backendClient.searchProducts(productName);
        if (products.isEmpty()) {
            return "Product not found: " + productName;
        }
        StringBuilder sb = new StringBuilder();
        for (Product product : products) {
            sb.append(String.format("Product: %s (ID: %d) - Stock: %d\n", product.getName(), product.getId(),
                    product.getStockQuantity()));
        }
        return sb.toString();
    }

    @Tool(description = "Add a product to the cart after verifying stock. Requires product name and quantity.")
    public String addToCart(String productName, String quantity) {
        int quantityInt;
        try {
            quantityInt = Integer.parseInt(quantity);
        } catch (NumberFormatException e) {
            return "FAILURE: Quantity must be a valid number.";
        }

        List<Product> products = backendClient.searchProducts(productName);
        if (products.isEmpty()) {
            return "Product not found: " + productName;
        }

        // Use the first match
        Product product = products.get(0);

        if (product.getStockQuantity() >= quantityInt) {
            try {
                backendClient.addToCart(product.getId(), quantityInt);
                return String.format("SUCCESS: Added %d x %s to cart. (Stock remaining: %d)", quantityInt,
                        product.getName(), product.getStockQuantity() - quantityInt);
            } catch (Exception e) {
                return "Error adding to cart: " + e.getMessage();
            }
        } else {
            return String.format("FAILURE: Insufficient stock for %s. Requested: %d, Available: %d", product.getName(),
                    quantityInt, product.getStockQuantity());
        }
    }
}
