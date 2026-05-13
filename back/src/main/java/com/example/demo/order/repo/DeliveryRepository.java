package com.example.demo.order.repo;

import com.example.demo.order.entity.Delivery;
import com.example.demo.order.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByStatusNotIn(List<DeliveryStatus> statuses);
}
