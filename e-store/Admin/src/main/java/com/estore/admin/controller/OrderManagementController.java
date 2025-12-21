package com.estore.admin.controller;

import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.User;
import com.estore.library.model.bisentity.Warehouse;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.OrderService;
import com.estore.library.service.OrderItemService;
import com.estore.library.service.UserService;
import com.estore.library.service.WarehouseService;
import com.estore.library.service.CityRouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для отдела ORDER_MANAGE
 * Доступ: управление заказами, изменение статусов, отмена
 */
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class OrderManagementController {

    private final AdminProfileService adminProfileService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final OrderStatusRepository orderStatusRepository;
    private final UserService userService;
    private final WarehouseService warehouseService;
    private final CityRouteService cityRouteService;

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
                    "orders", orders.getContent().stream().map(this::convertToDto).collect(Collectors.toList()),
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
                    "order", convertToDto(orderOpt.get()),
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
                    "orders", orders.getContent().stream().map(this::convertToDto).collect(Collectors.toList()),
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
                    "orders", orders.getContent().stream().map(this::convertToDto).collect(Collectors.toList()),
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

            return ResponseEntity.ok(orders.getContent().stream().map(this::convertToDto).collect(Collectors.toList()));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Обновить логистику заказа: склад, дата доставки, статус (опционально)
     * PUT /api/admin/orders/{orderId}/logistics
     */
    @PutMapping("/{orderId}/logistics")
    public ResponseEntity<?> updateLogistics(
            @RequestParam UUID adminUserId,
            @PathVariable UUID orderId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
            @RequestParam(required = false) Integer statusId
    ) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));

            if (warehouseId != null) {
                Warehouse wh = warehouseService.getWarehouseById(warehouseId)
                        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found"));
                order.setSourceWarehouse(wh);
            }

            if (deliveryDate != null) {
                order.setActualDeliveryDate(java.sql.Date.valueOf(deliveryDate));
            }

            if (statusId != null) {
                OrderStatus newStatus = orderStatusRepository.findById(statusId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid status ID"));
                order.setStatus(newStatus);
            }

            orderService.updateOrder(orderId, order);
            return ResponseEntity.ok(Map.of("success", true, "order", convertToDto(order)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private Map<String, Object> convertToDto(Order order) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("orderId", order.getId());
        dto.put("orderDate", order.getOrderDate());
        Integer statusId = null;
        String statusName = null;
        if (order.getStatus() != null) {
            statusId = order.getStatus().getStatusId();
            statusName = orderStatusRepository.findById(statusId)
                    .map(OrderStatus::getStatusName)
                    .orElse(order.getStatus().getStatusName());
        }
        dto.put("statusId", statusId);
        dto.put("statusName", statusName);
        dto.put("totalAmount", order.getTotalAmount());
        dto.put("shippingAddressText", order.getShippingAddressText());
        dto.put("actualDeliveryDate", order.getActualDeliveryDate());
        if (order.getShippingCity() != null) {
            dto.put("shippingCityId", order.getShippingCity().getCityId());
            dto.put("shippingCityName", order.getShippingCity().getCityName());
        }
        if (order.getDeliveryMethod() != null) {
            Map<String, Object> deliveryMethod = new HashMap<>();
            deliveryMethod.put("methodId", order.getDeliveryMethod().getMethodId());
            deliveryMethod.put("methodName", order.getDeliveryMethod().getMethodName());
            deliveryMethod.put("description", order.getDeliveryMethod().getDescription());
            dto.put("deliveryMethod", deliveryMethod);
        }
        if (order.getPaymentMethod() != null) {
            Map<String, Object> paymentMethod = new HashMap<>();
            paymentMethod.put("methodId", order.getPaymentMethod().getMethodId());
            paymentMethod.put("methodName", order.getPaymentMethod().getMethodName());
            paymentMethod.put("description", order.getPaymentMethod().getDescription());
            dto.put("paymentMethod", paymentMethod);
        }
        if (order.getSourceWarehouse() != null) {
            Warehouse w = order.getSourceWarehouse();
            Map<String, Object> wh = new HashMap<>();
            wh.put("warehouseId", w.getId());
            wh.put("warehouseName", w.getName());
            wh.put("address", w.getAddress());
            if (w.getCity() != null) {
                wh.put("cityId", w.getCity().getCityId());
                wh.put("cityName", w.getCity().getCityName());
            }
            dto.put("sourceWarehouse", wh);
            // distance
            if (order.getShippingCity() != null && w.getCity() != null) {
                try {
                    Integer warehouseCityId = w.getCity().getCityId();
                    Integer shippingCityId = order.getShippingCity().getCityId();
                    
                    // Если города одинаковые, расстояние = 0
                    if (warehouseCityId != null && warehouseCityId.equals(shippingCityId)) {
                        dto.put("distanceKm", 0.0);
                        dto.put("routePath", w.getCity().getCityName());
                    } else if (warehouseCityId != null && shippingCityId != null) {
                        // Используем метод по ID, который правильно обрабатывает двунаправленные маршруты
                        var routeSummary = cityRouteService.findShortestRouteBFSById(
                                warehouseCityId,
                                shippingCityId
                        );
                        if (routeSummary != null && routeSummary.getTotalDistance() != null) {
                            dto.put("distanceKm", routeSummary.getTotalDistance());
                            if (routeSummary.getPathName() != null) {
                                dto.put("routePath", routeSummary.getPathName());
                            }
                        } else {
                            // Если не найден через BFS, попробуем найти прямой маршрут
                            var directRoute = cityRouteService.getDirectRoutesBetweenCities(
                                    warehouseCityId, shippingCityId
                            );
                            if (!directRoute.isEmpty()) {
                                var route = directRoute.get(0);
                                dto.put("distanceKm", route.getDistanceKm());
                                dto.put("routePath", route.getCityA().getCityName() + " -> " + route.getCityB().getCityName());
                            } else {
                                // Маршрут не найден
                                dto.put("distanceKm", null);
                                dto.put("routePath", "Маршрут не найден");
                            }
                        }
                    }
                } catch (Exception e) {
                    // Логируем ошибку, но не прерываем выполнение
                    System.err.println("Error calculating distance: " + e.getMessage());
                    e.printStackTrace();
                    dto.put("distanceKm", null);
                    dto.put("routePath", "Ошибка вычисления маршрута: " + e.getMessage());
                }
            }
        }
        User u = order.getUser();
        if (u != null) {
            Map<String, Object> userDto = new HashMap<>();
            userDto.put("userId", u.getUserId());
            userDto.put("email", u.getEmail());
            userDto.put("firstName", u.getCustomerProfile() != null ? u.getCustomerProfile().getFirstName() : u.getAdminProfile() != null ? u.getAdminProfile().getFirstName() : null);
            userDto.put("lastName", u.getCustomerProfile() != null ? u.getCustomerProfile().getLastName() : u.getAdminProfile() != null ? u.getAdminProfile().getLastName() : null);
            userDto.put("phoneNumber", u.getCustomerProfile() != null ? u.getCustomerProfile().getPhoneNumber() : null);
            userDto.put("role", u.getRole());
            dto.put("user", userDto);
            dto.put("userEmail", u.getEmail());
        }
        return dto;
    }

    /**
     * Получить краткую информацию о пользователе (для ORDER_MANAGE без доступа к USER_MANAGE)
     * GET /api/admin/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBasic(
            @RequestParam UUID adminUserId,
            @PathVariable UUID userId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ORDER_MANAGE department required"));
            }
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
            }
            User u = userOpt.get();
            Map<String, Object> dto = new HashMap<>();
            dto.put("userId", u.getUserId());
            dto.put("email", u.getEmail());
            dto.put("role", u.getRole());
            if (u.getCustomerProfile() != null) {
                dto.put("firstName", u.getCustomerProfile().getFirstName());
                dto.put("lastName", u.getCustomerProfile().getLastName());
                dto.put("phoneNumber", u.getCustomerProfile().getPhoneNumber());
                dto.put("gender", u.getCustomerProfile().getGender());
                if (u.getCustomerProfile().getCity() != null) {
                    dto.put("cityName", u.getCustomerProfile().getCity().getCityName());
                }
            }
            if (u.getAdminProfile() != null) {
                dto.put("firstName", u.getAdminProfile().getFirstName());
                dto.put("lastName", u.getAdminProfile().getLastName());
                if (u.getAdminProfile().getDepartment() != null) {
                    dto.put("department", u.getAdminProfile().getDepartment().getDepartmentName());
                }
            }
            dto.put("isActive", u.getIsActive());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}