package com.malcolm.ecomproj.controller;

import com.malcolm.ecomproj.model.Cart;
import com.malcolm.ecomproj.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@CrossOrigin
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam int productId, @RequestParam int quantity,
            Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.addToCart(authentication.getName(), productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable int productId, Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.removeFromCart(authentication.getName(), productId));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable int productId, @RequestParam int quantity,
            Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.updateQuantity(authentication.getName(), productId, quantity));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok("Cart cleared");
    }
}
