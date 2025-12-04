package com.estore.customer.controller;

import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.OrderItem;
import com.estore.library.service.*;
import com.estore.library.model.dicts.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/customer/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class OrderController {
    
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CityRouteService cityRouteService;
    private final DeliveryMethodService deliveryMethodService;
    private final PaymentMethodService paymentMethodService;
    private final WarehouseService warehouseService;
    
    /**
     * Создать новый заказ
     * POST /api/customer/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            Order order = new Order();
            order.setUser(request.getUser());
            order.setShippingCity(request.getShippingCity());
            order.setShippingAddressText(request.getShippingAddress());
            order.setDeliveryMethod(request.getDeliveryMethod());
            order.setPaymentMethod(request.getPaymentMethod());
            order.setDiscountApplied(request.getDiscountApplied());
            order.setOrderItems(request.getItems());
            
            Order createdOrder = orderService.createOrder(order);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Order created successfully");
            response.put("orderId", createdOrder.getId());
            response.put("totalAmount", createdOrder.getTotalAmount());
            response.put("status", createdOrder.getStatus());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить заказы пользователя
     * GET /api/customer/orders/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserOrders(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Sort sort = Sort.by("orderDate").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Order> ordersPage = orderService.getOrdersByUserId(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", ordersPage.getContent());
            response.put("currentPage", ordersPage.getNumber());
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить детали заказа
     * GET /api/customer/orders/{orderId}
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderDetails(@PathVariable UUID orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found"));
            }
            
            Order order = orderOpt.get();
            List<OrderItem> items = orderItemService.getOrderItemsByOrderId(orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("order", order);
            response.put("items", items);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    

    /**
     * Получить статистику заказов пользователя
     * GET /api/customer/orders/user/{userId}/statistics
     */
    @GetMapping("/user/{userId}/statistics")
    public ResponseEntity<?> getUserOrderStatistics(@PathVariable UUID userId) {
        try {
            Long orderCount = orderService.getUserOrderCount(userId);
            BigDecimal totalSpent = orderService.getUserTotalSpent(userId);
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("orderCount", orderCount);
            stats.put("totalSpent", totalSpent);
            stats.put("averageOrderValue", 
                orderCount > 0 
                    ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Фильтр заказов по статусу
     * GET /api/customer/orders/user/{userId}/status/{status}
     */
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getUserOrdersByStatus(
            @PathVariable UUID userId,
            @PathVariable int status_id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Order> ordersPage = orderService.getOrdersByUserIdAndStatus(userId, status_id, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("orders", ordersPage.getContent());
            response.put("currentPage", ordersPage.getNumber());
            response.put("totalItems", ordersPage.getTotalElements());
            response.put("totalPages", ordersPage.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // DTO класс
    public static class CreateOrderRequest {
        private com.estore.library.model.bisentity.User user;
        private com.estore.library.model.dicts.City shippingCity;
        private String shippingAddress;
        private com.estore.library.model.dicts.DeliveryMethod deliveryMethod;
        private com.estore.library.model.dicts.PaymentMethod paymentMethod;
        private Double discountApplied;
        private List<OrderItem> items;
        
        public com.estore.library.model.bisentity.User getUser() { return user; }
        public void setUser(com.estore.library.model.bisentity.User user) { this.user = user; }
        
        public com.estore.library.model.dicts.City getShippingCity() { return shippingCity; }
        public void setShippingCity(com.estore.library.model.dicts.City shippingCity) { 
            this.shippingCity = shippingCity; 
        }
        
        public String getShippingAddress() { return shippingAddress; }
        public void setShippingAddress(String shippingAddress) { 
            this.shippingAddress = shippingAddress; 
        }
        
        public com.estore.library.model.dicts.DeliveryMethod getDeliveryMethod() { 
            return deliveryMethod; 
        }
        public void setDeliveryMethod(com.estore.library.model.dicts.DeliveryMethod deliveryMethod) { 
            this.deliveryMethod = deliveryMethod; 
        }
        
        public com.estore.library.model.dicts.PaymentMethod getPaymentMethod() { 
            return paymentMethod; 
        }
        public void setPaymentMethod(com.estore.library.model.dicts.PaymentMethod paymentMethod) { 
            this.paymentMethod = paymentMethod; 
        }
        
        public Double getDiscountApplied() { return discountApplied; }

        public void setDiscountApplied(Double discountApplied) {
            this.discountApplied = discountApplied; 
        }
        
        public List<OrderItem> getItems() { return items; }
        public void setItems(List<OrderItem> items) { this.items = items; }
    }
    
    // ========== НОВЫЕ ЭНДПОИНТЫ ==========
    
    /**
     * Получить доступные методы доставки
     * GET /api/customer/orders/delivery-methods
     */
    @GetMapping("/delivery-methods")
    public ResponseEntity<?> getDeliveryMethods() {
        try {
            List<DeliveryMethod> methods = deliveryMethodService.getAllDeliveryMethods();
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить доступные методы оплаты
     * GET /api/customer/orders/payment-methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() {
        try {
            List<PaymentMethod> methods = paymentMethodService.getAllPaymentMethods();
            return ResponseEntity.ok(methods);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Отследить статус заказа
     * GET /api/customer/orders/{orderId}/track
     */
    @GetMapping("/{orderId}/track")
    public ResponseEntity<?> trackOrder(@PathVariable UUID orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            
            if (orderOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Order not found"));
            }
            
            Order order = orderOpt.get();
            
            Map<String, Object> trackingInfo = new HashMap<>();
            trackingInfo.put("orderId", order.getId());
            trackingInfo.put("status", order.getStatus());
            trackingInfo.put("orderDate", order.getOrderDate());
            trackingInfo.put("shippingAddressText", order.getShippingAddressText());
            trackingInfo.put("shippingCity", order.getShippingCity().getCityName());
            trackingInfo.put("deliveryMethod", order.getDeliveryMethod().getMethodName());
            trackingInfo.put("totalAmount", order.getTotalAmount());
            
            // Добавим timeline статусов
            List<Map<String, Object>> timeline = new ArrayList<>();
            timeline.add(Map.of(
                "status", "PENDING",
                "description", "Заказ создан",
                "completed", true
            ));
            timeline.add(Map.of(
                "status", "PROCESSING",
                "description", "Заказ обрабатывается",
                "completed", order.getStatus().equals("PROCESSING") || 
                           order.getStatus().equals("SHIPPED") || 
                           order.getStatus().equals("DELIVERED")
            ));
            timeline.add(Map.of(
                "status", "SHIPPED",
                "description", "Заказ отправлен",
                "completed", order.getStatus().equals("SHIPPED") || 
                           order.getStatus().equals("DELIVERED")
            ));
            timeline.add(Map.of(
                "status", "DELIVERED",
                "description", "Заказ доставлен",
                "completed", order.getStatus().equals("DELIVERED")
            ));
            
            trackingInfo.put("timeline", timeline);
            
            return ResponseEntity.ok(trackingInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Рассчитать маршрут и стоимость доставки
     * GET /api/customer/orders/calculate-delivery
     */
    @GetMapping("/calculate-delivery")
    public ResponseEntity<?> calculateDelivery(
            @RequestParam String fromCityName,
            @RequestParam String toCityName) {
        
        try {
            // Найти кратчайший маршрут с помощью BFS
            Object[] route = cityRouteService.findShortestRouteBFS(fromCityName, toCityName);
            
            if (route == null) {
                return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "Маршрут не найден"
                ));
            }
            
            Map<String, Object> deliveryInfo = new HashMap<>();
            deliveryInfo.put("found", true);
            deliveryInfo.put("from", fromCityName);
            deliveryInfo.put("to", toCityName);
            deliveryInfo.put("distance", route[1]);
            deliveryInfo.put("stops", route[2]);
            deliveryInfo.put("route", route[3]);
            
            // Рассчитать примерную стоимость (например, 10 руб/км)
            double distance = ((Number) route[1]).doubleValue();
            double estimatedCost = distance * 10.0;
            deliveryInfo.put("estimatedCost", estimatedCost);
            
            // Примерное время доставки (например, 1 день на 100 км)
            int estimatedDays = (int) Math.ceil(distance / 100.0);
            deliveryInfo.put("estimatedDeliveryDays", estimatedDays);
            
            return ResponseEntity.ok(deliveryInfo);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Найти ближайший склад для доставки
     * GET /api/customer/orders/nearest-warehouse
     */
    @GetMapping("/nearest-warehouse")
    public ResponseEntity<?> findNearestWarehouse(@RequestParam Integer cityId) {
        try {
            Map<String, Object> warehouseInfo = warehouseService.findNearestWarehouseToCity(cityId);
            return ResponseEntity.ok(warehouseInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
