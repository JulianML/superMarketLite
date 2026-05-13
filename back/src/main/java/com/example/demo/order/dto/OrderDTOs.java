package com.example.demo.order.dto;

import com.example.demo.order.entity.AddressDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDTOs {

    public record CheckoutRequestDTO(
            @NotNull @Valid AddressRequestDTO address
    ) {}

    public record AddressRequestDTO(
            @NotBlank String street,
            @NotBlank String city,
            @NotBlank String postalCode,
            @NotBlank String country
    ) {}

    public record OrderSummaryDTO(
            Long id,
            String orderNumber,
            String status,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal total,
            String currency,
            LocalDateTime placedAt
    ) {}

    public record OrderItemDTO(
            Long productId,
            String productName,
            String sku,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal vatRate,
            BigDecimal lineTotal
    ) {}

    public record StatusHistoryDTO(
            String fromStatus,
            String toStatus,
            LocalDateTime changedAt,
            String note
    ) {}

    public record OrderDetailDTO(
            Long id,
            String orderNumber,
            String status,
            AddressDTO deliveryAddress,
            List<OrderItemDTO> items,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal shippingFee,
            BigDecimal total,
            String currency,
            LocalDateTime placedAt,
            List<StatusHistoryDTO> history
    ) {}
}
