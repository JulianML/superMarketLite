package com.example.demo.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private String eventType; // CREATE, UPDATE, DELETE
    private LocalDateTime timestamp;
    private String sourceSystem; // CSV_IMPORT

    public ProductEvent() {
    }

    public ProductEvent(Long productId, String productName, String description,
                        BigDecimal price, String eventType,
                        String sourceSystem) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.eventType = eventType;
        this.sourceSystem = sourceSystem;
        this.timestamp = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }
}