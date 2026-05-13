package com.example.demo.cart.repo;

import com.example.demo.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserIdAndBusinessIdAndActiveTrue(Long userId, Long businessId);
}
