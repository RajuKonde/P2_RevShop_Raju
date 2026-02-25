package com.revshop.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(columnNames = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==============================
    // CATEGORY NAME (Electronics, Fashion)
    // ==============================
    @Column(nullable = false, length = 100)
    private String name;

    // ==============================
    // URL FRIENDLY SLUG (electronics, men-fashion)
    // ==============================
    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    // ==============================
    // DESCRIPTION
    // ==============================
    @Column(length = 500)
    private String description;

    // ==============================
    // CATEGORY ACTIVE FLAG
    // ==============================
    @Column(nullable = false)
    private Boolean active = true;
}