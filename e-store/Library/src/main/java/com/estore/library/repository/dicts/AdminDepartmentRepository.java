package com.estore.library.repository.dicts;

import com.estore.library.model.dicts.AdminDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminDepartmentRepository extends JpaRepository<AdminDepartment, Integer> {
    
    Optional<AdminDepartment> findByDepartmentName(String departmentName);
    
    boolean existsByDepartmentName(String departmentName);
    
    @Query("SELECT ad FROM AdminDepartment ad ORDER BY ad.departmentName ASC")
    List<AdminDepartment> findAllOrderByDepartmentNameAsc();
}
