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

    // Client to fetch data from the actual E-commerce backend
    @Autowired
    private BackendClient backendClient;

    // DTO to expose only relevant product fields to the AI, keeping context small
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

    /**
     * Tool: searchProducts
     * Allows the AI to find products based on natural language queries.
     * 
     * @param keyword The search string
     * @return List of simplified product info
     */
    @Tool(name = "searchProducts", description = "Search for products by a keyword or phrase")
    public List<ProductInfo> searchProducts(String keyword) {
        return backendClient.searchProducts(keyword)
                .stream()
                .limit(2) // Temporary limit for debugging
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
                .limit(5)
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

    /**
     * Tool: checkStock
     * Returns a human-readable string about stock levels.
     * The AI uses this to answer "do you have the iPhone 15 in stock?"
     */
    @Tool(description = "Check stock availability for a product by name. Returns the available quantity.")
    public String checkStock(String productName) {
        // Try exact match first, then fallback to removing spaces
        List<Product> products = searchProductsWithFallback(productName);
        if (products.isEmpty()) {
            return "Product not found: " + productName;
        }

        // Build a report for multiple matches
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

        List<Product> products = searchProductsWithFallback(productName);
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

    private List<Product> searchProductsWithFallback(String keyword) {
        List<Product> products = backendClient.searchProducts(keyword);
        if (products.isEmpty() && keyword.contains(" ")) {
            String fallbackKeyword = keyword.replace(" ", "");
            products = backendClient.searchProducts(fallbackKeyword);
        }
        return products;
    }

    /**
     * Tool: getStorePolicies
     * Provides a static knowledge base of store policies (static RAG).
     */
    @Tool(description = "Get the store policies regarding shipping, returns, and warranties")
    public String getStorePolicies() {
        return """
                Ecommerce Store Policies:
                - Returns: 30-day window for most electronics/clothing. Must be in original packaging.
                - Shipping: Standard (3-5 days) is ₹500. Free for orders over ₹5000.
                - Warranty: 1-year limited manufacturer warranty on all major electronics.
                - Restocking Fee: 10% for opened electronics (e.g., phones, laptops).
                - Currency: All prices are in Indian Rupees (₹).
                """;
    }
}
