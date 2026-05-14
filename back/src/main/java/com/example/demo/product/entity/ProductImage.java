package com.example.demo.product.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_images")
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 512)
    private String url;

    @Column(nullable = false)
    private int position = 0;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public String getAltText() { return altText; }
    public void setAltText(String altText) { this.altText = altText; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
