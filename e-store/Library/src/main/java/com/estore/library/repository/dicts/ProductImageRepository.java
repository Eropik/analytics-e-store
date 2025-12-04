package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId ORDER BY pi.sortOrder ASC")
    List<ProductImage> findByProductIdOrderBySortOrder(@Param("productId") UUID productId);
    
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId")
    List<ProductImage> findByProductId(@Param("productId") UUID productId);
    
    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.productId = :productId")
    void deleteByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(pi) FROM ProductImage pi WHERE pi.product.productId = :productId")
    Long countByProductId(@Param("productId") UUID productId);


 @Query("SELECT i FROM ProductImage i WHERE i.product.productId = :productId ORDER BY i.sortOrder ASC LIMIT 1")
 Optional<ProductImage> findFirstByProductIdOrderBySortOrder(@Param("productId") UUID productId);

    @Transactional        // Удаляющие операции через deleteBy* должны быть транзакционными
    long deleteByProductProductIdAndImageId(UUID productId, Integer imageId);
}
