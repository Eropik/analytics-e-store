package com.estore.library.service.impl;

import com.estore.library.model.bisentity.Warehouse;
import com.estore.library.repository.WarehouseRepository;
import com.estore.library.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    
    @Override
    @Transactional
    public Warehouse createWarehouse(Warehouse warehouse) {
        if (warehouseRepository.existsByName(warehouse.getName())) {
            throw new IllegalArgumentException("Warehouse with name '" + warehouse.getName() + "' already exists");
        }
        return warehouseRepository.save(warehouse);
    }
    
    @Override
    @Transactional
    public Warehouse updateWarehouse(Long warehouseId, Warehouse warehouse) {
        Warehouse existing = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new IllegalArgumentException("Warehouse not found with id: " + warehouseId));
        
        if (!existing.getName().equals(warehouse.getName()) && 
            warehouseRepository.existsByName(warehouse.getName())) {
            throw new IllegalArgumentException("Warehouse name already exists");
        }
        
        existing.setName(warehouse.getName());
        existing.setCity(warehouse.getCity());
        existing.setAddress(warehouse.getAddress());
        
        return warehouseRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteWarehouse(Long warehouseId) {
        if (!warehouseRepository.existsById(warehouseId)) {
            throw new IllegalArgumentException("Warehouse not found with id: " + warehouseId);
        }
        warehouseRepository.deleteById(warehouseId);
    }
    
    @Override
    public Optional<Warehouse> getWarehouseById(Long warehouseId) {
        return warehouseRepository.findById(warehouseId);
    }
    
    @Override
    public Optional<Warehouse> getWarehouseByName(String name) {
        return warehouseRepository.findByName(name);
    }
    
    @Override
    public List<Warehouse> getAllWarehouses() {
        return warehouseRepository.findAllOrderByName();
    }
    
    @Override
    public List<Warehouse> getWarehousesByCity(Integer cityId) {
        return warehouseRepository.findByCityId(cityId);
    }
    
    @Override
    public List<Warehouse> getWarehousesByCityName(String cityName) {
        return warehouseRepository.findByCityName(cityName);
    }
    
    @Override
    public List<Warehouse> searchWarehouses(String search) {
        return warehouseRepository.searchWarehouses(search);
    }
    
    @Override
    public boolean existsByName(String name) {
        return warehouseRepository.existsByName(name);
    }
    
    // ============= ЛОГИКА ПОИСКА МАРШРУТОВ =============
    
    @Override
    public Map<String, Object> findNearestWarehouseToCity(Integer cityId) {
        Object[] result = warehouseRepository.findNearestWarehouse(cityId);
        
        if (result == null) {
            throw new IllegalArgumentException("No warehouse found reachable from city id: " + cityId);
        }
        
        Map<String, Object> warehouseData = new HashMap<>();
        warehouseData.put("warehouseId", result[0]);
        warehouseData.put("warehouseName", result[1]);
        warehouseData.put("cityId", result[2]);
        warehouseData.put("address", result[3]);
        warehouseData.put("distance", result[4]);
        
        return warehouseData;
    }
    
    @Override
    public List<Map<String, Object>> findWarehousesWithinDistance(Integer cityId, Double maxDistance) {
        List<Object[]> results = warehouseRepository.findWarehousesWithinDistance(cityId, maxDistance);
        List<Map<String, Object>> warehouses = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> warehouse = new HashMap<>();
            warehouse.put("warehouseId", row[0]);
            warehouse.put("warehouseName", row[1]);
            warehouse.put("cityId", row[2]);
            warehouse.put("address", row[3]);
            warehouse.put("distance", row[4]);
            warehouses.add(warehouse);
        }
        
        return warehouses;
    }
    
    @Override
    public Map<String, Object> findRouteBetweenWarehouses(Long warehouseFromId, Long warehouseToId) {
        if (warehouseFromId.equals(warehouseToId)) {
            throw new IllegalArgumentException("Source and destination warehouses cannot be the same");
        }
        
        Object[] result = warehouseRepository.findRouteBetweenWarehouses(warehouseFromId, warehouseToId);
        
        if (result == null) {
            throw new IllegalArgumentException(
                "No route found between warehouses " + warehouseFromId + " and " + warehouseToId
            );
        }
        
        Map<String, Object> routeData = new HashMap<>();
        routeData.put("destinationCityId", result[0]);
        routeData.put("destinationCityName", result[1]);
        routeData.put("totalDistance", result[2]);
        routeData.put("hops", result[3]); // количество пересадок
        routeData.put("path", result[4]); // путь через города
        routeData.put("destinationWarehouseId", result[5]);
        routeData.put("destinationWarehouseName", result[6]);
        
        return routeData;
    }
    
    @Override
    public List<Map<String, Object>> findReachableWarehouses(Long warehouseId) {
        List<Object[]> results = warehouseRepository.findReachableWarehouses(warehouseId);
        List<Map<String, Object>> reachableWarehouses = new ArrayList<>();
        
        for (Object[] row : results) {
            Map<String, Object> warehouse = new HashMap<>();
            warehouse.put("warehouseId", row[0]);
            warehouse.put("warehouseName", row[1]);
            warehouse.put("cityId", row[2]);
            warehouse.put("address", row[3]);
            warehouse.put("distance", row[4]);
            reachableWarehouses.add(warehouse);
        }
        
        return reachableWarehouses;
    }
    
    @Override
    public Map<String, Object> findOptimalWarehouseForDelivery(Integer deliveryCityId) {
        // Находим ближайший склад к городу доставки
        return findNearestWarehouseToCity(deliveryCityId);
    }
    
    @Override
    public Double calculateDeliveryCost(Long warehouseFromId, Long warehouseToId, Double pricePerKm) {
        if (pricePerKm == null || pricePerKm <= 0) {
            throw new IllegalArgumentException("Price per km must be greater than zero");
        }
        
        Map<String, Object> route = findRouteBetweenWarehouses(warehouseFromId, warehouseToId);
        
        Object distanceObj = route.get("totalDistance");
        Double distance;
        
        // Обработка разных типов данных для distance
        if (distanceObj instanceof BigDecimal) {
            distance = ((BigDecimal) distanceObj).doubleValue();
        } else if (distanceObj instanceof Double) {
            distance = (Double) distanceObj;
        } else if (distanceObj instanceof Number) {
            distance = ((Number) distanceObj).doubleValue();
        } else {
            throw new IllegalStateException("Unexpected distance type: " + distanceObj.getClass());
        }
        
        return distance * pricePerKm;
    }
}
