package com.revshop.service.impl;

import com.revshop.dao.CategoryDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.ProductImageDAO;
import com.revshop.dao.UserDAO;
import com.revshop.entity.*;
import com.revshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.math.BigDecimal;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final ProductImageDAO productImageDAO;
    private final UserDAO userDAO;

    // ===============================
    // CREATE PRODUCT (Seller Only)
    // ===============================
    @Override
    @Transactional
    public Product createProduct(
            Long sellerId,
            Long categoryId,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            List<String> imageUrls
    ) {

        User seller = validateSeller(sellerId);
        Category category = validateCategory(categoryId);

        Product product = Product.builder()
                .name(name)
                .description(description)
                .price(price)
                .stock(stock)
                .active(true)
                .deleted(false)
                .seller(seller)
                .category(category)
                .build();

        product = productDAO.save(product);

        saveProductImages(product, imageUrls);

        return product;
    }

    // ===============================
    // UPDATE PRODUCT
    // ===============================
    @Override
    @Transactional
    public Product updateProduct(
            Long productId,
            Long sellerId,
            String name,
            String description,
            BigDecimal price,
            Integer stock
    ) {
        Product product = getOwnedProduct(productId, sellerId);

        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);

        return productDAO.save(product);
    }

    // ===============================
    // DELETE PRODUCT (SOFT)
    // ===============================
    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = getOwnedProduct(productId, sellerId);
        product.setDeleted(true);
        productDAO.save(product);
    }

    // ===============================
    // GET PRODUCT BY ID
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // ===============================
    // SELLER PRODUCTS
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public List<Product> getSellerProducts(Long sellerId) {
        return productDAO.findBySellerId(sellerId);
    }

    // ===============================
    // CATEGORY PRODUCTS
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public List<Product> getProductsByCategory(Long categoryId) {
        return productDAO.findByCategory(categoryId);
    }

    // ===============================
    // PUBLIC PRODUCTS
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public List<Product> getAllActiveProducts() {
        return productDAO.findActiveProducts();
    }

    // ===============================
    // PRIVATE HELPERS
    // ===============================

    private User validateSeller(Long sellerId) {
        User user = userDAO.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        if (user.getRole() != Role.SELLER) {
            throw new RuntimeException("User is not a seller");
        }

        return user;
    }

    private Category validateCategory(Long categoryId) {
        return categoryDAO.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    private Product getOwnedProduct(Long productId, Long sellerId) {
        Product product = getProduct(productId);

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("You are not the owner of this product");
        }

        return product;
    }

    private void saveProductImages(Product product, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) return;

        int order = 1;
        for (String url : imageUrls) {
            ProductImage image = ProductImage.builder()
                    .imageUrl(url)
                    .displayOrder(order++)
                    .product(product)
                    .build();

            productImageDAO.save(image);
        }
    }
}