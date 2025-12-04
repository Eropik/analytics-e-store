package com.estore.library.repository;

import com.estore.library.model.bisentity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    
    Optional<Warehouse> findByName(String name);
    
    boolean existsByName(String name);
    
    @Query("SELECT w FROM Warehouse w WHERE w.city.cityId = :cityId")
    List<Warehouse> findByCityId(@Param("cityId") Integer cityId);
    
    @Query("SELECT w FROM Warehouse w WHERE w.city.cityName = :cityName")
    List<Warehouse> findByCityName(@Param("cityName") String cityName);
    
    @Query("SELECT w FROM Warehouse w WHERE LOWER(w.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(w.address) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Warehouse> searchWarehouses(@Param("search") String search);
    
    @Query("SELECT w FROM Warehouse w ORDER BY w.name ASC")
    List<Warehouse> findAllOrderByName();
    
    /**
     * Поиск ближайшего склада к указанному городу
     * Использует BFS для поиска склада с минимальным расстоянием
     */
    @Query(value = """
        WITH RECURSIVE bfs_search AS (
            SELECT
                c.city_id,
                c.city_name,
                0 AS distance,
                ARRAY[c.city_id] AS path_ids
            FROM city c
            WHERE c.city_id = :cityId
            
            UNION ALL
            
            SELECT
                c2.city_id,
                c2.city_name,
                t.distance + cr.distance_km,
                t.path_ids || c2.city_id
            FROM bfs_search t
            JOIN city_route cr ON cr.city_a_id = t.city_id
            JOIN city c2 ON c2.city_id = cr.city_b_id
            WHERE c2.city_id <> ALL(t.path_ids)
        )
        SELECT w.warehouse_id, w.warehouse_name, w.city_id, w.address, bs.distance
        FROM warehouse w
        JOIN bfs_search bs ON bs.city_id = w.city_id
        ORDER BY bs.distance ASC
        LIMIT 1
        """, nativeQuery = true)
    Object[] findNearestWarehouse(@Param("cityId") Integer cityId);
    
    /**
     * Поиск всех складов в пределах указанного расстояния от города
     */
    @Query(value = """
        WITH RECURSIVE bfs_search AS (
            SELECT
                c.city_id,
                c.city_name,
                0.00 AS distance,
                ARRAY[c.city_id] AS path_ids
            FROM city c
            WHERE c.city_id = :cityId
            
            UNION ALL
            
            SELECT
                c2.city_id,
                c2.city_name,
                t.distance + cr.distance_km,
                t.path_ids || c2.city_id
            FROM bfs_search t
            JOIN city_route cr ON cr.city_a_id = t.city_id
            JOIN city c2 ON c2.city_id = cr.city_b_id
            WHERE c2.city_id <> ALL(t.path_ids)
        )
        SELECT w.warehouse_id, w.warehouse_name, w.city_id, w.address, bs.distance
        FROM warehouse w
        JOIN bfs_search bs ON bs.city_id = w.city_id
        WHERE bs.distance <= :maxDistance
        ORDER BY bs.distance ASC
        """, nativeQuery = true)
    List<Object[]> findWarehousesWithinDistance(
            @Param("cityId") Integer cityId,
            @Param("maxDistance") Double maxDistance
    );
    
    /**
     * Поиск маршрута между двумя складами через города
     */
    @Query(value = """
        WITH RECURSIVE route_search AS (
            SELECT
                c1.city_id AS current_city,
                c1.city_name,
                0.00 AS total_distance,
                0 AS hops,
                ARRAY[c1.city_id] AS path_ids,
                c1.city_name AS path_names
            FROM warehouse w1
            JOIN city c1 ON w1.city_id = c1.city_id
            WHERE w1.warehouse_id = :warehouseFromId
            
            UNION ALL
            
            SELECT
                c2.city_id,
                c2.city_name,
                rs.total_distance + cr.distance_km,
                rs.hops + 1,
                rs.path_ids || c2.city_id,
                rs.path_names || ' -> ' || c2.city_name
            FROM route_search rs
            JOIN city_route cr ON cr.city_a_id = rs.current_city
            JOIN city c2 ON c2.city_id = cr.city_b_id
            WHERE c2.city_id <> ALL(rs.path_ids)
        )
        SELECT 
            rs.current_city,
            rs.city_name,
            rs.total_distance,
            rs.hops,
            rs.path_names,
            w2.warehouse_id,
            w2.warehouse_name
        FROM route_search rs
        JOIN warehouse w2 ON w2.city_id = rs.current_city
        WHERE w2.warehouse_id = :warehouseToId
        ORDER BY rs.total_distance ASC, rs.hops ASC
        LIMIT 1
        """, nativeQuery = true)
    Object[] findRouteBetweenWarehouses(
            @Param("warehouseFromId") Long warehouseFromId,
            @Param("warehouseToId") Long warehouseToId
    );
    
    /**
     * Получить все склады, достижимые из указанного склада
     */
    @Query(value = """
        WITH RECURSIVE reachable_cities AS (
            SELECT
                c.city_id,
                c.city_name,
                0.00 AS distance,
                ARRAY[c.city_id] AS path_ids
            FROM warehouse w
            JOIN city c ON w.city_id = c.city_id
            WHERE w.warehouse_id = :warehouseId
            
            UNION ALL
            
            SELECT
                c2.city_id,
                c2.city_name,
                rc.distance + cr.distance_km,
                rc.path_ids || c2.city_id
            FROM reachable_cities rc
            JOIN city_route cr ON cr.city_a_id = rc.city_id
            JOIN city c2 ON c2.city_id = cr.city_b_id
            WHERE c2.city_id <> ALL(rc.path_ids)
        )
        SELECT DISTINCT w.warehouse_id, w.warehouse_name, w.city_id, w.address, rc.distance
        FROM warehouse w
        JOIN reachable_cities rc ON rc.city_id = w.city_id
        WHERE w.warehouse_id != :warehouseId
        ORDER BY rc.distance ASC
        """, nativeQuery = true)
    List<Object[]> findReachableWarehouses(@Param("warehouseId") Long warehouseId);
}
