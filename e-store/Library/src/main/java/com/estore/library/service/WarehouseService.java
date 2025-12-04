package com.estore.library.service;

import com.estore.library.model.bisentity.Warehouse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WarehouseService {
    
    Warehouse createWarehouse(Warehouse warehouse);
    
    Warehouse updateWarehouse(Long warehouseId, Warehouse warehouse);
    
    void deleteWarehouse(Long warehouseId);
    
    Optional<Warehouse> getWarehouseById(Long warehouseId);
    
    Optional<Warehouse> getWarehouseByName(String name);
    
    List<Warehouse> getAllWarehouses();
    
    List<Warehouse> getWarehousesByCity(Integer cityId);
    
    List<Warehouse> getWarehousesByCityName(String cityName);
    
    List<Warehouse> searchWarehouses(String search);
    
    boolean existsByName(String name);
    
    // ============= ЛОГИКА ПОИСКА МАРШРУТОВ =============
    
    /**
     * Найти ближайший склад к указанному городу
     * @param cityId ID города
     * @return Map с данными склада и расстоянием
     */
    Map<String, Object> findNearestWarehouseToCity(Integer cityId);
    
    /**
     * Найти все склады в пределах указанного расстояния от города
     * @param cityId ID города
     * @param maxDistance максимальное расстояние в км
     * @return Список складов с расстояниями
     */
    List<Map<String, Object>> findWarehousesWithinDistance(Integer cityId, Double maxDistance);
    
    /**
     * Найти оптимальный маршрут между двумя складами
     * @param warehouseFromId ID склада отправления
     * @param warehouseToId ID склада назначения
     * @return Map с данными маршрута (расстояние, количество пересадок, путь)
     */
    Map<String, Object> findRouteBetweenWarehouses(Long warehouseFromId, Long warehouseToId);
    
    /**
     * Получить все склады, достижимые из указанного склада
     * @param warehouseId ID склада
     * @return Список достижимых складов с расстояниями
     */
    List<Map<String, Object>> findReachableWarehouses(Long warehouseId);
    
    /**
     * Найти ближайший склад с товаром к указанному адресу доставки
     * @param deliveryCityId ID города доставки
     * @return Map с данными ближайшего склада
     */
    Map<String, Object> findOptimalWarehouseForDelivery(Integer deliveryCityId);
    
    /**
     * Рассчитать стоимость доставки между складами
     * @param warehouseFromId ID склада отправления
     * @param warehouseToId ID склада назначения
     * @param pricePerKm цена за километр
     * @return стоимость доставки
     */
    Double calculateDeliveryCost(Long warehouseFromId, Long warehouseToId, Double pricePerKm);
}
