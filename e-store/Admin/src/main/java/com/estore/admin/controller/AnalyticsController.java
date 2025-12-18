package com.estore.admin.controller;

import com.estore.library.dto.analyze.dto.*;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для отдела ANALYZE
 * Доступ: только чтение, аналитика, отчеты
 */
@RestController
@RequestMapping("/api/admin/analytics")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")

@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class AnalyticsController {

    private final AdminProfileService adminProfileService;
    private final OrderService orderService;
    private final ProductService productService;
    private final CustomerProfileService customerProfileService;
    private final OrderItemService orderItemService;
    private final OrderStatusRepository orderStatusRepository;
    private final AnalyzeService analyzeService;
    private final LoginLogService loginLogService;

    /**
     * Проверка доступа к аналитике
     */
    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasAnalyticsAccess(adminUserId);
    }

    // ===== Новые аналитические секции =====

    @GetMapping("/product/overview")
    public ResponseEntity<?> productOverview(@RequestParam UUID adminUserId) {
        if (!checkAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(Map.of(
                "categoryShare", analyzeService.getCategoryShare(),
                "brandShare", analyzeService.getBrandShare(),
                "priceBuckets", analyzeService.getPriceBuckets(),
                "topCitiesRoutes", analyzeService.getTopCitiesInRoutes(),
                "routeDistanceBuckets", analyzeService.getRouteDistanceBuckets()
        ));
    }

    @GetMapping("/user/overview")
    public ResponseEntity<?> userOverview(@RequestParam UUID adminUserId) {
        if (!checkAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(Map.of(
                "ageBuckets", analyzeService.getAgeBuckets5y(),
                "loginByHour", analyzeService.getLoginByHourLast30d()
        ));
    }

    @GetMapping("/order/overview")
    public ResponseEntity<?> orderOverview(@RequestParam UUID adminUserId) {
        if (!checkAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(Map.of(
                "topBrands", analyzeService.getTopBrands(),
                "topCategories", analyzeService.getTopCategories(),
                "topProducts", analyzeService.getTopProducts(),
                "revenueByMonth", analyzeService.getRevenueByMonthLastYear(),
                "bestsellersByMonth", analyzeService.getBestsellersByMonth()
        ));
    }

    @GetMapping("/order/filter")
    public ResponseEntity<?> orderFilter(
            @RequestParam UUID adminUserId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId
    ) {
        if (!checkAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(Map.of(
                "brands", analyzeService.getOrderBrandsByFilter(status, gender, ageGroup, categoryId, brandId),
                "categories", analyzeService.getOrderCategoriesByFilter(status, gender, ageGroup, categoryId, brandId),
                "products", analyzeService.getOrderProductsByFilter(status, gender, ageGroup, categoryId, brandId)
        ));
    }

    @GetMapping("/analyze")
    public ResponseEntity<?> analyzeGeneric(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "products") String scope,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) Integer month
    ) {
        if (!checkAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied"));
        }
        return ResponseEntity.ok(Map.of(
                "result", analyzeService.analyzeGeneric(scope, gender, ageGroup, month)
        ));
    }

    /**
     * Вспомогательный метод для получения ID статуса по имени
     */
    private Integer getOrderStatusId(String statusName) {
        return orderStatusRepository.findByStatusName(statusName)
                .map(OrderStatus::getStatusId)
                .orElseThrow(() -> new IllegalStateException("Status '" + statusName + "' not found in DB"));
    }

    /**
     * Общая статистика продаж
     * GET /api/admin/analytics/sales
     */
    @GetMapping("/sales")
    public ResponseEntity<?> getSalesStatistics(
            @RequestParam UUID adminUserId,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            // Если даты не указаны, берем последние 30 дней
            if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
            if (endDate == null) endDate = LocalDateTime.now();

            // Для аналитики продаж, логично считать только доставленные (DELIVERED) заказы
            Integer deliveredStatusId = getOrderStatusId("DELIVERED");

            // Получаем только заказы в статусе DELIVERED
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
            var orders = orderService.getOrdersByDateRangeAndStatus(startDate, endDate, deliveredStatusId, pageable);

            BigDecimal totalRevenue = orders.getContent().stream()
                    .map(order -> order.getTotalAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> stats = new HashMap<>();
            stats.put("period", Map.of("start", startDate, "end", endDate));
            stats.put("totalOrders", orders.getTotalElements());
            stats.put("totalRevenue", totalRevenue);
            stats.put("averageOrderValue",
                    orders.getTotalElements() > 0
                            ? totalRevenue.divide(BigDecimal.valueOf(orders.getTotalElements()), 2, BigDecimal.ROUND_HALF_UP)
                            : BigDecimal.ZERO
            );

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Аналитика по товарам
     * GET /api/admin/analytics/products
     */
    @GetMapping("/products")
    public ResponseEntity<?> getProductAnalytics(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Pageable pageable = PageRequest.of(0, 10);
            var topRated = productService.getTopRatedProducts(pageable);
            var lowStock = productService.getLowStockProducts(10, pageable);
            var newest = productService.getNewestProducts(pageable);

            List<Object[]> topSelling = orderItemService.getTopSellingProducts();

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("topRatedProducts", topRated.getContent());
            analytics.put("lowStockProducts", lowStock.getContent());
            analytics.put("newestProducts", newest.getContent());
            analytics.put("topSellingProducts", topSelling);

            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Аналитика по клиентам
     * GET /api/admin/analytics/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomerAnalytics(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Pageable pageable = PageRequest.of(0, 10);
            var topSpenders = customerProfileService.getTopSpenders(pageable);
            var mostActive = customerProfileService.getMostActiveCustomers(pageable);

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("topSpenders", topSpenders.getContent());
            analytics.put("mostActiveCustomers", mostActive.getContent());

            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 🚀 Статистика по заказам (обновлено)
     * GET /api/admin/analytics/orders
     */
    @GetMapping("/orders")
    public ResponseEntity<?> getOrderAnalytics(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Pageable pageable = PageRequest.of(0, 1); // Используем pageable минимального размера для получения общего количества

            // Используем все возможные статусы из БД (если надо) или заранее известные:
            List<String> statuses = Arrays.asList("PROCESSING", "IN_TRANSIT", "DELIVERED");
            Map<String, Long> ordersByStatus = new HashMap<>();

            for (String statusName : statuses) {
                Integer statusId = getOrderStatusId(statusName);
                // Используем getOrdersByStatus(Integer statusId, Pageable pageable)
                var orders = orderService.getOrdersByStatus(statusId, pageable);
                ordersByStatus.put(statusName, orders.getTotalElements());
            }

            Map<String, Object> analytics = new HashMap<>();
            analytics.put("ordersByStatus", ordersByStatus);

            return ResponseEntity.ok(analytics);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 📈 Dashboard - общая сводка (обновлено)
     * GET /api/admin/analytics/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Собираем данные за последние 30 дней
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            LocalDateTime endDate = LocalDateTime.now();

            Map<String, Object> dashboard = new HashMap<>();
            Pageable pageable = PageRequest.of(0, 5); // Используем pageable

            // Продажи (повторно вызываем метод sales)
            var salesStats = getSalesStatistics(adminUserId, startDate, endDate);
            // Проверка, что запрос getSalesStatistics был успешен
            if (salesStats.getStatusCode() == HttpStatus.OK) {
                dashboard.put("sales", salesStats.getBody());
            } else {
                dashboard.put("sales", Map.of("error", "Failed to load sales stats"));
            }

            // Товары
            dashboard.put("lowStockCount", productService.getLowStockProducts(10, pageable).getTotalElements());

            // Заказы
            // 1. Ожидающие обработки (PROCESSING)
            Integer processingId = getOrderStatusId("PROCESSING");
            dashboard.put("processingOrders", orderService.getOrdersByStatus(processingId, PageRequest.of(0, 1)).getTotalElements());

            // 2. В пути (IN_TRANSIT)
            Integer inTransitId = getOrderStatusId("IN_TRANSIT");
            dashboard.put("inTransitOrders", orderService.getOrdersByStatus(inTransitId, PageRequest.of(0, 1)).getTotalElements());


            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Топ товаров по объему продаж
     * GET /api/admin/analytics/best-sellers
     */
    @GetMapping("/best-sellers")
    public ResponseEntity<?> getBestSellers(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<BestSellerDto> bestSellers = analyzeService.getBestSellers(limit);
            return ResponseEntity.ok(Map.of("bestSellers", bestSellers));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Анализ по категориям и брендам
     * GET /api/admin/analytics/category-brand
     */
    @GetMapping("/category-brand")
    public ResponseEntity<?> getCategoryBrandAnalysis(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<CategoryBrandAnalysisDto> analysis = analyzeService.getCategoryBrandAnalysis();
            return ResponseEntity.ok(Map.of("categoryBrandAnalysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Анализ по возрастным группам покупателей
     * GET /api/admin/analytics/age-groups
     */
    @GetMapping("/age-groups")
    public ResponseEntity<?> getAgeGroupAnalysis(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<AgeGroupAnalysisDto> analysis = analyzeService.getAgeGroupAnalysis();
            return ResponseEntity.ok(Map.of("ageGroupAnalysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Анализ по маршрутам доставки
     * GET /api/admin/analytics/routes
     */
    @GetMapping("/routes")
    public ResponseEntity<?> getRouteAnalysis(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<RouteAnalysisDto> analysis = analyzeService.getRouteAnalysis();
            return ResponseEntity.ok(Map.of("routeAnalysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Анализ по способам оплаты
     * GET /api/admin/analytics/payment-methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getPaymentMethodAnalysis(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<PaymentDeliveryAnalysisDto> analysis = analyzeService.getPaymentMethodAnalysis();
            return ResponseEntity.ok(Map.of("paymentMethodAnalysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Анализ по способам доставки
     * GET /api/admin/analytics/delivery-methods
     */
    @GetMapping("/delivery-methods")
    public ResponseEntity<?> getDeliveryMethodAnalysis(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            List<PaymentDeliveryAnalysisDto> analysis = analyzeService.getDeliveryMethodAnalysis();
            return ResponseEntity.ok(Map.of("deliveryMethodAnalysis", analysis));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Прогноз продаж по категории (скользящее среднее)
     * GET /api/admin/analytics/sales/forecast
     */
    @GetMapping("/sales/forecast")
    public ResponseEntity<?> getSalesForecast(
            @RequestParam UUID adminUserId,
            @RequestParam Integer categoryId,
            @RequestParam(defaultValue = "3") int windowSize) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. ANALYZE department required"));
            }

            ForecastDto forecast = analyzeService.getMonthlySalesForecast(categoryId, windowSize);
            return ResponseEntity.ok(Map.of("forecast", forecast));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}