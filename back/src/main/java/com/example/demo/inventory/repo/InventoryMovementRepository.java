package com.example.demo.inventory.repo;

import com.example.demo.inventory.entity.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    List<InventoryMovement> findByBusinessIdAndProductIdOrderByCreatedAtDesc(Long businessId, Long productId);
    List<InventoryMovement> findByBusinessIdAndCreatedAtBetweenOrderByCreatedAtDesc(Long businessId, LocalDateTime from, LocalDateTime to);
}
