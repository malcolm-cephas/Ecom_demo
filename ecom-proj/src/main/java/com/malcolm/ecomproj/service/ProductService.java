package com.malcolm.ecomproj.service;

import java.util.Objects;
import com.malcolm.ecomproj.model.Product;
import com.malcolm.ecomproj.repo.ProductRepo;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo repo;

    public List<Product> getAllProducts() {
        return repo.findAll();
    }

    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findAll(pageable);
    }

    public Page<Product> getAllProducts(int page, int size, String sortBy, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Objects.requireNonNull(direction), sortBy));
        return repo.findAll(pageable);
    }

    public Product getProduct(int id) {
        return repo.findById(id).orElse(null);
    }

    public Product addProduct(Product product, MultipartFile imageFile) throws IOException {
        product.setImageName(imageFile.getOriginalFilename());
        product.setImageType(imageFile.getContentType());
        product.setImageData(imageFile.getBytes());

        return repo.save(product);
    }

    public Product addProduct(Product product) {
        return Objects.requireNonNull(repo.save(product));
    }

    public Product updateProduct(int id, Product product, MultipartFile imageFile) throws IOException {
        product.setImageName(imageFile.getOriginalFilename());
        product.setImageType(imageFile.getContentType());
        product.setImageData(imageFile.getBytes());
        return repo.save(product);
    }

    public void deleteProduct(int id) {
        repo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword) {
        return repo.searchProducts(keyword);
    }

    public Page<Product> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.searchProducts(keyword, pageable);
    }

    public Page<Product> searchProducts(String keyword, int page, int size, String sortBy, Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Objects.requireNonNull(direction), sortBy));
        return repo.searchProducts(keyword, pageable);
    }

    public Product toggleFavorite(int id) {
        Product product = repo.findById(id).orElse(null);
        if (product != null) {
            product.setFavorite(!product.isFavorite());
            return repo.save(product);
        }
        return null;
    }

}
