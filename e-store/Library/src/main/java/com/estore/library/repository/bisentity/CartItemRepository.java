package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    List<CartItem> findByCartId(@Param("cartId") UUID cartId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    Optional<CartItem> findByCartIdAndProductId(
            @Param("cartId") UUID cartId,
            @Param("productId") UUID productId
    );
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.product.productId = :productId")
    List<CartItem> findByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Long countByCartId(@Param("cartId") UUID cartId);
    
    @Query("SELECT SUM(ci.quantity * ci.unitPrice) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    BigDecimal calculateCartTotal(@Param("cartId") UUID cartId);
    
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    Integer sumQuantityByCartId(@Param("cartId") UUID cartId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    void deleteByCartId(@Param("cartId") UUID cartId);
    
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    void deleteByCartIdAndProductId(
            @Param("cartId") UUID cartId,
            @Param("productId") UUID productId
    );
    
    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END FROM CartItem ci " +
           "WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId")
    boolean existsByCartIdAndProductId(
            @Param("cartId") UUID cartId,
            @Param("productId") UUID productId
    );
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.user.userId = :userId")
    List<CartItem> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT ci.product.productId, SUM(ci.quantity) FROM CartItem ci " +
           "GROUP BY ci.product.productId ORDER BY SUM(ci.quantity) DESC")
    List<Object[]> findMostAddedToCartProducts();
}
