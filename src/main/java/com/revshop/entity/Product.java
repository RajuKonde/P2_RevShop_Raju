package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // BASIC INFO
    // ===============================
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 2000)
    private String description;

    // ===============================
    // PRICING
    // ===============================
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    // ===============================
    // INVENTORY
    // ===============================
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Boolean inStock;

    // ===============================
    // PRODUCT STATUS
    // ===============================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false)
    private Boolean active;

    // ===============================
    // CATEGORY (Many Products → One Category)
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ===============================
    // SELLER (Marketplace Model)
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}