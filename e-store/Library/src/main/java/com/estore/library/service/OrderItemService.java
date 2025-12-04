package com.estore.library.service;

import com.estore.library.model.bisentity.OrderItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemService {
    
    OrderItem createOrderItem(OrderItem orderItem);
    
    OrderItem updateOrderItem(Integer orderItemId, OrderItem orderItem);
    
    void deleteOrderItem(Integer orderItemId);
    
    Optional<OrderItem> getOrderItemById(Integer orderItemId);
    
    List<OrderItem> getAllOrderItems();
    
    List<OrderItem> getOrderItemsByOrderId(UUID orderId);
    
    List<OrderItem> getOrderItemsByProductId(UUID productId);
    
    Long getTotalQuantitySoldForProduct(UUID productId);
    
    List<Object[]> getTopSellingProducts();
}
