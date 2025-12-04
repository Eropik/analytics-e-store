package com.estore.library.repository.bisentity;

import com.estore.library.dto.city.RoutePathProjection;
import com.estore.library.dto.city.RouteSummaryProjection;
import com.estore.library.model.bisentity.CityRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CityRouteRepository extends JpaRepository<CityRoute, Integer> {
    
    @Query("SELECT cr FROM CityRoute cr WHERE cr.cityA.cityId = :cityId OR cr.cityB.cityId = :cityId")
    List<CityRoute> findByCityId(@Param("cityId") Integer cityId);

    @Query("SELECT cr FROM CityRoute cr " +
            "JOIN FETCH cr.cityA ca " + // Принудительно загрузить cityA
            "JOIN FETCH cr.cityB cb " + // Принудительно загрузить cityB
            "WHERE cr.cityA.cityId = :cityAId AND cr.cityB.cityId = :cityBId")
    List<CityRoute> findByCityAAndCityB(
            @Param("cityAId") Integer cityAId,
            @Param("cityBId") Integer cityBId
    );


    /**
     * BFS поиск всех маршрутов из начального города
     * Возвращает: [city_name, level-1, total_distance, path_name]
     */
    @Query(value = """
    WITH RECURSIVE bfs_search AS (
        SELECT
            c.city_id,
            c.city_name,
            1 AS level,
            CAST(0.00 AS NUMERIC) AS total_distance,  -- <-- ИСПРАВЛЕНО
            ARRAY[c.city_id] AS path_ids,
            CAST(c.city_name AS TEXT) AS path_name     -- <-- ИСПРАВЛЕНО
        FROM city c
        WHERE c.city_name = :startCityName
        
        UNION ALL
        
        SELECT
            c2.city_id,
            c2.city_name,
            t.level + 1,
            t.total_distance + cr.distance_km,
            t.path_ids || c2.city_id,
            t.path_name || ' -> ' || c2.city_name
        FROM bfs_search t
        JOIN city_route cr ON cr.city_a_id = t.city_id
        JOIN city c2 ON c2.city_id = cr.city_b_id
        WHERE c2.city_id <> ALL(t.path_ids)
    )
    SELECT
        city_name AS city_name,
        level - 1 AS transfers,
        total_distance AS total_distance,
        path_name AS path_name
    FROM bfs_search
    ORDER BY level, total_distance
    """, nativeQuery = true)
    List<Object[]> findAllRoutesBFS(@Param("startCityName") String startCityName);



    @Query("SELECT cr FROM CityRoute cr WHERE cr.distanceKm <= :maxDistance")
    List<CityRoute> findByMaxDistance(@Param("maxDistance") BigDecimal maxDistance);
    
    @Query("SELECT cr FROM CityRoute cr WHERE cr.cityA.cityName = :cityName OR cr.cityB.cityName = :cityName")
    List<CityRoute> findByCityName(@Param("cityName") String cityName);
    

    /**
     * BFS поиск маршрута до конкретного города
     */
    @Query(value = """
        WITH RECURSIVE bfs_search AS (
            SELECT
                c.city_id,
                c.city_name,
                1 AS level,
                0.00 AS total_distance,
                ARRAY[c.city_id] AS path_ids,
                c.city_name AS path_name
            FROM city c
            WHERE c.city_name = :startCityName
            
            UNION ALL
            
            SELECT
                c2.city_id,
                c2.city_name,
                t.level + 1,
                t.total_distance + cr.distance_km,
                t.path_ids || c2.city_id,
                t.path_name || ' -> ' || c2.city_name
            FROM bfs_search t
            JOIN city_route cr ON cr.city_a_id = t.city_id
            JOIN city c2 ON c2.city_id = cr.city_b_id
            WHERE c2.city_id <> ALL(t.path_ids)
        )
        SELECT
            city_name AS city_name,
            level - 1 AS transfers,
            total_distance AS total_distance,
            path_name AS path_name
        FROM bfs_search
        WHERE city_name = :endCityName
        ORDER BY level, total_distance
        LIMIT 1
        """, nativeQuery = true)
    Object[] findShortestRouteBFS(
            @Param("startCityName") String startCityName,
            @Param("endCityName") String endCityName
    );
    
    /**
     * Получить все прямые маршруты из города
     */
    @Query("SELECT cr FROM CityRoute cr WHERE cr.cityA.cityId = :cityId ORDER BY cr.distanceKm ASC")
    List<CityRoute> findDirectRoutesFromCity(@Param("cityId") Integer cityId);
    
    /**
     * Получить все прямые маршруты в город
     */
    @Query("SELECT cr FROM CityRoute cr WHERE cr.cityB.cityId = :cityId ORDER BY cr.distanceKm ASC")
    List<CityRoute> findDirectRoutesToCity(@Param("cityId") Integer cityId);
    
    /**
     * Проверка существования прямого маршрута между городами
     */
    @Query("SELECT CASE WHEN COUNT(cr) > 0 THEN true ELSE false END FROM CityRoute cr " +
           "WHERE (cr.cityA.cityId = :cityAId AND cr.cityB.cityId = :cityBId) " +
           "OR (cr.cityA.cityId = :cityBId AND cr.cityB.cityId = :cityAId)")
    boolean existsDirectRoute(@Param("cityAId") Integer cityAId, @Param("cityBId") Integer cityBId);


    // В CityRouteRepository.java (в методе findShortestRouteBFSById)

    @Query(value = """
    WITH RECURSIVE bfs_search AS (
    SELECT
        c.city_id,
        c.city_name,
        1 AS level,
        CAST(0.00 AS NUMERIC) AS total_distance,
        ARRAY[c.city_id] AS path_ids,
        CAST(c.city_name AS TEXT) AS path_name
    FROM city c
    WHERE c.city_id = :startCityId
    
    UNION ALL
    
    SELECT
        c2.city_id,
        c2.city_name,
        t.level + 1,
        t.total_distance + cr.distance_km,
        t.path_ids || c2.city_id,
        t.path_name || ' -> ' || c2.city_name
    FROM bfs_search t
    JOIN city_route cr ON cr.city_a_id = t.city_id
    JOIN city c2 ON c2.city_id = cr.city_b_id
    WHERE c2.city_id <> ALL(t.path_ids)
)
    SELECT
        city_name AS city_name,
        city_id AS end_city_id,             -- <-- ДОБАВЛЯЕМ ID КОНЕЧНОГО ГОРОДА
        path_ids AS path_ids,               -- <-- ДОБАВЛЯЕМ МАССИВ ID ВСЕГО ПУТИ
        level - 1 AS transfers,
        total_distance AS total_distance,
        path_name AS path_name
    FROM bfs_search
    WHERE city_id = :endCityId
    ORDER BY level, total_distance
    LIMIT 1
    """, nativeQuery = true)
    RoutePathProjection findShortestRouteBFSById(
            @Param("startCityId") Integer startCityId,
            @Param("endCityId") Integer endCityId
    );


    @Query(value = """
    WITH RECURSIVE bfs_search AS (
    -- ... (логика CTE такая же, как раньше, с CAST(0.00 AS NUMERIC) и CAST(c.city_name AS TEXT))
    SELECT
        c.city_id,
        c.city_name,
        1 AS level,
        CAST(0.00 AS NUMERIC) AS total_distance,
        ARRAY[c.city_id] AS path_ids,
        CAST(c.city_name AS TEXT) AS path_name
    FROM city c
    WHERE c.city_name = :startCityName
    
    UNION ALL
    
    SELECT
        c2.city_id,
        c2.city_name,
        t.level + 1,
        t.total_distance + cr.distance_km,
        t.path_ids || c2.city_id,
        t.path_name || ' -> ' || c2.city_name
    FROM bfs_search t
    JOIN city_route cr ON cr.city_a_id = t.city_id
    JOIN city c2 ON c2.city_id = cr.city_b_id
    WHERE c2.city_id <> ALL(t.path_ids)
    
)
    SELECT
        city_name AS city_name,
        city_id AS end_city_id,         -- <-- ДОБАВЛЕНО: ID конечного города
        path_ids AS path_ids,           -- <-- ДОБАВЛЕНО: Массив ID всего пути
        level - 1 AS transfers,
        total_distance AS total_distance,
        path_name AS path_name
    FROM bfs_search
    WHERE city_name = :endCityName
    ORDER BY level, total_distance
    LIMIT 1
    """, nativeQuery = true)
    RoutePathProjection findShortestRouteByName(
            @Param("startCityName") String startCityName,
            @Param("endCityName") String endCityName
    );
}
