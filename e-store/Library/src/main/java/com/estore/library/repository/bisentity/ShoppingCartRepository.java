package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.user.userId = :userId")
    Optional<ShoppingCart> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM ShoppingCart sc WHERE sc.user.userId = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.updatedAt < :date")
    List<ShoppingCart> findInactiveCarts(@Param("date") LocalDateTime date);
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.createdAt >= :startDate AND sc.createdAt <= :endDate")
    List<ShoppingCart> findByCreatedAtBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    @Query("SELECT COUNT(sc) FROM ShoppingCart sc WHERE sc.items IS NOT EMPTY")
    Long countNonEmptyCarts();
    
    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.items WHERE sc.user.userId = :userId")
    Optional<ShoppingCart> findByUserIdWithItems(@Param("userId") UUID userId);
    
    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.items WHERE sc.cartId = :cartId")
    Optional<ShoppingCart> findByIdWithItems(@Param("cartId") UUID cartId);
    
    @Query("DELETE FROM ShoppingCart sc WHERE sc.updatedAt < :date AND sc.items IS EMPTY")
    void deleteEmptyCartsOlderThan(@Param("date") LocalDateTime date);
}
