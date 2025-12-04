package com.estore.customer.controller;

import com.estore.library.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/cities")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CityController {
    
    private final CityService cityService;
    
    /**
     * Получить все города
     * GET /api/customer/cities
     */
    @GetMapping
    public ResponseEntity<?> getAllCities() {
        try {
            return ResponseEntity.ok(cityService.getAllCities());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Поиск городов
     * GET /api/customer/cities/search?query=Moscow
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCities(@RequestParam String query) {
        try {
            return ResponseEntity.ok(cityService.searchCities(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить город по ID
     * GET /api/customer/cities/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getCityById(@PathVariable Integer id) {
        try {
            return cityService.getCityById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
