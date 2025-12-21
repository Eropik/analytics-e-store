package com.estore.admin.controller;

import com.estore.library.model.bisentity.Warehouse;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Контроллер для управления складами и логистикой
 * Доступ: PRODUCT_MANAGE + ORDER_MANAGE (логистика)
 */
@RestController
@RequestMapping("/api/admin/warehouses")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class WarehouseController {
    
    private final AdminProfileService adminProfileService;
    private final WarehouseService warehouseService;
    
    /**
     * Проверка доступа: PRODUCT_MANAGE или ORDER_MANAGE
     */
    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasProductManagementAccess(adminUserId) ||
               adminProfileService.hasOrderManagementAccess(adminUserId);
    }
    
    /**
     * Получить все склады
     * GET /api/admin/warehouses
     */
    @GetMapping
    public ResponseEntity<?> getAllWarehouses(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<Warehouse> warehouses = warehouseService.getAllWarehouses();
            return ResponseEntity.ok(warehouses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить склад по ID
     * GET /api/admin/warehouses/{warehouseId}
     */
    @GetMapping("/{warehouseId}")
    public ResponseEntity<?> getWarehouseById(
            @RequestParam UUID adminUserId,
            @PathVariable Long warehouseId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Optional<Warehouse> warehouse = warehouseService.getWarehouseById(warehouseId);
            if (warehouse.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(warehouse.get());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Создать склад
     * POST /api/admin/warehouses
     */
    @PostMapping
    public ResponseEntity<?> createWarehouse(
            @RequestParam UUID adminUserId,
            @RequestBody Warehouse warehouse) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Warehouse created = warehouseService.createWarehouse(warehouse);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "warehouse", created));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить склад
     * PUT /api/admin/warehouses/{warehouseId}
     */
    @PutMapping("/{warehouseId}")
    public ResponseEntity<?> updateWarehouse(
            @RequestParam UUID adminUserId,
            @PathVariable Long warehouseId,
            @RequestBody Warehouse warehouse) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Warehouse updated = warehouseService.updateWarehouse(warehouseId, warehouse);
            
            return ResponseEntity.ok(Map.of("success", true, "warehouse", updated));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Удалить склад
     * DELETE /api/admin/warehouses/{warehouseId}
     */
    @DeleteMapping("/{warehouseId}")
    public ResponseEntity<?> deleteWarehouse(
            @RequestParam UUID adminUserId,
            @PathVariable Long warehouseId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            warehouseService.deleteWarehouse(warehouseId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Warehouse deleted"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Поиск складов
     * GET /api/admin/warehouses/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchWarehouses(
            @RequestParam UUID adminUserId,
            @RequestParam String query) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<Warehouse> warehouses = warehouseService.searchWarehouses(query);
            return ResponseEntity.ok(warehouses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Склады по городу
     * GET /api/admin/warehouses/city/{cityId}
     */
    @GetMapping("/city/{cityId}")
    public ResponseEntity<?> getWarehousesByCity(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<Warehouse> warehouses = warehouseService.getWarehousesByCity(cityId);
            return ResponseEntity.ok(warehouses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== ЛОГИСТИКА И МАРШРУТЫ ==========
    
    /**
     * Найти ближайший склад к городу
     * GET /api/admin/warehouses/nearest/{cityId}
     */
    @GetMapping("/nearest/{cityId}")
    public ResponseEntity<?> findNearestWarehouse(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Map<String, Object> result = warehouseService.findNearestWarehouseToCity(cityId);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Склады в пределах расстояния от города
     * GET /api/admin/warehouses/within-distance
     */
    @GetMapping("/within-distance")
    public ResponseEntity<?> findWarehousesWithinDistance(
            @RequestParam UUID adminUserId,
            @RequestParam Integer cityId,
            @RequestParam Double maxDistance) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<Map<String, Object>> warehouses = 
                warehouseService.findWarehousesWithinDistance(cityId, maxDistance);
            
            return ResponseEntity.ok(warehouses);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Маршрут между складами
     * GET /api/admin/warehouses/route
     */
    @GetMapping("/route")
    public ResponseEntity<?> findRouteBetweenWarehouses(
            @RequestParam UUID adminUserId,
            @RequestParam Long fromWarehouseId,
            @RequestParam Long toWarehouseId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Map<String, Object> route = 
                warehouseService.findRouteBetweenWarehouses(fromWarehouseId, toWarehouseId);
            
            return ResponseEntity.ok(route);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Оптимальный склад для доставки
     * GET /api/admin/warehouses/optimal-delivery/{cityId}
     */
    @GetMapping("/optimal-delivery/{cityId}")
    public ResponseEntity<?> findOptimalWarehouse(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Map<String, Object> result = 
                warehouseService.findOptimalWarehouseForDelivery(cityId);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Расчет стоимости доставки
     * GET /api/admin/warehouses/delivery-cost
     */
    @GetMapping("/delivery-cost")
    public ResponseEntity<?> calculateDeliveryCost(
            @RequestParam UUID adminUserId,
            @RequestParam Long fromWarehouseId,
            @RequestParam Long toWarehouseId,
            @RequestParam(defaultValue = "10.0") Double pricePerKm) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Double cost = warehouseService.calculateDeliveryCost(
                fromWarehouseId, toWarehouseId, pricePerKm);
            
            return ResponseEntity.ok(Map.of("deliveryCost", cost, "pricePerKm", pricePerKm));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Достижимые склады
     * GET /api/admin/warehouses/{warehouseId}/reachable
     */
    @GetMapping("/{warehouseId}/reachable")
    public ResponseEntity<?> findReachableWarehouses(
            @RequestParam UUID adminUserId,
            @PathVariable Long warehouseId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<Map<String, Object>> reachable = 
                warehouseService.findReachableWarehouses(warehouseId);
            
            return ResponseEntity.ok(reachable);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
