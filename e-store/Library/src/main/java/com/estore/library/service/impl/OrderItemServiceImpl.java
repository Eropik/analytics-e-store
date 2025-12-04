package com.estore.library.service.impl;

import com.estore.library.model.bisentity.OrderItem;
import com.estore.library.repository.bisentity.OrderItemRepository;
import com.estore.library.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderItemServiceImpl implements OrderItemService {
    
    private final OrderItemRepository orderItemRepository;
    
    @Override
    @Transactional
    public OrderItem createOrderItem(OrderItem orderItem) {
        if (orderItem.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return orderItemRepository.save(orderItem);
    }
    
    @Override
    @Transactional
    public OrderItem updateOrderItem(Integer orderItemId, OrderItem orderItem) {
        OrderItem existing = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new IllegalArgumentException("Order item not found with id: " + orderItemId));
        
        existing.setQuantity(orderItem.getQuantity());
        existing.setUnitPrice(orderItem.getUnitPrice());
        
        return orderItemRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteOrderItem(Integer orderItemId) {
        if (!orderItemRepository.existsById(orderItemId)) {
            throw new IllegalArgumentException("Order item not found with id: " + orderItemId);
        }
        orderItemRepository.deleteById(orderItemId);
    }
    
    @Override
    public Optional<OrderItem> getOrderItemById(Integer orderItemId) {
        return orderItemRepository.findById(orderItemId);
    }
    
    @Override
    public List<OrderItem> getAllOrderItems() {
        return orderItemRepository.findAll();
    }
    
    @Override
    public List<OrderItem> getOrderItemsByOrderId(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }
    
    @Override
    public List<OrderItem> getOrderItemsByProductId(UUID productId) {
        return orderItemRepository.findByProductId(productId);
    }
    
    @Override
    public Long getTotalQuantitySoldForProduct(UUID productId) {
        Long total = orderItemRepository.sumQuantityByProductId(productId);
        return total != null ? total : 0L;
    }
    
    @Override
    public List<Object[]> getTopSellingProducts() {
        return orderItemRepository.findTopSellingProducts();
    }
}
