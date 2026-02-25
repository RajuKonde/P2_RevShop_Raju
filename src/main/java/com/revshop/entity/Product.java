package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
    @Builder.Default
    private Boolean inStock = true;

    // ===============================
    // PRODUCT STATUS
    // ===============================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    // ===============================
    // SOFT DELETE (ENTERPRISE)
    // ===============================
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;

    // ===============================
    // CATEGORY
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ===============================
    // SELLER
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    // ===============================
    // IMAGES
    // ===============================
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    // ===============================
    // HELPER METHODS (BEST PRACTICE)
    // ===============================
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);
    }

    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }
}