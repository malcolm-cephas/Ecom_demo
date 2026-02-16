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

    // Use a fixed session ID for this demo
    private static final String DEFAULT_SESSION_ID = "default_session";

    @Transactional
    public Cart getCart() {
        Cart cart = cartRepo.findBySessionId(DEFAULT_SESSION_ID);
        if (cart == null) {
            System.out.println("Cart not found for session " + DEFAULT_SESSION_ID + ", creating new one.");
            cart = new Cart();
            cart.setSessionId(DEFAULT_SESSION_ID);
            cart = cartRepo.save(cart);
        } else {
            System.out
                    .println("Found existing cart id=" + cart.getId() + " with " + cart.getItems().size() + " items.");
        }
        return cart;
    }

    @Transactional
    public Cart addToCart(int productId, int quantity) {
        System.out.println("Adding product " + productId + " quantity " + quantity + " to cart.");
        Cart cart = getCart();
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            System.out.println("Updated quantity for product " + productId + " to " + item.getQuantity());
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
            System.out.println("Added new item for product " + productId);
        }

        Cart savedCart = cartRepo.save(cart);
        System.out.println("Cart saved. Total items: " + savedCart.getItems().size());
        return savedCart;
    }

    @Transactional
    public Cart removeFromCart(int productId) {
        Cart cart = getCart();
        cart.getItems().removeIf(item -> item.getProduct().getId() == productId);
        return cartRepo.save(cart);
    }

    @Transactional
    public Cart updateQuantity(int productId, int newQuantity) {
        if (newQuantity <= 0) {
            return removeFromCart(productId);
        }
        Cart cart = getCart();
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
    public void clearCart() {
        Cart cart = getCart();
        cart.getItems().clear();
        cartRepo.save(cart);
    }
}
