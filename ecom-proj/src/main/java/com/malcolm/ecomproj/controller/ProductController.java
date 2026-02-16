package com.malcolm.ecomproj.controller;

import java.util.Objects;
import com.malcolm.ecomproj.model.Product;
import com.malcolm.ecomproj.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for managing products.
 * Handles CRUD operations, image retrieval, searching, and pagination.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin // Allows the React frontend (different port) to access this API
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService service;

    /**
     * Retrieves all products available in the database.
     */
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return new ResponseEntity<>(service.getAllProducts(), HttpStatus.OK);
    }

    /**
     * Retrieves products with pagination and sorting support.
     */
    @GetMapping("/products/page")
    public ResponseEntity<Page<Product>> getAllProductsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(Objects.requireNonNull(sortDirection).toUpperCase());
        return new ResponseEntity<>(
                service.getAllProducts(page, size, Objects.requireNonNull(sortBy), Objects.requireNonNull(direction)),
                HttpStatus.OK);
    }

    /**
     * Gets details for a single product by its unique ID.
     */
    @GetMapping("/product/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable int id) {
        Product product = service.getProduct(id);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Adds a new product along with an image file.
     * Uses @RequestPart for multi-part form data processing.
     */
    @PostMapping("/product")
    public ResponseEntity<?> addProduct(@RequestPart @jakarta.validation.Valid Product product,
            @RequestPart MultipartFile imageFile) {
        try {
            Product product1 = service.addProduct(product, imageFile);
            return new ResponseEntity<>(product1, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error adding product: {}", e.getMessage(), e);
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Serves the raw image bytes for a specific product.
     * Sets the Content-Type header dynamically based on the stored image type.
     */
    @GetMapping("/product/{productId}/image")
    public ResponseEntity<byte[]> getImageByProductId(@PathVariable int productId) {
        Product product = service.getProduct(productId);
        byte[] imageFile = product.getImageData();

        if (imageFile != null && product.getImageType() != null) {
            return ResponseEntity.ok().contentType(MediaType.valueOf(Objects.requireNonNull(product.getImageType())))
                    .body(imageFile);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Updates an existing product's details and/or its image.
     */
    @PutMapping("/product/{id}")
    public ResponseEntity<String> updateProduct(@PathVariable int id,
            @RequestPart @jakarta.validation.Valid Product product,
            @RequestPart MultipartFile imageFile) {

        Product product1 = null;
        try {
            product1 = service.updateProduct(id, product, imageFile);
        } catch (IOException e) {
            log.error("Error updating product: {}", e.getMessage(), e);
            return new ResponseEntity<>("Failed to update", HttpStatus.BAD_REQUEST);
        }
        if (product1 != null) {
            return new ResponseEntity<>("updated", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Failed to update", HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Deletes a product from the database.
     */
    @DeleteMapping("/product/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable int id) {
        Product product = service.getProduct(id);
        if (product != null) {
            service.deleteProduct(id);
            return new ResponseEntity<>("Deleted", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Performs a keyword-based search across product names, brands, and
     * descriptions.
     */
    @GetMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        List<Product> products = service.searchProducts(keyword);
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Updates the stock quantity of a product.
     */
    @PatchMapping("/product/{id}/stock")
    public ResponseEntity<Product> updateStock(@PathVariable int id, @RequestBody int stock) {
        Product product = service.updateStock(id, stock);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/products/search/page")
    public ResponseEntity<Page<Product>> searchProductsPaginated(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "9") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(Objects.requireNonNull(sortDirection).toUpperCase());
        Page<Product> products = service.searchProducts(keyword, page, size, Objects.requireNonNull(sortBy),
                Objects.requireNonNull(direction));
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    /**
     * Toggles the favorite status of a product.
     */
    @PutMapping("/product/{id}/favorite")
    public ResponseEntity<Product> toggleFavorite(@PathVariable int id) {
        Product product = service.toggleFavorite(id);
        if (product != null) {
            return new ResponseEntity<>(product, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
