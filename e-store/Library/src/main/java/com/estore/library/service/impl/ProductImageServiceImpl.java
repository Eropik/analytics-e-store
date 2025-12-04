package com.estore.library.service.impl;

import com.estore.library.model.dicts.ProductImage;
import com.estore.library.repository.dicts.ProductImageRepository;
import com.estore.library.service.ProductImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductImageServiceImpl implements ProductImageService {
    
    private final ProductImageRepository productImageRepository;
    
    @Override
    @Transactional
    public ProductImage createProductImage(ProductImage productImage) {
        if (productImage.getImageUrl() == null || productImage.getImageUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be empty");
        }
        
        // Если sortOrder не указан, ставим в конец
        if (productImage.getSortOrder() == null) {
            Long count = productImageRepository.countByProductId(productImage.getProduct().getProductId());
            productImage.setSortOrder(count.intValue());
        }
        
        return productImageRepository.save(productImage);
    }
    
    @Override
    @Transactional
    public ProductImage updateProductImage(Integer imageId, ProductImage productImage) {
        ProductImage existing = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Product image not found with id: " + imageId));
        
        existing.setImageUrl(productImage.getImageUrl());
        existing.setSortOrder(productImage.getSortOrder());
        
        return productImageRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteProductImage(Integer imageId) {
        if (!productImageRepository.existsById(imageId)) {
            throw new IllegalArgumentException("Product image not found with id: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }
    
    @Override
    public Optional<ProductImage> getProductImageById(Integer imageId) {
        return productImageRepository.findById(imageId);
    }
    
    @Override
    public List<ProductImage> getAllProductImages() {
        return productImageRepository.findAll();
    }
    
    @Override
    public List<ProductImage> getProductImagesByProductId(UUID productId) {
        return productImageRepository.findByProductId(productId);
    }
    
    @Override
    public List<ProductImage> getProductImagesOrderedBySortOrder(UUID productId) {
        return productImageRepository.findByProductIdOrderBySortOrder(productId);
    }
    
    @Override
    @Transactional
    public void deleteAllProductImages(UUID productId) {
        productImageRepository.deleteByProductId(productId);
    }
    
    @Override
    public Long countProductImages(UUID productId) {
        return productImageRepository.countByProductId(productId);
    }
    
    @Override
    @Transactional
    public void updateSortOrder(Integer imageId, Integer sortOrder) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Product image not found with id: " + imageId));
        
        image.setSortOrder(sortOrder);
        productImageRepository.save(image);
    }





    @Override
    public Optional<ProductImage> findFirstByProductIdOrderBySortOrder(UUID productId) {
        PageRequest pageable = PageRequest.of(0, 1);

        // Вызываем метод репозитория
        Optional<ProductImage> images = productImageRepository.findFirstByProductIdOrderBySortOrder(productId);


        return images.isEmpty() ? Optional.empty() : Optional.of(images.get());
    }

    @Override
    public void deleteProductImageByProductIdAndImageId(UUID productId, Integer imageId) {

        // 1. Найти изображение по ID.
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Product image not found with id: " + imageId));

        // 2. Проверить, что изображение принадлежит указанному продукту.
        // Используем .get().getProductId(), чтобы безопасно работать с прокси/ленивой загрузкой
        if (!image.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException(
                    "Access denied or Image ID " + imageId + " does not belong to Product ID " + productId
            );
        }

        // 3. Удалить изображение.
        long deletedCount = productImageRepository.deleteByProductProductIdAndImageId(productId, imageId);
        if (deletedCount == 0) {
            // Если ничего не удалено, значит, либо изображение не существует, либо
            // оно не принадлежит указанному продукту.
            throw new IllegalArgumentException(
                    "Image ID " + imageId + " not found or does not belong to Product ID " + productId
            );
        }
    }
}
