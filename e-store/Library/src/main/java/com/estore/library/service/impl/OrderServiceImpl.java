package com.estore.library.service.impl;
import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.OrderItem;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.bisentity.OrderRepository;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusRepository orderStatusRepository;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        order.setOrderDate(new Date());

        // Устанавливаем статус по умолчанию (PROCESSING)
        OrderStatus defaultStatus = orderStatusRepository.findByStatusName("PROCESSING")
                .orElseThrow(() -> new IllegalStateException("Default status 'PROCESSING' not found"));
        order.setStatus(defaultStatus);

        // Вычисляем общую сумму заказа
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

        // Пересчитываем сумму если изменились элементы
        BigDecimal totalAmount = calculateTotalAmount(existingOrder);
        existingOrder.setTotalAmount(totalAmount);

        return orderRepository.save(existingOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(UUID orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new IllegalArgumentException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
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
    public Page<Order> getOrdersByCity(Integer cityId, Pageable pageable) {
        return orderRepository.findByShippingCityId(cityId, pageable);
    }

    @Override
    public Page<Order> getOrdersByDeliveryMethod(Integer methodId, Pageable pageable) {
        return orderRepository.findByDeliveryMethodId(methodId, pageable);
    }

    @Override
    public Page<Order> getOrdersByPaymentMethod(Integer methodId, Pageable pageable) {
        return orderRepository.findByPaymentMethodId(methodId, pageable);
    }

    @Override
    public Page<Order> getOrdersByStatuses(List<Integer> statusIds, Pageable pageable) {
        return orderRepository.findByStatus_StatusIdIn(statusIds, pageable);
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





    /**
     * 🚀 НОВАЯ РЕАЛИЗАЦИЯ: Фильтрация по дате и статусу
     */
    @Override
    public Page<Order> getOrdersByDateRangeAndStatus(LocalDateTime startDate, LocalDateTime endDate, Integer statusId, Pageable pageable) {
        return orderRepository.findByOrderDateBetweenAndStatus_StatusId(startDate, endDate, statusId, pageable);
    }

    @Override
    public BigDecimal calculateOrderTotal(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));
        return calculateTotalAmount(order);
    }

    private BigDecimal calculateTotalAmount(Order order) {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = order.getOrderItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Применяем скидку если есть
        if (order.getDiscountApplied() != null && order.getDiscountApplied() > 0.0) {
            BigDecimal discount = subtotal.multiply(BigDecimal.valueOf(order.getDiscountApplied())).divide(BigDecimal.valueOf(100));
            subtotal = subtotal.subtract(discount);
        }

        return subtotal;
    }

    private Integer getDeliveredStatusId() {
        return orderStatusRepository.findByStatusName("DELIVERED")
                .map(OrderStatus::getStatusId)
                .orElseThrow(() -> new IllegalStateException("Status 'DELIVERED' not found"));
    }
}