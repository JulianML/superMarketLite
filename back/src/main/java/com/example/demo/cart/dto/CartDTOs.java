package com.example.demo.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartDTOs {

    /** Stored in the guest cookie — never contains price. */
    public record CartCookieItem(Long productId, int quantity) {}

    /** API response item — prices always fetched server-side. */
    public record CartItemDTO(Long productId, String productName, String sku,
                              int quantity, BigDecimal unitPrice, BigDecimal lineTotal) {}

    /** API response for the whole cart. */
    public record CartDTO(Long businessId, List<CartItemDTO> items, BigDecimal total) {}

    public record AddItemRequest(@NotNull Long productId, @Min(1) int quantity) {}

    public record UpdateQuantityRequest(@Min(1) int quantity) {}

    /** RabbitMQ payload — never contains price. */
    public record CartSyncMessage(Long userId, Long businessId,
                                  List<CartCookieItem> items, LocalDateTime syncedAt) {}
}
