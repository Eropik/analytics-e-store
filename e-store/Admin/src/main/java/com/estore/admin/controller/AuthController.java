package com.estore.admin.controller;

import com.estore.library.model.bisentity.AdminProfile;
import com.estore.library.model.bisentity.User;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:8019", "null"})
public class AuthController {
    
    private final UserService userService;
    private final AdminProfileService adminProfileService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Авторизация администратора
     * POST /api/admin/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userService.getUserByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }
            
            User user = userOpt.get();
            
            // Проверка пароля
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid credentials"));
            }
            
            // Проверка роли Admin
            if (!"Admin".equals(user.getRole().getRoleName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. Admin role required"));
            }
            
            // Проверка активности
            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account is inactive"));
            }
            
            // Обновление времени входа
            userService.updateLastLogin(user.getUserId());
            
            // Получение профиля админа
            Optional<AdminProfile> profileOpt = adminProfileService.getProfileById(user.getUserId());
            
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Admin profile not found"));
            }
            
            AdminProfile profile = profileOpt.get();
            String departmentName = profile.getDepartment().getDepartmentName();
            
            // Получение доступных эндпоинтов
            List<String> endpoints = adminProfileService.getAvailableEndpoints(user.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("firstName", profile.getFirstName());
            response.put("lastName", profile.getLastName());
            response.put("department", departmentName);
            response.put("availableEndpoints", endpoints);
            response.put("permissions", Map.of(
                "hasAnalytics", adminProfileService.hasAnalyticsAccess(user.getUserId()),
                "hasOrderManagement", adminProfileService.hasOrderManagementAccess(user.getUserId()),
                "hasProductManagement", adminProfileService.hasProductManagementAccess(user.getUserId()),
                "hasUserManagement", adminProfileService.hasUserManagementAccess(user.getUserId())
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Проверка прав доступа
     * GET /api/admin/auth/check-permission?permission=ANALYTICS
     */
    @GetMapping("/check-permission")
    public ResponseEntity<?> checkPermission(
            @RequestParam UUID userId,
            @RequestParam String permission) {
        try {
            boolean hasPermission = adminProfileService.hasPermission(userId, permission);
            return ResponseEntity.ok(Map.of("hasPermission", hasPermission));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // DTO
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
