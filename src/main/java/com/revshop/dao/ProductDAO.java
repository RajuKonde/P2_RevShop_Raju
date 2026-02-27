package com.revshop.dao;

import com.revshop.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductDAO {

    Product save(Product product);

    Optional<Product> findById(Long id);

    List<Product> findBySellerEmail(String email);

    List<Product> findByCategory(Long categoryId);

    List<Product> findActiveProducts();

    List<Product> searchByName(String keyword);
}