package com.estore.library.service;

import com.estore.library.model.bisentity.Product;
import com.estore.library.dto.product.request.ProductCreateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    
    Product createProduct(Product product);
    Product createProductWithImages(ProductCreateRequest request);

    Product updateProduct(UUID productId, Product product);
    
    void deleteProduct(UUID productId);
    
    Optional<Product> getProductById(UUID productId);
    
    Page<Product> getAllProducts(Pageable pageable);
    
    Page<Product> getAvailableProducts(Pageable pageable);
    
    Page<Product> getProductsByCategory(Integer categoryId, Pageable pageable);
    
    Page<Product> getProductsByBrand(Integer brandId, Pageable pageable);
    
    Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    Page<Product> searchProducts(String search, Pageable pageable);
    
    Page<Product> getProductsByMinRating(BigDecimal minRating, Pageable pageable);
    
    Page<Product> getInStockProducts(Pageable pageable);
    
    Page<Product> getLowStockProducts(Integer threshold, Pageable pageable);
    
    Page<Product> getTopRatedProducts(Pageable pageable);
    
    Page<Product> getNewestProducts(Pageable pageable);
    
    Page<Product> getProductsByCategoryAndBrand(Integer categoryId, Integer brandId, Pageable pageable);


    void updateStock(UUID productId, Integer quantity);
    
    void updateRating(UUID productId, BigDecimal newRating);
    
    void markAsAvailable(UUID productId);
    
    void markAsUnavailable(UUID productId);
}
