package com.revshop.service.impl;

import com.revshop.dao.CategoryDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.product.ProductCreateRequest;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.product.ProductUpdateRequest;
import com.revshop.entity.Category;
import com.revshop.entity.Product;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.ForbiddenOperationException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.mapper.ProductMapper;
import com.revshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request, String sellerEmail) {
        User seller = userDAO.findByEmail(sellerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        if (seller.getRole() != Role.SELLER) {
            throw new ForbiddenOperationException("User is not a seller");
        }

        Category category = categoryDAO.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .inStock(request.getStock() > 0)
                .active(true)
                .seller(seller)
                .category(category)
                .build();

        return productMapper.toResponse(productDAO.save(product));
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long productId, String sellerEmail, ProductUpdateRequest request) {
        Product product = getOwnedProduct(productId, sellerEmail);

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setInStock(request.getStock() > 0);

        return productMapper.toResponse(productDAO.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, String sellerEmail) {
        Product product = getOwnedProduct(productId, sellerEmail);
        product.setActive(false);
        product.setIsDeleted(true);
        productDAO.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getSellerProducts(String sellerEmail) {
        return productDAO.findBySellerEmail(sellerEmail)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllActiveProducts() {
        return productDAO.findActiveProducts()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        return productDAO.findByCategory(categoryId)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchProducts(String keyword) {
        return productDAO.searchByName(keyword)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    private Product getOwnedProduct(Long productId, String sellerEmail) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            throw new ForbiddenOperationException("Not owner of this product");
        }

        return product;
    }
}
