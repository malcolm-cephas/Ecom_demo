package com.malcolm.ecomproj.repo;

import com.malcolm.ecomproj.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepo extends JpaRepository<Cart, Integer> {
    Cart findBySessionId(String sessionId);
}
