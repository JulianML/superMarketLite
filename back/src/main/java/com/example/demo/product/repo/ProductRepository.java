package com.example.demo.product.repo;

import com.example.demo.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBusinessIdAndId(Long businessId, Long id);
    Optional<Product> findByBusinessIdAndSku(Long businessId, String sku);

    Page<Product> findByBusinessIdAndIsActiveTrue(Long businessId, Pageable pageable);

    @Query(value = "SELECT p FROM Product p WHERE p.businessId = :businessId AND p.isActive = true AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))" )
    Page<Product> search(Long businessId, String q, Pageable pageable);
}
