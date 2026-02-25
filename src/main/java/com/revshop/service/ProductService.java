package com.revshop.service;

import com.revshop.entity.Product;

import java.util.List;
import java.math.BigDecimal;
public interface ProductService {

    Product createProduct(
            Long sellerId,
            Long categoryId,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            List<String> imageUrls
    );

    Product updateProduct(
            Long productId,
            Long sellerId,
            String name,
            String description,
            BigDecimal price,
            Integer stock
    );

    void deleteProduct(Long productId, Long sellerId);

    Product getProduct(Long id);

    List<Product> getSellerProducts(Long sellerId);

    List<Product> getProductsByCategory(Long categoryId);

    List<Product> getAllActiveProducts();
}