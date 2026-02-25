package com.revshop.dao;

import com.revshop.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findByCategory(Long categoryId);

    List<Product> findActiveProducts();

    void softDelete(Long id);
}