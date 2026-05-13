package com.example.demo.cart.web;

import com.example.demo.cart.dto.CartDTOs.*;
import com.example.demo.cart.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{businessId}")
    public ResponseEntity<CartDTO> getCart(@PathVariable Long businessId,
                                           HttpServletRequest req, HttpServletResponse resp) {
        return ResponseEntity.ok(cartService.getCart(businessId, req, resp));
    }

    @PostMapping("/{businessId}/items")
    public ResponseEntity<CartDTO> addItem(@PathVariable Long businessId,
                                           @Valid @RequestBody AddItemRequest body,
                                           HttpServletRequest req, HttpServletResponse resp) {
        return ResponseEntity.ok(cartService.addItem(businessId, body.productId(), body.quantity(), req, resp));
    }

    @PutMapping("/{businessId}/items/{productId}")
    public ResponseEntity<CartDTO> updateItem(@PathVariable Long businessId,
                                              @PathVariable Long productId,
                                              @Valid @RequestBody UpdateQuantityRequest body,
                                              HttpServletRequest req, HttpServletResponse resp) {
        return ResponseEntity.ok(cartService.updateItem(businessId, productId, body.quantity(), req, resp));
    }

    @DeleteMapping("/{businessId}/items/{productId}")
    public ResponseEntity<CartDTO> removeItem(@PathVariable Long businessId,
                                              @PathVariable Long productId,
                                              HttpServletRequest req, HttpServletResponse resp) {
        return ResponseEntity.ok(cartService.removeItem(businessId, productId, req, resp));
    }

    @DeleteMapping("/{businessId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long businessId,
                                          HttpServletRequest req, HttpServletResponse resp) {
        cartService.clearCart(businessId, req, resp);
        return ResponseEntity.noContent().build();
    }
}
