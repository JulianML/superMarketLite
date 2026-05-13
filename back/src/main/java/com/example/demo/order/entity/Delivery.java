package com.example.demo.order.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Getter
@Setter
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "business_id", nullable = false)
    private Long businessId;

    @Column(nullable = false, length = 30)
    private String method = "LOCAL_DELIVERY";

    @Column(name = "eta_from")
    private LocalDateTime etaFrom;

    @Column(name = "eta_to")
    private LocalDateTime etaTo;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "courier_name", length = 120)
    private String courierName;

    @Column(name = "courier_phone", length = 30)
    private String courierPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryStatus status;

    @Column(length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
    }
}
