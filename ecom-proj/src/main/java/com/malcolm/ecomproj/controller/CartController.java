package com.malcolm.ecomproj.controller;

import com.malcolm.ecomproj.model.Cart;
import com.malcolm.ecomproj.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam int productId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addToCart(productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable int productId) {
        return ResponseEntity.ok(cartService.removeFromCart(productId));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable int productId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(productId, quantity));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart() {
        cartService.clearCart();
        return ResponseEntity.ok("Cart cleared");
    }
}
