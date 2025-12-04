package com.estore.library.service;

import com.estore.library.model.dicts.ProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageService {
    
    ProductImage createProductImage(ProductImage productImage);
    
    ProductImage updateProductImage(Integer imageId, ProductImage productImage);
    
    void deleteProductImage(Integer imageId);
    
    Optional<ProductImage> getProductImageById(Integer imageId);
    
    List<ProductImage> getAllProductImages();
    
    List<ProductImage> getProductImagesByProductId(UUID productId);
    
    List<ProductImage> getProductImagesOrderedBySortOrder(UUID productId);
    
    void deleteAllProductImages(UUID productId);
    
    Long countProductImages(UUID productId);
    
    void updateSortOrder(Integer imageId, Integer sortOrder);

    void deleteProductImageByProductIdAndImageId(UUID productId, Integer imageId);

    Optional<ProductImage> findFirstByProductIdOrderBySortOrder(UUID productId);
}
