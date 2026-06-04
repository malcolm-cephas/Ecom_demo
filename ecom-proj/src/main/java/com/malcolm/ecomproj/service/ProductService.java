package com.malcolm.ecomproj.service;

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
import java.util.Objects;

import com.malcolm.ecomproj.repo.FavoriteRepo;
import com.malcolm.ecomproj.model.Favorite;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepo repo;
    private final FavoriteRepo favoriteRepo;

    private void populateFavorites(List<Product> products, String userId) {
        if (userId == null || userId.trim().isEmpty())
            return;
        List<Favorite> favorites = favoriteRepo.findByUserId(userId);
        Set<Integer> favoriteIds = favorites.stream()
                .map(f -> f.getProduct().getId())
                .collect(Collectors.toSet());
        products.forEach(p -> p.setFavorite(favoriteIds.contains(p.getId())));
    }

    private Product populateFavorite(Product product, String userId) {
        if (product != null && userId != null && !userId.trim().isEmpty()) {
            Optional<Favorite> favorite = favoriteRepo.findByUserIdAndProductId(userId, product.getId());
            product.setFavorite(favorite.isPresent());
        }
        return product;
    }

    public List<Product> getAllProducts(String userId) {
        List<Product> products = repo.findAll();
        populateFavorites(products, userId);
        return products;
    }

    public Page<Product> getAllProducts(int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> pageResult = repo.findAll(pageable);
        populateFavorites(pageResult.getContent(), userId);
        return pageResult;
    }

    public Page<Product> getAllProducts(int page, int size, String sortBy, Sort.Direction direction, String userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Objects.requireNonNull(direction), sortBy));
        Page<Product> pageResult = repo.findAll(pageable);
        populateFavorites(pageResult.getContent(), userId);
        return pageResult;
    }

    public Product getProduct(int id, String userId) {
        Product product = repo.findById(id).orElse(null);
        return populateFavorite(product, userId);
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

    public Product updateStock(int id, int stock) {
        Product product = repo.findById(id).orElse(null);
        if (product != null) {
            product.setStockQuantity(stock);
            // Auto update availability if stock is positive
            if (stock > 0 && !product.isAvailable()) {
                product.setAvailable(true);
            } else if (stock == 0 && product.isAvailable()) {
                product.setAvailable(false);
            }
            return repo.save(product);
        }
        return null;
    }

    public void deleteProduct(int id) {
        repo.deleteById(id);
    }

    public List<Product> searchProducts(String keyword, String userId) {
        List<Product> products = repo.searchProducts(keyword);
        populateFavorites(products, userId);
        return products;
    }

    public Page<Product> searchProducts(String keyword, int page, int size, String userId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> pageResult = repo.searchProducts(keyword, pageable);
        populateFavorites(pageResult.getContent(), userId);
        return pageResult;
    }

    public Page<Product> searchProducts(String keyword, int page, int size, String sortBy, Sort.Direction direction,
            String userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Objects.requireNonNull(direction), sortBy));
        Page<Product> pageResult = repo.searchProducts(keyword, pageable);
        populateFavorites(pageResult.getContent(), userId);
        return pageResult;
    }

    public Product toggleFavorite(int id, String userId) {
        Product product = repo.findById(id).orElse(null);
        if (product == null || userId == null || userId.trim().isEmpty()) {
            return null;
        }

        Optional<Favorite> existingFavorite = favoriteRepo.findByUserIdAndProductId(userId, id);

        if (existingFavorite.isPresent()) {
            // Already favorited, so remove it
            favoriteRepo.delete(existingFavorite.get());
            product.setFavorite(false);
        } else {
            // Not favorited, so add it
            Favorite newFavorite = new Favorite();
            newFavorite.setUserId(userId);
            newFavorite.setProduct(product);
            favoriteRepo.save(newFavorite);
            product.setFavorite(true);
        }

        return product;
    }

    public List<Product> getFavorites(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return List.of();
        }
        return favoriteRepo.findByUserId(userId).stream()
                .map(favorite -> {
                    Product p = favorite.getProduct();
                    p.setFavorite(true);
                    return p;
                })
                .collect(Collectors.toList());
    }

    public List<Product> getLowStockProducts(int threshold) {
        return repo.findByStockQuantityLessThanEqual(threshold);
    }


    public List<Product> getOutOfStockProducts() {
        return repo.findByStockQuantityLessThanEqual(0);
    }

}
