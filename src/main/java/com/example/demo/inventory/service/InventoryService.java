package com.example.demo.inventory.service;

import com.example.demo.common.exception.BusinessRuleException;
import com.example.demo.common.exception.NotFoundException;
import com.example.demo.inventory.dto.InventoryDTOs;
import com.example.demo.inventory.entity.Inventory;
import com.example.demo.inventory.entity.InventoryMovement;
import com.example.demo.inventory.repo.InventoryMovementRepository;
import com.example.demo.inventory.repo.InventoryRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;

    public InventoryService(InventoryRepository inventoryRepository, InventoryMovementRepository movementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
    }

    public InventoryDTOs.InventoryDTO get(Long businessId, Long productId) {
        Inventory inv = inventoryRepository.findByBusinessIdAndProductId(businessId, productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found"));
        return toDTO(inv);
    }

    public List<InventoryDTOs.InventoryDTO> list(Long businessId) {
        return inventoryRepository.findByBusinessId(businessId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<InventoryDTOs.InventoryDTO> belowSafety(Long businessId) {
        return inventoryRepository.findByBusinessIdAndStockLessThanEqual(businessId, 0).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public InventoryDTOs.InventoryDTO setStock(Long businessId, Long productId, InventoryDTOs.InventorySetDTO dto) {
        Inventory inv = inventoryRepository.findWithLockingByBusinessIdAndProductId(businessId, productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found"));
        if (dto.stock < 0) throw new BusinessRuleException("Stock cannot be negative");
        int delta = dto.stock - inv.getStock();
        inv.setStock(dto.stock);
        if (dto.safetyStock != null) {
            if (dto.safetyStock < 0) throw new BusinessRuleException("Safety stock cannot be negative");
            inv.setSafetyStock(dto.safetyStock);
        }
        Inventory saved = inventoryRepository.save(inv);
        if (delta != 0) {
            InventoryMovement m = new InventoryMovement();
            m.setBusinessId(businessId);
            m.setProductId(productId);
            m.setQuantityChange(delta);
            m.setReason("SET");
            m.setReferenceId(null);
            movementRepository.save(m);
        }
        return toDTO(saved);
    }

    @Transactional
    public InventoryDTOs.InventoryDTO adjustStock(Long businessId, Long productId, InventoryDTOs.InventoryAdjustDTO dto) {
        Inventory inv = inventoryRepository.findWithLockingByBusinessIdAndProductId(businessId, productId)
                .orElseThrow(() -> new NotFoundException("Inventory not found"));
        int newStock = inv.getStock() + dto.delta;
        if (newStock < 0) {
            throw new BusinessRuleException("Insufficient stock");
        }
        inv.setStock(newStock);
        Inventory saved = inventoryRepository.save(inv);
        InventoryMovement m = new InventoryMovement();
        m.setBusinessId(businessId);
        m.setProductId(productId);
        m.setQuantityChange(dto.delta);
        m.setReason(dto.reason);
        m.setReferenceId(dto.referenceId);
        try {
            movementRepository.save(m);
        } catch (DataIntegrityViolationException ex) {
            // Unique constraint violated -> idempotent behavior: do nothing else
        }
        return toDTO(saved);
    }

    public List<InventoryDTOs.InventoryMovementDTO> movements(Long businessId, Long productId) {
        return movementRepository.findByBusinessIdAndProductIdOrderByCreatedAtDesc(businessId, productId)
                .stream().map(this::toMovementDTO).collect(Collectors.toList());
    }

    private InventoryDTOs.InventoryDTO toDTO(Inventory inv) {
        InventoryDTOs.InventoryDTO dto = new InventoryDTOs.InventoryDTO();
        dto.businessId = inv.getBusinessId();
        dto.productId = inv.getProductId();
        dto.stock = inv.getStock();
        dto.safetyStock = inv.getSafetyStock();
        dto.updatedAt = inv.getUpdatedAt();
        return dto;
    }

    private InventoryDTOs.InventoryMovementDTO toMovementDTO(InventoryMovement m) {
        InventoryDTOs.InventoryMovementDTO dto = new InventoryDTOs.InventoryMovementDTO();
        dto.id = m.getId();
        dto.businessId = m.getBusinessId();
        dto.productId = m.getProductId();
        dto.quantityChange = m.getQuantityChange();
        dto.reason = m.getReason();
        dto.referenceId = m.getReferenceId();
        dto.createdAt = m.getCreatedAt();
        return dto;
    }
}
