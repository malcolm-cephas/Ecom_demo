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

    private String getUserId(Authentication auth) {
        return (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) ? auth.getName()
                : "testUser";
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(Authentication authentication) {
        String userId = getUserId(authentication);
        if (userId.equals("anonymousUser")) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/add")
    public ResponseEntity<Cart> addToCart(@RequestParam int productId, @RequestParam int quantity,
            @RequestParam(required = false) String targetUserId,
            Authentication authentication) {
        String userId = (targetUserId != null && !targetUserId.isEmpty()) ? targetUserId : getUserId(authentication);
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<Cart> removeFromCart(@PathVariable int productId, Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.removeFromCart(userId, productId));
    }

    @PutMapping("/update/{productId}")
    public ResponseEntity<Cart> updateQuantity(@PathVariable int productId, @RequestParam int quantity,
            Authentication authentication) {
        String userId = getUserId(authentication);
        return ResponseEntity.ok(cartService.updateQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(401).build();
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok("Cart cleared");
    }
}
