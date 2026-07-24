package com.estore.library.service.impl;
import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.OrderItem;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.bisentity.OrderRepository;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.CityRouteService;
import com.estore.library.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;
    private final CityRouteService cityRouteService;




    @Override
    @Transactional
    public Order createOrder(Order order) {
        order.setOrderDate(new Date());

        OrderStatus defaultStatus = orderStatusRepository.findByStatusName("PROCESSING")
                .orElseThrow(() -> new IllegalStateException("Default status 'PROCESSING' not found"));
        order.setStatus(defaultStatus);

        BigDecimal totalAmount = calculateTotalAmount(order);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order updateOrder(UUID orderId, Order order) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        existingOrder.setStatus(order.getStatus());
        existingOrder.setShippingCity(order.getShippingCity());
        existingOrder.setShippingAddressText(order.getShippingAddressText());
        existingOrder.setDeliveryMethod(order.getDeliveryMethod());
        existingOrder.setPaymentMethod(order.getPaymentMethod());
        existingOrder.setDiscountApplied(order.getDiscountApplied());
        existingOrder.setActualDeliveryDate(order.getActualDeliveryDate());
        BigDecimal totalAmount = calculateTotalAmount(existingOrder);
        existingOrder.setTotalAmount(totalAmount);

        return orderRepository.save(existingOrder);
    }

    @Override
    public Optional<Order> getOrderById(UUID orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Page<Order> getOrdersByUserId(UUID userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatus(Integer statusId, Pageable pageable) {
        return orderRepository.findByStatus_StatusId(statusId, pageable);
    }

    @Override
    public Page<Order> getOrdersByUserIdAndStatus(UUID userId, Integer statusId, Pageable pageable) {
        return orderRepository.findByUserIdAndStatus_StatusId(userId, statusId, pageable);
    }

    @Override
    public Page<Order> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByOrderDateBetween(startDate, endDate, pageable);
    }

    @Override
    public Long getUserOrderCount(UUID userId) {
        Integer deliveredId = getDeliveredStatusId();
        return orderRepository.countByUserIdAndStatus_StatusId(userId, deliveredId);
    }

    @Override
    public BigDecimal getUserTotalSpent(UUID userId) {
        Integer deliveredId = getDeliveredStatusId();
        BigDecimal total = orderRepository.sumTotalAmountByUserIdAndStatus_StatusId(userId, deliveredId);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void updateOrderStatus(UUID orderId, Integer newStatusId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

        OrderStatus newStatus = orderStatusRepository.findById(newStatusId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid status ID: " + newStatusId));

        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Override
    public Page<Order> getOrdersByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, Integer statusId, Pageable pageable) {
        return orderRepository.findByOrderDateBetweenAndStatus_StatusId(startDate, endDate, statusId, pageable);
    }

    private BigDecimal calculateTotalAmount(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemsCount = order.getOrderItems().stream()
                .mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity())
                .sum();

        double discountPercent = itemsCount >= 10 ? 10.0 : (itemsCount > 5 ? 8.0 : 0.0);
        order.setDiscountApplied(discountPercent);
        BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discount);

        String deliveryMethodName = order.getDeliveryMethod() != null ? order.getDeliveryMethod().getMethodName() : "";
        boolean selfPickup = deliveryMethodName != null && deliveryMethodName.toLowerCase().contains("pickup");
        int deliveryPercent = 0;
        if (!selfPickup) {
            long roundedDistance = Math.round(resolveDistance(order));
            deliveryPercent = roundedDistance <= 150 ? 5 : 8;
        }
        BigDecimal deliveryExtra = afterDiscount.multiply(BigDecimal.valueOf(deliveryPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return afterDiscount.add(deliveryExtra).setScale(2, RoundingMode.HALF_UP);
    }

    private double resolveDistance(Order order) {
        if (order == null || order.getSourceWarehouse() == null || order.getShippingCity() == null
                || order.getSourceWarehouse().getCity() == null) {
            return 0.0;
        }
        Integer fromCityId = order.getSourceWarehouse().getCity().getCityId();
        Integer toCityId = order.getShippingCity().getCityId();
        if (fromCityId == null || toCityId == null) return 0.0;
        if (fromCityId.equals(toCityId)) return 0.0;
        try {
            var route = cityRouteService.findShortestRouteBFSById(fromCityId, toCityId);
            if (route != null && route.getTotalDistance() != null) {
                return route.getTotalDistance().doubleValue();
            }
        } catch (Exception ignored) {
        }
        return 0.0;
    }

    private Integer getDeliveredStatusId() {
        return orderStatusRepository.findByStatusName("DELIVERED")
                .map(OrderStatus::getStatusId)
                .orElseThrow(() -> new IllegalStateException("Status 'DELIVERED' not found"));
    }
}