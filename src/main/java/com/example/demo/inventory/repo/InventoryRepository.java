package com.example.demo.inventory.repo;

import com.example.demo.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByBusinessIdAndProductId(Long businessId, Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    Optional<Inventory> findWithLockingByBusinessIdAndProductId(Long businessId, Long productId);

    List<Inventory> findByBusinessId(Long businessId);

    List<Inventory> findByBusinessIdAndStockLessThanEqual(Long businessId, Integer safety);
}
