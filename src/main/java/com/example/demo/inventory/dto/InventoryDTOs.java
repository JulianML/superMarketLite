package com.example.demo.inventory.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class InventoryDTOs {

    public static class InventoryDTO {
        public Long businessId;
        public Long productId;
        public Integer stock;
        public Integer safetyStock;
        public LocalDateTime updatedAt;
    }

    public static class InventorySetDTO {
        @NotNull
        public Integer stock;
        @Min(0)
        public Integer safetyStock;
    }

    public static class InventoryAdjustDTO {
        @NotNull
        public Integer delta;
        @NotBlank
        public String reason;
        public Long referenceId; // puede ser null
    }

    public static class InventoryMovementDTO {
        public Long id;
        public Long businessId;
        public Long productId;
        public Integer quantityChange;
        public String reason;
        public Long referenceId;
        public LocalDateTime createdAt;
    }
}
