package com.revshop.controller;

import com.revshop.dto.common.ApiResponse;
import com.revshop.dto.product.ProductCreateRequest;
import com.revshop.dto.product.ProductResponse;
import com.revshop.dto.product.ProductUpdateRequest;
import com.revshop.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication auth
    ) {
        ProductResponse response = productService.createProduct(request, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Product created successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication auth
    ) {
        ProductResponse response = productService.updateProduct(id, auth.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Product updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id, Authentication auth) {
        productService.deleteProduct(id, auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Product deleted successfully", null));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> myProducts(Authentication auth) {
        List<ProductResponse> response = productService.getSellerProducts(auth.getName());
        return ResponseEntity.ok(ApiResponse.success("Seller products fetched", response));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> publicProducts() {
        List<ProductResponse> response = productService.getAllActiveProducts();
        return ResponseEntity.ok(ApiResponse.success("Active products fetched", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(ApiResponse.success("Product fetched", response));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> byCategory(@PathVariable Long categoryId) {
        List<ProductResponse> response = productService.getProductsByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category products fetched", response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> search(@RequestParam String keyword) {
        List<ProductResponse> response = productService.searchProducts(keyword);
        return ResponseEntity.ok(ApiResponse.success("Search results fetched", response));
    }
}
