package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    
    Optional<Brand> findByBrandName(String brandName);
    
    boolean existsByBrandName(String brandName);
    
    @Query("SELECT b FROM Brand b WHERE LOWER(b.brandName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Brand> searchByBrandName(@Param("search") String search);
    
    @Query("SELECT b FROM Brand b ORDER BY b.brandName ASC")
    List<Brand> findAllOrderByBrandNameAsc();
}
