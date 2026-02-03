package com.malcolm.ecomproj.mcp;

import com.malcolm.ecomproj.model.Product;
import com.malcolm.ecomproj.service.ProductService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductMcpTools {

    @Autowired
    private ProductService productService;

    /**
     * Light-weight record to avoid sending heavy image data to the LLM.
     */
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
        return productService.searchProducts(keyword)
                .stream()
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }

    @Tool(description = "Get details of a specific product by its ID")
    public ProductInfo getProductDetails(int id) {
        Product product = productService.getProduct(id);
        if (product != null) {
            return mapToInfo(product);
        }
        return null;
    }

    @Tool(description = "List all available products")
    public List<ProductInfo> listAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(this::mapToInfo)
                .collect(Collectors.toList());
    }
}
