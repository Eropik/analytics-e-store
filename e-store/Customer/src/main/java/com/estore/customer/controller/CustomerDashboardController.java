package com.estore.customer.controller;

import com.estore.library.model.bisentity.*;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository; // <--- ДОБАВЛЕНО
import com.estore.library.service.*;
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

/**
 * Контроллер личного кабинета клиента
 * Расширенная бизнес-логика для персонализации и статистики
 */
@RestController
@RequestMapping("/api/customer/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8020", "null"})
public class CustomerDashboardController {

    private final CustomerProfileService customerProfileService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ProductService productService;
    private final CityRouteService cityRouteService;
    private final ShoppingCartService shoppingCartService;
    private final OrderStatusRepository orderStatusRepository; // <--- НОВАЯ ЗАВИСИМОСТЬ

    /**
     * Вспомогательный метод для получения ID статуса по имени
     */
    private Integer getOrderStatusId(String statusName) {
        return orderStatusRepository.findByStatusName(statusName)
                .map(OrderStatus::getStatusId)
                .orElseThrow(() -> new IllegalStateException("Status '" + statusName + "' not found in DB"));
    }

    /**
     * Главная панель личного кабинета
     * GET /api/customer/dashboard/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getDashboard(@PathVariable UUID userId) {
        try {
            Optional<CustomerProfile> profileOpt = customerProfileService.getProfileById(userId);

            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Profile not found"));
            }

            CustomerProfile profile = profileOpt.get();

            // Статистика заказов
            // NOTE: Предполагаем, что сервисы getUserOrderCount и getUserTotalSpent
            // уже используют логику фильтрации по статусу (например, DELIVERED)
            Long orderCount = orderService.getUserOrderCount(userId);
            BigDecimal totalSpent = orderService.getUserTotalSpent(userId);

            // Последние заказы
            Pageable pageable = PageRequest.of(0, 5, Sort.by("orderDate").descending());
            Page<Order> recentOrders = orderService.getOrdersByUserId(userId, pageable);

            // Корзина
            Optional<ShoppingCart> cartOpt = shoppingCartService.getCartByUserId(userId);
            Integer cartItemsCount = 0;
            BigDecimal cartTotal = BigDecimal.ZERO;
            if (cartOpt.isPresent()) {
                cartItemsCount = shoppingCartService.getCartItemsCount(cartOpt.get().getCartId());
                cartTotal = shoppingCartService.getCartTotal(cartOpt.get().getCartId());
            }

            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("profile", profile);
            dashboard.put("statistics", Map.of(
                    "orderCount", orderCount,
                    "totalSpent", totalSpent,
                    "averageOrderValue", orderCount > 0
                            ? totalSpent.divide(BigDecimal.valueOf(orderCount), 2, BigDecimal.ROUND_HALF_UP)
                            : BigDecimal.ZERO
            ));
            dashboard.put("recentOrders", recentOrders.getContent());
            dashboard.put("cart", Map.of(
                    "itemsCount", cartItemsCount,
                    "total", cartTotal
            ));

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * История покупок с детализацией
     * GET /api/customer/dashboard/{userId}/purchase-history
     */
    @GetMapping("/{userId}/purchase-history")
    public ResponseEntity<?> getPurchaseHistory(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            Sort sort = Sort.by("orderDate").descending();
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Order> ordersPage = orderService.getOrdersByUserId(userId, pageable);

            List<Map<String, Object>> detailedOrders = new ArrayList<>();

            for (Order order : ordersPage.getContent()) {
                // Предполагаем, что Order имеет метод getId()
                List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());

                Map<String, Object> orderDetails = new HashMap<>();
                orderDetails.put("order", order);
                orderDetails.put("items", items);
                orderDetails.put("itemsCount", items.size());

                detailedOrders.add(orderDetails);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("orders", detailedOrders);
            response.put("currentPage", ordersPage.getNumber());
            response.put("totalPages", ordersPage.getTotalPages());
            response.put("totalItems", ordersPage.getTotalElements());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Рекомендации товаров на основе истории покупок
     * GET /api/customer/dashboard/{userId}/recommendations
     */
    @GetMapping("/{userId}/recommendations")
    public ResponseEntity<?> getRecommendations(@PathVariable UUID userId) {
        try {
            // ... (логика рекомендаций без изменений, так как она не зависит от статусов) ...

            // Получить все товары, которые покупал пользователь
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Order> orders = orderService.getOrdersByUserId(userId, pageable);

            Set<Integer> purchasedCategories = new HashSet<>();
            Set<Integer> purchasedBrands = new HashSet<>();

            for (Order order : orders.getContent()) {
                List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
                for (OrderItem item : items) {
                    Product product = item.getProduct();
                    if (product.getCategory() != null) {
                        purchasedCategories.add(product.getCategory().getCategoryId());
                    }
                    if (product.getBrand() != null) {
                        purchasedBrands.add(product.getBrand().getBrandId());
                    }
                }
            }

            // Рекомендовать топ товары из этих категорий и брендов
            List<Product> recommendations = new ArrayList<>();
            Pageable topProductsPageable = PageRequest.of(0, 10);

            for (Integer categoryId : purchasedCategories) {
                Page<Product> categoryProducts = productService.getProductsByCategory(
                        categoryId, topProductsPageable);
                recommendations.addAll(categoryProducts.getContent());
                if (recommendations.size() >= 10) break;
            }

            // Если недостаточно, добавить топ-рейтинг
            if (recommendations.size() < 10) {
                Page<Product> topRated = productService.getTopRatedProducts(topProductsPageable);
                recommendations.addAll(topRated.getContent());
            }

            // Убрать дубликаты
            Set<Product> uniqueRecommendations = new LinkedHashSet<>(recommendations);
            List<Product> finalRecommendations = new ArrayList<>(uniqueRecommendations)
                    .subList(0, Math.min(10, uniqueRecommendations.size()));

            return ResponseEntity.ok(Map.of(
                    "recommendations", finalRecommendations,
                    "basedOn", Map.of(
                            "categoriesCount", purchasedCategories.size(),
                            "brandsCount", purchasedBrands.size()
                    )
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Адреса доставки пользователя и доступные маршруты
     * GET /api/customer/dashboard/{userId}/delivery-info
     */
    @GetMapping("/{userId}/delivery-info")
    public ResponseEntity<?> getDeliveryInfo(@PathVariable UUID userId) {
        try {
            // ... (логика доставки без изменений, так как она не зависит от статусов) ...

            Optional<CustomerProfile> profileOpt = customerProfileService.getProfileById(userId);

            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Profile not found"));
            }

            CustomerProfile profile = profileOpt.get();

            Map<String, Object> deliveryInfo = new HashMap<>();
            deliveryInfo.put("city", profile.getCity());

            // Получить последние адреса доставки из заказов
            Pageable pageable = PageRequest.of(0, 5);
            Page<Order> recentOrders = orderService.getOrdersByUserId(userId, pageable);

            Set<String> recentAddresses = new LinkedHashSet<>();
            for (Order order : recentOrders.getContent()) {
                recentAddresses.add(order.getShippingAddressText());
            }

            deliveryInfo.put("recentAddresses", new ArrayList<>(recentAddresses));

            // Если есть город профиля, получить доступные маршруты
            if (profile.getCity() != null) {
                String cityName = profile.getCity().getCityName();
                List<Object[]> routes = cityRouteService.findAllRoutesBFS(cityName);

                List<Map<String, Object>> availableRoutes = new ArrayList<>();
                for (Object[] route : routes) {
                    if (availableRoutes.size() >= 5) break;

                    Map<String, Object> routeInfo = new HashMap<>();
                    routeInfo.put("destination", route[0]);
                    routeInfo.put("distance", route[1]);
                    routeInfo.put("stops", route[2]);
                    availableRoutes.add(routeInfo);
                }

                deliveryInfo.put("availableRoutes", availableRoutes);
            }

            return ResponseEntity.ok(deliveryInfo);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Избранные товары (на основе частых покупок)
     * GET /api/customer/dashboard/{userId}/favorites
     */
    @GetMapping("/{userId}/favorites")
    public ResponseEntity<?> getFavorites(@PathVariable UUID userId) {
        try {
            // ... (логика избранного без изменений) ...

            // Получить товары, которые пользователь покупал чаще всего
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            Page<Order> orders = orderService.getOrdersByUserId(userId, pageable);

            Map<UUID, Integer> productPurchaseCount = new HashMap<>();
            Map<UUID, Product> productMap = new HashMap<>();

            for (Order order : orders.getContent()) {
                List<OrderItem> items = orderItemService.getOrderItemsByOrderId(order.getId());
                for (OrderItem item : items) {
                    UUID productId = item.getProduct().getProductId();
                    productPurchaseCount.put(productId,
                            productPurchaseCount.getOrDefault(productId, 0) + item.getQuantity());
                    productMap.put(productId, item.getProduct());
                }
            }

            // Сортировать по количеству покупок
            List<Map.Entry<UUID, Integer>> sortedProducts = new ArrayList<>(productPurchaseCount.entrySet());
            sortedProducts.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            List<Map<String, Object>> favorites = new ArrayList<>();
            for (int i = 0; i < Math.min(10, sortedProducts.size()); i++) {
                UUID productId = sortedProducts.get(i).getKey();
                Integer purchaseCount = sortedProducts.get(i).getValue();
                Product product = productMap.get(productId);

                Map<String, Object> favoriteItem = new HashMap<>();
                favoriteItem.put("product", product);
                favoriteItem.put("purchaseCount", purchaseCount);

                favorites.add(favoriteItem);
            }

            return ResponseEntity.ok(Map.of("favorites", favorites));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🟢 Активные заказы (в процессе доставки) - ИСПРАВЛЕНО
     * GET /api/customer/dashboard/{userId}/active-orders
     */
    @GetMapping("/{userId}/active-orders")
    public ResponseEntity<?> getActiveOrders(@PathVariable UUID userId) {
        try {
            Pageable pageable = PageRequest.of(0, 20);

            // Получаем ID новых активных статусов: PROCESSING (Обработка) и IN_TRANSIT (В пути)
            // Исключаем DELIVERED (Доставлен) и CANCELLED (Отменен)
            Integer processingId = getOrderStatusId("PROCESSING");
            Integer inTransitId = getOrderStatusId("IN_TRANSIT");

            // 1. Получаем заказы в статусе PROCESSING
            Page<Order> processing = orderService.getOrdersByUserIdAndStatus(userId, processingId, pageable);

            // 2. Получаем заказы в статусе IN_TRANSIT
            Page<Order> inTransit = orderService.getOrdersByUserIdAndStatus(userId, inTransitId, pageable);

            List<Order> activeOrders = new ArrayList<>();
            activeOrders.addAll(processing.getContent());
            activeOrders.addAll(inTransit.getContent());

            // Сортировать по дате
            activeOrders.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));

            return ResponseEntity.ok(Map.of(
                    "activeOrders", activeOrders,
                    "count", activeOrders.size()
            ));

        } catch (IllegalStateException e) {
            // Обработка случая, если статус не найден в БД (ошибка конфигурации)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Database Status Configuration Error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}