package com.revshop.service;

import com.revshop.dto.product.ProductCreateRequest;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.product.ProductUpdateRequest;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request, String sellerEmail);

    ProductResponse updateProduct(Long productId, String sellerEmail, ProductUpdateRequest request);

    void deleteProduct(Long productId, String sellerEmail);

    List<ProductResponse> getSellerProducts(String sellerEmail);

    List<ProductResponse> getAllActiveProducts();

    ProductResponse getProductById(Long id);

    List<ProductResponse> getProductsByCategory(Long categoryId);

    List<ProductResponse> searchProducts(String keyword);
}
