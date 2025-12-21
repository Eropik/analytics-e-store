package com.estore.admin.controller;

import com.estore.library.dto.analyze.dto.ForecastDto;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.AnalyzeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/analytics/analyze")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class AnalyzeAnalyticsController {

    private final AdminProfileService adminProfileService;
    private final AnalyzeService analyzeService;

    private boolean hasAnalyzeAccess(UUID adminUserId) {
        return adminProfileService.hasAnalyticsAccess(adminUserId);
    }

    @GetMapping
    public ResponseEntity<?> analyzeGeneric(@RequestParam UUID adminUserId,
                                            @RequestParam(defaultValue = "products") String scope,
                                            @RequestParam(required = false) String gender,
                                            @RequestParam(required = false) String ageGroup,
                                            @RequestParam(required = false) Integer month) {
        if (!hasAnalyzeAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ANALYZE department required"));
        }
        return ResponseEntity.ok(Map.of("result", analyzeService.analyzeGeneric(scope, gender, ageGroup, month)));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestParam UUID adminUserId) {
        if (!hasAnalyzeAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ANALYZE department required"));
        }
        // Легкий прокси: берем сводку через сервисы
        return ResponseEntity.ok(Map.of(
                "salesLast30d", analyzeService.getRevenueByMonthLastYear(),
                "topBrands", analyzeService.getTopBrands(),
                "topCategories", analyzeService.getTopCategories()
        ));
    }

    @GetMapping("/sales/forecast")
    public ResponseEntity<?> salesForecast(@RequestParam UUID adminUserId,
                                           @RequestParam Integer categoryId,
                                           @RequestParam(defaultValue = "3") int windowSize) {
        if (!hasAnalyzeAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. ANALYZE department required"));
        }
        ForecastDto forecast = analyzeService.getMonthlySalesForecast(categoryId, windowSize);
        return ResponseEntity.ok(Map.of("forecast", forecast));
    }
}

