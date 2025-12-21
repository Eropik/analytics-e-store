package com.estore.admin.controller;

import com.estore.library.dto.analyze.dto.BestSellerDto;
import com.estore.library.dto.analyze.dto.CategoryBrandAnalysisDto;
import com.estore.library.dto.analyze.dto.RouteAnalysisDto;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.AnalyzeService;
import com.estore.library.service.OrderItemService;
import com.estore.library.service.ProductService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/analytics/product")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class ProductAnalyticsController {

    private final AdminProfileService adminProfileService;
    private final AnalyzeService analyzeService;
    private final ProductService productService;
    private final OrderItemService orderItemService;

    private boolean hasProductAccess(UUID adminUserId) {
        return adminProfileService.hasProductManagementAccess(adminUserId)
                || adminProfileService.hasAnalyticsAccess(adminUserId);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> productOverview(@RequestParam UUID adminUserId) {
        if (!hasProductAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. PRODUCT_MANAGE or ANALYZE department required"));
        }
        return ResponseEntity.ok(Map.of(
                "categoryShare", analyzeService.getCategoryShare(),
                "brandShare", analyzeService.getBrandShare(),
                "priceBuckets", analyzeService.getPriceBuckets(),
                "topCitiesRoutes", analyzeService.getTopCitiesInRoutes(),
                "routeDistanceBuckets", analyzeService.getRouteDistanceBuckets()
        ));
    }

    @GetMapping("/routes")
    public ResponseEntity<?> routeAnalytics(@RequestParam UUID adminUserId) {
        if (!hasProductAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. PRODUCT_MANAGE or ANALYZE department required"));
        }
        List<RouteAnalysisDto> analysis = analyzeService.getRouteAnalysis();
        return ResponseEntity.ok(Map.of("routeAnalysis", analysis));
    }

    @GetMapping("/category-brand")
    public ResponseEntity<?> categoryBrand(@RequestParam UUID adminUserId) {
        if (!hasProductAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. PRODUCT_MANAGE or ANALYZE department required"));
        }
        List<CategoryBrandAnalysisDto> analysis = analyzeService.getCategoryBrandAnalysis();
        return ResponseEntity.ok(Map.of("categoryBrandAnalysis", analysis));
    }

    @GetMapping("/products")
    public ResponseEntity<?> products(@RequestParam UUID adminUserId) {
        if (!hasProductAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. PRODUCT_MANAGE or ANALYZE department required"));
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
    }

    @GetMapping("/best-sellers")
    public ResponseEntity<?> bestSellers(@RequestParam UUID adminUserId,
                                         @RequestParam(defaultValue = "10") int limit) {
        if (!hasProductAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. PRODUCT_MANAGE or ANALYZE department required"));
        }
        List<BestSellerDto> bestSellers = analyzeService.getBestSellers(limit);
        return ResponseEntity.ok(Map.of("bestSellers", bestSellers));
    }
}

