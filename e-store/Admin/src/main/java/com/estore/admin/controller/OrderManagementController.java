package com.estore.admin.controller;

import com.estore.library.model.bisentity.Order;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.OrderService;
import com.estore.library.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Контроллер для отдела ORDER_MANAGE
 * Доступ: управление заказами, изменение статусов, отмена
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:8019", "null"})
public class OrderManagementController {

    private final AdminProfileService adminProfileService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderStatusRepository orderStatusRepository;

    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasOrderManagementAccess(adminUserId);
    }

    /**
     * Получить все заказы
     * GET /api/admin/orders
     */
    @GetMapping
    public ResponseEntity<?> getAllOrders(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ORDER_MANAGE department required"));
            }

            Sort sort = Sort.by("orderDate").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Order> orders = orderService.getAllOrders(pageable);

            return ResponseEntity.ok(Map.of(
                    "orders", orders.getContent(),
                    "currentPage", orders.getNumber(),
                    "totalPages", orders.getTotalPages(),
                    "totalItems", orders.getTotalElements()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Получить заказ по ID
     * GET /api/admin/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(
            @RequestParam UUID adminUserId,
            @PathVariable UUID orderId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var items = orderItemService.getOrderItemsByOrderId(orderId);

            return ResponseEntity.ok(Map.of(
                    "order", orderOpt.get(),
                    "items", items
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Обновить статус заказа
     * PUT /api/admin/orders/{orderId}/status
     */
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @RequestParam UUID adminUserId,
            @PathVariable UUID orderId,
            @RequestParam Integer statusId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Валидация статуса ID (проверка существования)
            Optional<OrderStatus> statusOpt = orderStatusRepository.findById(statusId);
            if (statusOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status ID"));
            }

            orderService.updateOrderStatus(orderId, statusId);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Order status updated to " + statusOpt.get().getStatusName()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Фильтр по статусу
     * GET /api/admin/orders/status/{statusId}
     */
    @GetMapping("/status/{statusId}")
    public ResponseEntity<?> getOrdersByStatus(
            @RequestParam UUID adminUserId,
            @PathVariable Integer statusId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Валидация статуса ID
            if (orderStatusRepository.findById(statusId).isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid status ID"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getOrdersByStatus(statusId, pageable);

            return ResponseEntity.ok(Map.of(
                    "orders", orders.getContent(),
                    "currentPage", orders.getNumber(),
                    "totalPages", orders.getTotalPages(),
                    "totalItems", orders.getTotalElements()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Заказы за период
     * GET /api/admin/orders/period
     */
    @GetMapping("/period")
    public ResponseEntity<?> getOrdersByPeriod(
            @RequestParam UUID adminUserId,
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Order> orders = orderService.getOrdersByDateRange(startDate, endDate, pageable);

            return ResponseEntity.ok(Map.of(
                    "orders", orders.getContent(),
                    "currentPage", orders.getNumber(),
                    "totalPages", orders.getTotalPages(),
                    "totalItems", orders.getTotalElements()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Заказы, ожидающие обработки (PROCESSING)
     * GET /api/admin/orders/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingOrders(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Integer processingId = orderStatusRepository.findByStatusName("PROCESSING")
                    .map(OrderStatus::getStatusId)
                    .orElseThrow(() -> new IllegalStateException("Status 'PROCESSING' not found"));

            Pageable pageable = PageRequest.of(0, 50);
            Page<Order> orders = orderService.getOrdersByStatus(processingId, pageable);

            return ResponseEntity.ok(orders.getContent());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}