package com.estore.customer.controller;

import com.estore.library.model.bisentity.Order;
import com.estore.library.model.bisentity.OrderItem;
import com.estore.library.model.bisentity.Product;
import com.estore.library.model.bisentity.User;
import com.estore.library.model.dicts.*;
import com.estore.library.service.*;
import com.estore.library.repository.dicts.OrderStatusRepository;
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
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8020", "null"})
public class OrderController {
    
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final CityRouteService cityRouteService;
    private final DeliveryMethodService deliveryMethodService;
    private final PaymentMethodService paymentMethodService;
    private final WarehouseService warehouseService;
    private final ProductService productService;
    private final UserService userService;
    private final CityService cityService;
    private final OrderStatusRepository orderStatusRepository;
    private final com.estore.library.service.ShoppingCartService shoppingCartService;
    
    /**
     * Создать новый заказ
     * POST /api/customer/orders
     */
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            User user = userService.getUserById(request.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getUserId()));
            City city = cityService.getCityById(request.getShippingCityId())
                    .orElseThrow(() -> new IllegalArgumentException("City not found: " + request.getShippingCityId()));
            DeliveryMethod deliveryMethod = deliveryMethodService.getDeliveryMethodById(request.getDeliveryMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("Delivery method not found: " + request.getDeliveryMethodId()));
            PaymentMethod paymentMethod = paymentMethodService.getPaymentMethodById(request.getPaymentMethodId())
                    .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + request.getPaymentMethodId()));

            Order order = new Order();
            order.setUser(user);
            order.setShippingCity(city);
            order.setShippingAddressText(request.getShippingAddressText());
            order.setDeliveryMethod(deliveryMethod);
            order.setPaymentMethod(paymentMethod);
            order.setDiscountApplied(request.getDiscountApplied());
            order.setOrderItems(new ArrayList<>());

            if (request.getItems() != null) {
                for (CreateOrderItem itemReq : request.getItems()) {
                    Product product = productService.getProductById(itemReq.getProductId())
                            .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.getProductId()));
                    int available = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    int newStock = available - itemReq.getQuantity();
                    if (newStock < 0) newStock = 0; // не даём уйти в минус, но заказ оформляем

                    OrderItem item = new OrderItem();
                    item.setOrder(order);
                    item.setProduct(product);
                    item.setQuantity(itemReq.getQuantity());
                    item.setUnitPrice(product.getPrice());
                    order.getOrderItems().add(item);

                    // уменьшение склада
                    product.setStockQuantity(newStock);
                    productService.updateProduct(product.getProductId(), product);
                }
            }
            
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
     * Отмена заказа (возврат остатков)
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable UUID orderId, @RequestParam UUID userId) {
        try {
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
            if (!order.getUser().getUserId().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
            }

            List<OrderItem> items = orderItemService.getOrderItemsByOrderId(orderId);
            for (OrderItem item : items) {
                Product product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                productService.updateProduct(product.getProductId(), product);
            }

            OrderStatus cancelled = orderStatusRepository.findByStatusName("CANCELLED")
                    .orElseThrow(() -> new IllegalStateException("Status 'CANCELLED' not found"));
            order.setStatus(cancelled);
            orderService.updateOrder(orderId, order);

            return ResponseEntity.ok(Map.of("success", true, "message", "Order cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Список способов доставки (публично)
     */
    @GetMapping("/delivery-methods")
    public ResponseEntity<?> getDeliveryMethods() {
        try {
            return ResponseEntity.ok(deliveryMethodService.getAllDeliveryMethods());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Список способов оплаты (публично)
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethods() {
        try {
            return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods());
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
        private UUID userId;
        private Integer shippingCityId;
        private String shippingAddressText;
        private Integer deliveryMethodId;
        private Integer paymentMethodId;
        private Double discountApplied;
        private List<CreateOrderItem> items;

        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        public Integer getShippingCityId() { return shippingCityId; }
        public void setShippingCityId(Integer shippingCityId) { this.shippingCityId = shippingCityId; }
        public String getShippingAddressText() { return shippingAddressText; }
        public void setShippingAddressText(String shippingAddressText) { this.shippingAddressText = shippingAddressText; }
        public Integer getDeliveryMethodId() { return deliveryMethodId; }
        public void setDeliveryMethodId(Integer deliveryMethodId) { this.deliveryMethodId = deliveryMethodId; }
        public Integer getPaymentMethodId() { return paymentMethodId; }
        public void setPaymentMethodId(Integer paymentMethodId) { this.paymentMethodId = paymentMethodId; }
        public Double getDiscountApplied() { return discountApplied; }
        public void setDiscountApplied(Double discountApplied) { this.discountApplied = discountApplied; }
        public List<CreateOrderItem> getItems() { return items; }
        public void setItems(List<CreateOrderItem> items) { this.items = items; }
    }

    public static class CreateOrderItem {
        private UUID productId;
        private Integer quantity;
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
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
