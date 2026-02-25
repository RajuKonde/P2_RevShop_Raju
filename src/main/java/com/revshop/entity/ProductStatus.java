package com.revshop.entity;

public enum ProductStatus {

    DRAFT,      // seller saved but not published
    ACTIVE,     // visible to buyers
    OUT_OF_STOCK,
    DISCONTINUED,
    BLOCKED     // admin disabled
}