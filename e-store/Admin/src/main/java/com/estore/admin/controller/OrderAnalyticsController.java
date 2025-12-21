package com.estore.admin.controller;

import com.estore.library.dto.analyze.dto.PaymentDeliveryAnalysisDto;
import com.estore.library.dto.analyze.dto.RouteAnalysisDto;
import com.estore.library.model.dicts.OrderStatus;
import com.estore.library.repository.dicts.OrderStatusRepository;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.AnalyzeService;
import com.estore.library.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/analytics/order")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class OrderAnalyticsController {

    private final AdminProfileService adminProfileService;
    private final AnalyzeService analyzeService;
    private final OrderService orderService;
    private final OrderStatusRepository orderStatusRepository;

    private boolean hasOrderAccess(UUID adminUserId) {
        return adminProfileService.hasOrderManagementAccess(adminUserId)
                || adminProfileService.hasAnalyticsAccess(adminUserId);
    }

    private Integer getOrderStatusId(String statusName) {
        return orderStatusRepository.findByStatusName(statusName)
                .map(OrderStatus::getStatusId)
                .orElseThrow(() -> new IllegalStateException("Status '" + statusName + "' not found in DB"));
    }

    @GetMapping("/overview")
    public ResponseEntity<?> orderOverview(@RequestParam UUID adminUserId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        return ResponseEntity.ok(Map.of(
                "topBrands", analyzeService.getTopBrands(),
                "topCategories", analyzeService.getTopCategories(),
                "topProducts", analyzeService.getTopProducts(),
                "revenueByMonth", analyzeService.getRevenueByMonthLastYear(),
                "bestsellersByMonth", analyzeService.getBestsellersByMonth()
        ));
    }

    @GetMapping("/filter")
    public ResponseEntity<?> orderFilter(@RequestParam UUID adminUserId,
                                         @RequestParam(required = false) String status,
                                         @RequestParam(required = false) String gender,
                                         @RequestParam(required = false) String ageGroup,
                                         @RequestParam(required = false) Integer categoryId,
                                         @RequestParam(required = false) Integer brandId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        return ResponseEntity.ok(Map.of(
                "brands", analyzeService.getOrderBrandsByFilter(status, gender, ageGroup, categoryId, brandId),
                "categories", analyzeService.getOrderCategoriesByFilter(status, gender, ageGroup, categoryId, brandId),
                "products", analyzeService.getOrderProductsByFilter(status, gender, ageGroup, categoryId, brandId)
        ));
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders(@RequestParam UUID adminUserId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        Pageable pageable = PageRequest.of(0, 1);
        List<String> statuses = List.of("PROCESSING", "IN_TRANSIT", "DELIVERED");
        Map<String, Long> ordersByStatus = new HashMap<>();
        for (String statusName : statuses) {
            Integer statusId = getOrderStatusId(statusName);
            var orders = orderService.getOrdersByStatus(statusId, pageable);
            ordersByStatus.put(statusName, orders.getTotalElements());
        }
        return ResponseEntity.ok(Map.of("ordersByStatus", ordersByStatus));
    }

    @GetMapping("/payment-methods")
    public ResponseEntity<?> paymentMethods(@RequestParam UUID adminUserId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        List<PaymentDeliveryAnalysisDto> analysis = analyzeService.getPaymentMethodAnalysis();
        return ResponseEntity.ok(Map.of("paymentMethodAnalysis", analysis));
    }

    @GetMapping("/delivery-methods")
    public ResponseEntity<?> deliveryMethods(@RequestParam UUID adminUserId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        List<PaymentDeliveryAnalysisDto> analysis = analyzeService.getDeliveryMethodAnalysis();
        return ResponseEntity.ok(Map.of("deliveryMethodAnalysis", analysis));
    }

    @GetMapping("/routes")
    public ResponseEntity<?> routeAnalytics(@RequestParam UUID adminUserId) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        List<RouteAnalysisDto> analysis = analyzeService.getRouteAnalysis();
        return ResponseEntity.ok(Map.of("routeAnalysis", analysis));
    }

    @GetMapping("/sales")
    public ResponseEntity<?> sales(@RequestParam UUID adminUserId,
                                   @RequestParam(required = false) LocalDateTime startDate,
                                   @RequestParam(required = false) LocalDateTime endDate) {
        if (!hasOrderAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ORDER_MANAGE or ANALYZE department required"));
        }
        if (startDate == null) startDate = LocalDateTime.now().minusDays(30);
        if (endDate == null) endDate = LocalDateTime.now();

        Integer deliveredStatusId = getOrderStatusId("DELIVERED");
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
                        : BigDecimal.ZERO);

        return ResponseEntity.ok(stats);
    }
}

