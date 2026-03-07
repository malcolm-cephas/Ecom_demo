package com.malcolm.ecomproj.repo;

import com.malcolm.ecomproj.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepo extends JpaRepository<Favorite, Integer> {

    // Find a specific favorite entry for a user and product
    Optional<Favorite> findByUserIdAndProductId(String userId, int productId);

    // Get all favorites for a specific user
    List<Favorite> findByUserId(String userId);

    // Get all product IDs favorited by a user
    List<Favorite> findAllByUserId(String userId);

    // Delete a specific favorite
    void deleteByUserIdAndProductId(String userId, int productId);
}
