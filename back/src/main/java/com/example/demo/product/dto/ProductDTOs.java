package com.example.demo.product.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductDTOs {

    public static class ProductCreateDTO {
        @NotNull
        public Long businessId;
        @NotBlank
        public String sku;
        @NotBlank
        public String name;
        public String description;
        @NotNull @DecimalMin("0.00")
        public BigDecimal price;
        @Pattern(regexp = "^[A-Z]{3}$")
        public String currency = "EUR";
        @DecimalMin("0.00") @DecimalMax("100.00")
        public BigDecimal vatRate;
    }

    public static class ProductUpdateDTO {
        @NotBlank
        public String sku;
        @NotBlank
        public String name;
        public String description;
        @NotNull @DecimalMin("0.00")
        public BigDecimal price;
        @Pattern(regexp = "^[A-Z]{3}$")
        public String currency = "EUR";
        @DecimalMin("0.00") @DecimalMax("100.00")
        public BigDecimal vatRate;
        public Boolean isActive;
    }

    public static class ProductSummaryDTO {
        public Long id;
        public Long businessId;
        public String sku;
        public String name;
        public BigDecimal price;
        public String currency;
        public boolean isActive;
        public String imageUrl;
    }

    public static class ProductDTO extends ProductSummaryDTO {
        public String description;
        public BigDecimal vatRate;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
        public LocalDateTime deletedAt;
    }
}
