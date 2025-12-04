package com.estore.customer.controller;

import com.estore.library.model.bisentity.CustomerProfile;
import com.estore.library.model.dicts.City;
import com.estore.library.service.CustomerProfileService;
import com.estore.library.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/profile")
@RequiredArgsConstructor
/*@CrossOrigin(origins = "http://localhost:3000")*/
@CrossOrigin(origins = {"http://localhost:8020", "null"})
public class CustomerProfileController {
    
    private final CustomerProfileService customerProfileService;
    private final CityService cityService;
    
    /**
     * Получить профиль клиента
     * GET /api/customer/profile/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getProfile(@PathVariable UUID userId) {
        try {
            Optional<CustomerProfile> profileOpt = customerProfileService.getProfileById(userId);
            
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Profile not found"));
            }
            
            CustomerProfile profile = profileOpt.get();
            
            Map<String, Object> response = new HashMap<>();
            response.put("userId", profile.getUserId());
            response.put("firstName", profile.getFirstName());
            response.put("lastName", profile.getLastName());
            response.put("phoneNumber", profile.getPhoneNumber());
            response.put("dateOfBirth", profile.getDateOfBirth());
            response.put("profilePictureUrl", profile.getProfilePictureUrl());
            response.put("totalSpent", profile.getTotalSpent());
            response.put("ordersCount", profile.getOrdersCount());
            
            if (profile.getCity() != null) {
                response.put("cityId", profile.getCity().getCityId());
                response.put("cityName", profile.getCity().getCityName());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить профиль клиента
     * PUT /api/customer/profile/{userId}
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateProfile(@PathVariable UUID userId, 
                                          @RequestBody UpdateProfileRequest request) {
        try {
            Optional<CustomerProfile> existingProfileOpt = customerProfileService.getProfileById(userId);
            
            if (existingProfileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Profile not found"));
            }
            
            CustomerProfile profile = existingProfileOpt.get();
            
            // Обновление полей
            if (request.getFirstName() != null) {
                profile.setFirstName(request.getFirstName());
            }
            if (request.getLastName() != null) {
                profile.setLastName(request.getLastName());
            }
            if (request.getPhoneNumber() != null) {
                profile.setPhoneNumber(request.getPhoneNumber());
            }
            if (request.getDateOfBirth() != null) {
                profile.setDateOfBirth(request.getDateOfBirth());
            }
            if (request.getProfilePictureUrl() != null) {
                profile.setProfilePictureUrl(request.getProfilePictureUrl());
            }
            if (request.getCityId() != null) {
                Optional<City> cityOpt = cityService.getCityById(request.getCityId());
                cityOpt.ifPresent(profile::setCity);
            }
            
            CustomerProfile updated = customerProfileService.updateProfile(userId, profile);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profile updated successfully");
            response.put("profile", buildProfileResponse(updated));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить статистику клиента
     * GET /api/customer/profile/{userId}/statistics
     */
    @GetMapping("/{userId}/statistics")
    public ResponseEntity<?> getStatistics(@PathVariable UUID userId) {
        try {
            Optional<CustomerProfile> profileOpt = customerProfileService.getProfileById(userId);
            
            if (profileOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Profile not found"));
            }
            
            CustomerProfile profile = profileOpt.get();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSpent", profile.getTotalSpent());
            stats.put("ordersCount", profile.getOrdersCount());
            stats.put("averageOrderValue", 
                profile.getOrdersCount() > 0 
                    ? profile.getTotalSpent().divide(
                        java.math.BigDecimal.valueOf(profile.getOrdersCount()), 
                        2, 
                        java.math.RoundingMode.HALF_UP
                    )
                    : java.math.BigDecimal.ZERO
            );
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Проверить доступность номера телефона
     * GET /api/customer/profile/check-phone?phone=+1234567890
     */
    @GetMapping("/check-phone")
    public ResponseEntity<?> checkPhone(@RequestParam String phone) {
        Optional<CustomerProfile> profile = customerProfileService.getProfileByPhoneNumber(phone);
        return ResponseEntity.ok(Map.of(
            "exists", profile.isPresent(),
            "available", profile.isEmpty()
        ));
    }
    
    // Вспомогательный метод
    private Map<String, Object> buildProfileResponse(CustomerProfile profile) {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", profile.getUserId());
        response.put("firstName", profile.getFirstName());
        response.put("lastName", profile.getLastName());
        response.put("phoneNumber", profile.getPhoneNumber());
        response.put("dateOfBirth", profile.getDateOfBirth());
        response.put("profilePictureUrl", profile.getProfilePictureUrl());
        response.put("totalSpent", profile.getTotalSpent());
        response.put("ordersCount", profile.getOrdersCount());
        
        if (profile.getCity() != null) {
            response.put("cityId", profile.getCity().getCityId());
            response.put("cityName", profile.getCity().getCityName());
        }
        
        return response;
    }
    
    // DTO класс
    public static class UpdateProfileRequest {
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private LocalDate dateOfBirth;
        private String profilePictureUrl;
        private Integer cityId;
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public LocalDate getDateOfBirth() { return dateOfBirth; }
        public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
        
        public String getProfilePictureUrl() { return profilePictureUrl; }
        public void setProfilePictureUrl(String profilePictureUrl) { 
            this.profilePictureUrl = profilePictureUrl; 
        }
        
        public Integer getCityId() { return cityId; }
        public void setCityId(Integer cityId) { this.cityId = cityId; }
    }
}
