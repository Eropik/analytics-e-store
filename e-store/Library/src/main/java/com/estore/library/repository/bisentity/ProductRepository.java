package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    Page<Product> findByIsAvailable(Boolean isAvailable, Pageable pageable);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.brand")
    Page<Product> findAllWithCategoryAndBrand(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId AND p.isAvailable = true")
    Page<Product> findByCategoryId(@Param("categoryId") Integer categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.brand.brandId = :brandId AND p.isAvailable = true")
    Page<Product> findByBrandId(@Param("brandId") Integer brandId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isAvailable = true")
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
    
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchByNameOrDescription(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.averageRating >= :minRating AND p.isAvailable = true")
    Page<Product> findByMinRating(@Param("minRating") BigDecimal minRating, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0 AND p.isAvailable = true")
    Page<Product> findInStock(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold")
    Page<Product> findLowStock(@Param("threshold") Integer threshold, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.category.categoryId = :categoryId " +
           "AND p.brand.brandId = :brandId AND p.isAvailable = true")
    Page<Product> findByCategoryAndBrand(
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            Pageable pageable
    );
    
    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.averageRating DESC")
    Page<Product> findTopRated(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.createdAt DESC")
    Page<Product> findNewest(Pageable pageable);

    @Query("""
           SELECT p FROM Product p
           LEFT JOIN FETCH p.category c
           LEFT JOIN FETCH p.brand b
           WHERE (:productId IS NULL OR p.productId = :productId)
             AND (:categoryId IS NULL OR c.categoryId = :categoryId)
             AND (:brandId IS NULL OR b.brandId = :brandId)
             AND (:minPrice IS NULL OR p.price >= :minPrice)
             AND (:maxPrice IS NULL OR p.price <= :maxPrice)
             AND (
                 :search IS NULL OR :search = '' OR
                 LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
                 LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
             )
           """)
    Page<Product> searchAdvanced(
            @Param("productId") UUID productId,
            @Param("categoryId") Integer categoryId,
            @Param("brandId") Integer brandId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("search") String search,
            Pageable pageable);
}
