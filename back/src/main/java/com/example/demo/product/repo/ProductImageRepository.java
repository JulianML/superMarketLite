package com.example.demo.product.repo;

import com.example.demo.product.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.productId IN :ids AND pi.position = 0 AND pi.deletedAt IS NULL")
    List<ProductImage> findPrimaryByProductIds(Collection<Long> ids);

    Optional<ProductImage> findFirstByProductIdAndPositionAndDeletedAtIsNull(Long productId, int position);
}
