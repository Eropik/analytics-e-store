package com.estore.library.service;

import com.estore.library.model.bisentity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {

    Order createOrder(Order order);

    Order updateOrder(UUID orderId, Order order);

    Optional<Order> getOrderById(UUID orderId);

    Page<Order> getAllOrders(Pageable pageable);

    Page<Order> getOrdersByUserId(UUID userId, Pageable pageable);

    Page<Order> getOrdersByStatus(Integer statusId, Pageable pageable);

    Page<Order> getOrdersByUserIdAndStatus(UUID userId, Integer statusId, Pageable pageable);

    Page<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Long getUserOrderCount(UUID userId);

    BigDecimal getUserTotalSpent(UUID userId);

    void updateOrderStatus(UUID orderId, Integer newStatusId);

    // 🚀 НОВЫЙ МЕТОД: для фильтрации по диапазону дат И статусу
    Page<Order> getOrdersByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, Integer statusId, Pageable pageable);

}