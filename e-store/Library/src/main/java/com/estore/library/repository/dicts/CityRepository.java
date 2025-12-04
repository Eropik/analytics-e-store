package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.City;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CityRepository extends JpaRepository<City, Integer> {
    
    Optional<City> findByCityName(String cityName);
    
    boolean existsByCityName(String cityName);
    
    @Query("SELECT c FROM City c WHERE LOWER(c.cityName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<City> searchByCityName(@Param("search") String search);
    
    @Query("SELECT c FROM City c ORDER BY c.cityName ASC")
    List<City> findAllOrderByCityNameAsc();
}
