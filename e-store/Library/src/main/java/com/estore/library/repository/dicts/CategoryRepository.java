package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    Optional<Category> findByCategoryName(String categoryName);
    
    boolean existsByCategoryName(String categoryName);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.categoryName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Category> searchByCategoryName(@Param("search") String search);
    
    @Query("SELECT c FROM Category c ORDER BY c.categoryName ASC")
    List<Category> findAllOrderByCategoryNameAsc();
}
