package com.revshop.service.impl;

import com.revshop.dao.CategoryDAO;
import com.revshop.dao.ProductDAO;
import com.revshop.dao.ProductImageDAO;
import com.revshop.dao.UserDAO;
import com.revshop.dto.product.ProductCreateRequest;
import com.revshop.dto.product.ProductImageResponse;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.product.ProductUpdateRequest;
import com.revshop.entity.Category;
import com.revshop.entity.Product;
import com.revshop.entity.ProductImage;
import com.revshop.entity.Role;
import com.revshop.entity.User;
import com.revshop.exception.BadRequestException;
import com.revshop.exception.ForbiddenOperationException;
import com.revshop.exception.InternalServerException;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.mapper.ProductMapper;
import com.revshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductDAO productDAO;
    private final ProductImageDAO productImageDAO;
    private final CategoryDAO categoryDAO;
    private final UserDAO userDAO;
    private final ProductMapper productMapper;
    @Value("${app.upload.product-images-dir:uploads/product-images}")
    private String productImagesDir;
    @Value("${app.upload.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

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

    @Override
    @Transactional
    public List<ProductImageResponse> uploadProductImages(Long productId, String sellerEmail, List<MultipartFile> files) {
        Product product = getOwnedProduct(productId, sellerEmail);

        if (files == null || files.isEmpty()) {
            throw new BadRequestException("At least one image file is required");
        }

        AtomicLong orderCounter = new AtomicLong(productImageDAO.countByProductId(productId));

        return files.stream()
                .map(file -> saveProductImage(product, file, orderCounter.incrementAndGet()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImageResponse> getProductImages(Long productId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        if (Boolean.TRUE.equals(product.getIsDeleted()) || !Boolean.TRUE.equals(product.getActive())) {
            throw new ResourceNotFoundException("Product not found");
        }

        return productImageDAO.findByProductId(productId).stream()
                .map(image -> ProductImageResponse.builder()
                        .id(image.getId())
                        .imageUrl(image.getImageUrl())
                        .displayOrder(image.getDisplayOrder())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void deleteProductImage(Long productId, Long imageId, String sellerEmail) {
        Product product = getOwnedProduct(productId, sellerEmail);

        ProductImage image = productImageDAO.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        if (!image.getProduct().getId().equals(product.getId())) {
            throw new BadRequestException("Image does not belong to requested product");
        }

        deletePhysicalFileIfExists(image.getImageUrl());
        productImageDAO.delete(image);
    }

    private Product getOwnedProduct(Long productId, String sellerEmail) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getSeller().getEmail().equals(sellerEmail)) {
            throw new ForbiddenOperationException("Not owner of this product");
        }

        return product;
    }

    private ProductImageResponse saveProductImage(Product product, MultipartFile file, long orderValue) {
        if (file.isEmpty()) {
            throw new BadRequestException("Empty file is not allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        String originalFilename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String fileName = UUID.randomUUID() + extension;

        Path uploadDir = Paths.get(productImagesDir).toAbsolutePath().normalize();
        Path destination = uploadDir.resolve(fileName).normalize();

        try {
            Files.createDirectories(uploadDir);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new InternalServerException("Failed to store image file");
        }

        int order = (int) orderValue;
        String imageUrl = publicBaseUrl + "/uploads/product-images/" + fileName;

        ProductImage image = ProductImage.builder()
                .imageUrl(imageUrl)
                .displayOrder(order)
                .product(product)
                .build();

        ProductImage saved = productImageDAO.save(image);
        return ProductImageResponse.builder()
                .id(saved.getId())
                .imageUrl(saved.getImageUrl())
                .displayOrder(saved.getDisplayOrder())
                .build();
    }

    private String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return ".jpg";
        }
        return fileName.substring(dotIndex).toLowerCase();
    }

    private void deletePhysicalFileIfExists(String imageUrl) {
        int idx = imageUrl.lastIndexOf('/');
        if (idx < 0 || idx == imageUrl.length() - 1) {
            return;
        }
        String fileName = imageUrl.substring(idx + 1);
        Path target = Paths.get(productImagesDir).toAbsolutePath().normalize().resolve(fileName);
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Keep DB consistency even if file deletion is best-effort.
        }
    }
}
