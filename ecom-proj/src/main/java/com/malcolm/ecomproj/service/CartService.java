package com.malcolm.ecomproj.service;

import com.malcolm.ecomproj.model.Cart;
import com.malcolm.ecomproj.model.CartItem;
import com.malcolm.ecomproj.model.Product;
import com.malcolm.ecomproj.repo.CartRepo;
import com.malcolm.ecomproj.repo.ProductRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepo cartRepo;
    private final ProductRepo productRepo;

    @Transactional
    public Cart getCart(String sessionId) {
        Cart cart = cartRepo.findBySessionId(sessionId);
        if (cart == null) {
            System.out.println("Cart not found for session " + sessionId + ", creating new one.");
            cart = new Cart();
            cart.setSessionId(sessionId);
            cart = cartRepo.save(cart);
        }
        return cart;
    }

    @Transactional
    public Cart addToCart(String sessionId, int productId, int quantity) {
        Cart cart = getCart(sessionId);
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }

        return cartRepo.save(cart);
    }

    @Transactional
    public Cart removeFromCart(String sessionId, int productId) {
        Cart cart = getCart(sessionId);
        cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart updateQuantity(String sessionId, int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(sessionId, productId);
        }
        Cart cart = getCart(sessionId);
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(newQuantity);
            return cartRepo.save(cart);
        }
        throw new RuntimeException("Product not in cart");
    }

    @Transactional
    public void clearCart(String sessionId) {
        Cart cart = getCart(sessionId);
        cart.getItems().clear();
        cartRepo.save(cart);
    }
}
