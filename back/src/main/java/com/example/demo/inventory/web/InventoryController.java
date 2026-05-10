package com.example.demo.inventory.web;

import com.example.demo.inventory.dto.InventoryDTOs;
import com.example.demo.inventory.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    @Operation(summary = "List inventory by business")
    public List<InventoryDTOs.InventoryDTO> list(@RequestParam Long businessId) {
        return inventoryService.list(businessId);
    }

    @GetMapping("/below-safety")
    @Operation(summary = "List inventory items at or below safety for a business")
    public List<InventoryDTOs.InventoryDTO> belowSafety(@RequestParam Long businessId) {
        return inventoryService.belowSafety(businessId);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get inventory by business and product")
    public InventoryDTOs.InventoryDTO get(@RequestParam Long businessId, @PathVariable Long productId) {
        return inventoryService.get(businessId, productId);
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Set absolute stock and optionally safety stock")
    public InventoryDTOs.InventoryDTO set(@RequestParam Long businessId,
                                          @PathVariable Long productId,
                                          @Valid @RequestBody InventoryDTOs.InventorySetDTO dto) {
        return inventoryService.setStock(businessId, productId, dto);
    }

    @PostMapping("/{productId}/adjust")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Adjust stock by delta and create an audit movement (idempotent by referenceId)")
    public InventoryDTOs.InventoryDTO adjust(@RequestParam Long businessId,
                                             @PathVariable Long productId,
                                             @Valid @RequestBody InventoryDTOs.InventoryAdjustDTO dto) {
        return inventoryService.adjustStock(businessId, productId, dto);
    }

    @GetMapping("/{productId}/movements")
    @Operation(summary = "List movements for a product in a business")
    public List<InventoryDTOs.InventoryMovementDTO> movements(@RequestParam Long businessId,
                                                              @PathVariable Long productId) {
        return inventoryService.movements(businessId, productId);
    }
}
