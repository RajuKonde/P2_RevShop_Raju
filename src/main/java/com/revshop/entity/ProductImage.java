package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===============================
    // IMAGE URL (CDN / S3 / Cloudinary)
    // ===============================
    @Column(nullable = false, length = 1000)
    private String imageUrl;

    // ===============================
    // ALT TEXT (SEO + Accessibility)
    // ===============================
    @Column(length = 255)
    private String altText;

    // ===============================
    // IMAGE ORDER (gallery sorting)
    // ===============================
    private Integer displayOrder;

    // ===============================
    // THUMBNAIL FLAG
    // ===============================
    @Column(nullable = false)
    private Boolean thumbnail;

    // ===============================
    // RELATION → PRODUCT
    // ===============================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}