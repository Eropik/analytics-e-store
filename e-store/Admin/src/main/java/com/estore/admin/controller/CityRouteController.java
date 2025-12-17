package com.estore.admin.controller;

import com.estore.library.dto.city.CityRouteDto;
import com.estore.library.dto.city.RouteSummaryDto;
import com.estore.library.dto.city.RouteSummaryProjection;
import com.estore.library.model.bisentity.CityRoute;
import com.estore.library.model.dicts.City;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.CityRouteService;
import com.estore.library.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Контроллер для управления городами и маршрутами
 * Доступ: PRODUCT_MANAGE + ORDER_MANAGE (логистика)
 */
@RestController
@RequestMapping("/api/admin/cities")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class CityRouteController {
    
    private final AdminProfileService adminProfileService;
    private final CityService cityService;
    private final CityRouteService cityRouteService;
    
    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasProductManagementAccess(adminUserId) ||
               adminProfileService.hasOrderManagementAccess(adminUserId);
    }
    
    // ========== ГОРОДА ==========
    
    /**
     * Получить все города
     * GET /api/admin/cities
     */
    @GetMapping
    public ResponseEntity<?> getAllCities(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<City> cities = cityService.getAllCities();
            return ResponseEntity.ok(cities);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Создать город
     * POST /api/admin/cities
     */
    @PostMapping
    public ResponseEntity<?> createCity(
            @RequestParam UUID adminUserId,
            @RequestBody City city) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. PRODUCT_MANAGE required"));
            }
            
            City created = cityService.createCity(city);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "city", created));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить город
     * PUT /api/admin/cities/{cityId}
     */
    @PutMapping("/{cityId}")
    public ResponseEntity<?> updateCity(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId,
            @RequestBody City city) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // 1. Получаем старый город для извлечения имени
            Optional<City> oldCityOptional = cityService.getCityById(cityId);

            // 2. Корректно извлекаем старое имя или устанавливаем заглушку
            String oldCityName = oldCityOptional
                    .map(City::getCityName) // Используем геттер для получения имени
                    .orElse("Город не найден перед обновлением"); // Более осмысленная заглушка

            // 3. Выполняем обновление
            City updated = cityService.updateCity(cityId, city);

            // 4. Корректно формируем Map.of()
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "oldCityName", oldCityName, // Новый ключ для старого имени
                    "updatedCity", updated      // Новый ключ для обновленного объекта
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Удалить город
     * DELETE /api/admin/cities/{cityId}
     */
    @DeleteMapping("/{cityId}")
    public ResponseEntity<?> deleteCity(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            cityService.deleteCity(cityId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "City deleted"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Поиск городов
     * GET /api/admin/cities/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchCities(
            @RequestParam UUID adminUserId,
            @RequestParam String query) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            List<City> cities = cityService.searchCities(query);
            return ResponseEntity.ok(cities);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== МАРШРУТЫ МЕЖДУ ГОРОДАМИ ==========
    
    /**
     * Получить все маршруты
     * GET /api/admin/cities/routes
     */
    @GetMapping("/routes")
    public ResponseEntity<?> getAllRoutes(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            


            List<CityRoute> routes = cityRouteService.getAllRoutes();

            // 2. Преобразуем List<CityRoute> в List<CityRouteDto>
            List<CityRouteDto> routeDtos = routes.stream()
                    .map(CityRouteDto::fromEntity)
                    .toList();

            return ResponseEntity.ok(routeDtos); // Возвращаем DTO

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Создать маршрут
     * POST /api/admin/cities/routes
     */
    @PostMapping("/routes")
    public ResponseEntity<?> createRoute(
            @RequestParam UUID adminUserId,
            @RequestBody CityRoute route) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. PRODUCT_MANAGE required"));
            }
            
            CityRoute created = cityRouteService.createRoute(route);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "route", created));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить маршрут
     * PUT /api/admin/cities/routes/{routeId}
     */
    @PutMapping("/routes/{routeId}")
    public ResponseEntity<?> updateRoute(
            @RequestParam UUID adminUserId,
            @PathVariable Integer routeId,
            @RequestBody CityRoute route) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            CityRoute updated = cityRouteService.updateRoute(routeId, route);
            
            return ResponseEntity.ok(Map.of("success", true, "route", updated));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Удалить маршрут
     * DELETE /api/admin/cities/routes/{routeId}
     */
    @DeleteMapping("/routes/{routeId}")
    public ResponseEntity<?> deleteRoute(
            @RequestParam UUID adminUserId,
            @PathVariable Integer routeId) {
        
        try {
            if (!adminProfileService.hasProductManagementAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            cityRouteService.deleteRoute(routeId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Route deleted"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Прямые маршруты между городами
     * GET /api/admin/cities/routes/direct
     */
    @GetMapping("/routes/direct")
    public ResponseEntity<?> getDirectRoutes(
            @RequestParam UUID adminUserId,
            @RequestParam Integer cityAId,
            @RequestParam Integer cityBId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<CityRoute> routes =
                    cityRouteService.getDirectRoutesBetweenCities(cityAId, cityBId);

            // Преобразование в DTO с вложенной структурой
            List<CityRouteDto> dtos = routes.stream()
                    .map(CityRouteDto::fromEntity)
                    .toList();

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Маршруты из города
     * GET /api/admin/cities/{cityId}/routes/from
     */
    @GetMapping("/{cityId}/routes/from")
    public ResponseEntity<?> getRoutesFromCity(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<CityRoute> routes = cityRouteService.getDirectRoutesFromCity(cityId);

            // Преобразование в DTO
            List<CityRouteDto> dtos = routes.stream()
                    .map(CityRouteDto::fromEntity)
                    .toList();

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Маршруты в город
     * GET /api/admin/cities/{cityId}/routes/to
     */
    @GetMapping("/{cityId}/routes/to")
    public ResponseEntity<?> getRoutesToCity(
            @RequestParam UUID adminUserId,
            @PathVariable Integer cityId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<CityRoute> routes = cityRouteService.getDirectRoutesToCity(cityId);

            // Преобразование в DTO
            List<CityRouteDto> dtos = routes.stream()
                    .map(CityRouteDto::fromEntity)
                    .toList();

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Найти все маршруты из города (BFS)
     * GET /api/admin/cities/routes/bfs/all
     * * Примечание: Возвращает List<Object[]>, форматирование которого уже выполнено в контроллере.
     * Данный формат ответа (List<Map<String, Object>>) является приемлемым для результатов BFS.
     */
    @GetMapping("/routes/bfs/all")
    public ResponseEntity<?> findAllRoutesBFS(
            @RequestParam UUID adminUserId,
            @RequestParam String startCityName) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<Object[]> routes = cityRouteService.findAllRoutesBFS(startCityName);

            // Форматируем результат (эта логика остается, так как она специфична для BFS)
            List<Map<String, Object>> formatted = new ArrayList<>();
            for (Object[] route : routes) {
                Map<String, Object> routeMap = new HashMap<>();
                routeMap.put("destinationCity", route[0]);
                routeMap.put("totalDistance", route[1]);
                routeMap.put("numberOfStops", route[2]);
                routeMap.put("path", route[3]);
                formatted.add(routeMap);
            }

            return ResponseEntity.ok(formatted);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }




    /**
     * Маршруты в пределах расстояния
     * GET /api/admin/cities/routes/within-distance
     */
    @GetMapping("/routes/within-distance")
    public ResponseEntity<?> getRoutesWithinDistance(
            @RequestParam UUID adminUserId,
            @RequestParam BigDecimal maxDistance) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            List<CityRoute> routes = cityRouteService.getRoutesWithinDistance(maxDistance);

            // Преобразование в DTO
            List<CityRouteDto> dtos = routes.stream()
                    .map(CityRouteDto::fromEntity)
                    .toList();

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/routes/bfs/shortest-by-id")
    public ResponseEntity<?> findShortestRouteBFSById(
            @RequestParam UUID adminUserId,
            @RequestParam Integer startCityId,
            @RequestParam Integer endCityId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Вызываем сервис, который возвращает готовый DTO
            RouteSummaryDto summaryDto = cityRouteService.findShortestRouteBFSById(startCityId, endCityId);

            if (summaryDto == null) {
                return ResponseEntity.ok(Map.of("found", false, "message", "No route found"));
            }

            // Если найден, возвращаем готовый DTO. Spring автоматически преобразует его в JSON.
            return ResponseEntity.ok(summaryDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Найти кратчайший маршрут между городами (BFS)
     * GET /api/admin/cities/routes/bfs/shortest-by-name
     */
    @GetMapping("/routes/bfs/shortest-by-name")
    public ResponseEntity<?> findShortestRouteBFSByName(
            @RequestParam UUID adminUserId,
            @RequestParam String startCityName,
            @RequestParam String endCityName) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Вызываем сервис, который теперь возвращает RouteSummaryDto
            RouteSummaryDto summaryDto = cityRouteService.findShortestRouteBFSByName(startCityName, endCityName);

            if (summaryDto == null) {
                return ResponseEntity.ok(Map.of("found", false, "message", "No route found"));
            }

            // Возвращаем готовый DTO
            return ResponseEntity.ok(summaryDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }



}
