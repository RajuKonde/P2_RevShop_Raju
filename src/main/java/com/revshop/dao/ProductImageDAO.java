package com.revshop.dao;

import com.revshop.entity.ProductImage;

import java.util.List;

public interface ProductImageDAO {

    ProductImage save(ProductImage image);

    List<ProductImage> findByProductId(Long productId);

    void deleteByProductId(Long productId);
}