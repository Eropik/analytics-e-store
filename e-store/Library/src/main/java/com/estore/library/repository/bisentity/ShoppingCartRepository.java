package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, UUID> {
    
    @Query("SELECT sc FROM ShoppingCart sc WHERE sc.user.userId = :userId")
    Optional<ShoppingCart> findByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT CASE WHEN COUNT(sc) > 0 THEN true ELSE false END FROM ShoppingCart sc WHERE sc.user.userId = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);
    
    @Query("SELECT sc FROM ShoppingCart sc LEFT JOIN FETCH sc.items WHERE sc.user.userId = :userId")
    Optional<ShoppingCart> findByUserIdWithItems(@Param("userId") UUID userId);
}
