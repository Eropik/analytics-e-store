package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrderId(@Param("orderId") UUID orderId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.product.productId = :productId")
    List<OrderItem> findByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.productId = :productId")
    Long sumQuantityByProductId(@Param("productId") UUID productId);
    
    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.id = :orderId AND oi.product.productId = :productId")
    List<OrderItem> findByOrderIdAndProductId(
            @Param("orderId") UUID orderId,
            @Param("productId") UUID productId
    );
    
    @Query("SELECT oi.product.productId, SUM(oi.quantity) as totalQuantity FROM OrderItem oi " +
           "GROUP BY oi.product.productId ORDER BY totalQuantity DESC")
    List<Object[]> findTopSellingProducts();
}
