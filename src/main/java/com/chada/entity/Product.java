package com.chada.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnoreProperties({"products"}) // ignore product list inside category, but serialize category itself
    private Category category;

    @Column(name = "image_urls", columnDefinition = "TEXT")
    private String imageUrls; // JSON array of image URLs, max 4 images (legacy format)

    @Column(name = "image_details", columnDefinition = "TEXT")
    private String imageDetails; // JSON array of ProductImageDTO objects: [{url, price, description}, ...]

    private String imageUrl; // Legacy field for backward compatibility
    
    private String fragrance;
    private Integer volume;

    @Column(nullable = false)
    private Boolean active = true;

    // Promotion fields
    @Column(name = "discount_percentage")
    private Integer discountPercentage; // 0-100

    @Column(name = "original_price")
    private BigDecimal originalPrice; // Price before discount

    @Column(name = "promotion_start_date")
    private LocalDate promotionStartDate;

    @Column(name = "promotion_end_date")
    private LocalDate promotionEndDate;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"product"}) // prevent recursion
    private List<Review> reviews;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
