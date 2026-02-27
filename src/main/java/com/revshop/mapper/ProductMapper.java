package com.revshop.mapper;

import com.revshop.dto.product.ProductResponse;
import com.revshop.entity.ProductImage;
import com.revshop.entity.Product;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        List<String> imageUrls = product.getImages() == null ? List.of() :
                product.getImages()
                        .stream()
                        .sorted(Comparator.comparing(ProductImage::getDisplayOrder, Comparator.nullsLast(Integer::compareTo)))
                        .map(ProductImage::getImageUrl)
                        .toList();

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .inStock(product.getInStock())
                .active(product.getActive())
                .status(product.getStatus())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .sellerId(product.getSeller().getId())
                .sellerEmail(product.getSeller().getEmail())
                .imageUrls(imageUrls)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
