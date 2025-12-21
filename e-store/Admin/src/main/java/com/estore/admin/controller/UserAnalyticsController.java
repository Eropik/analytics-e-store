package com.estore.admin.controller;

import com.estore.library.dto.analyze.dto.AgeGroupAnalysisDto;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.AnalyzeService;
import com.estore.library.service.CustomerProfileService;
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
@RequestMapping("/api/admin/analytics/user")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class UserAnalyticsController {

    private final AdminProfileService adminProfileService;
    private final AnalyzeService analyzeService;
    private final CustomerProfileService customerProfileService;

    private boolean hasUserAccess(UUID adminUserId) {
        return adminProfileService.hasUserManagementAccess(adminUserId)
                || adminProfileService.hasAnalyticsAccess(adminUserId);
    }

    @GetMapping("/overview")
    public ResponseEntity<?> userOverview(@RequestParam UUID adminUserId) {
        if (!hasUserAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. USER_MANAGE or ANALYZE department required"));
        }
        return ResponseEntity.ok(Map.of(
                "ageBuckets", analyzeService.getAgeBuckets5y(),
                "loginByHour", analyzeService.getLoginByHourLast30d()
        ));
    }

    @GetMapping("/customers")
    public ResponseEntity<?> customers(@RequestParam UUID adminUserId) {
        if (!hasUserAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. USER_MANAGE or ANALYZE department required"));
        }
        Pageable pageable = PageRequest.of(0, 10);
        var topSpenders = customerProfileService.getTopSpenders(pageable);
        var mostActive = customerProfileService.getMostActiveCustomers(pageable);

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("topSpenders", topSpenders.getContent());
        analytics.put("mostActiveCustomers", mostActive.getContent());
        return ResponseEntity.ok(analytics);
    }

    @GetMapping("/age-groups")
    public ResponseEntity<?> ageGroups(@RequestParam UUID adminUserId) {
        if (!hasUserAccess(adminUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. USER_MANAGE or ANALYZE department required"));
        }
        List<AgeGroupAnalysisDto> analysis = analyzeService.getAgeGroupAnalysis();
        return ResponseEntity.ok(Map.of("ageGroupAnalysis", analysis));
    }
}

