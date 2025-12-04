package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.AdminProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdminProfileRepository extends JpaRepository<AdminProfile, UUID> {
    
    @Query("SELECT ap FROM AdminProfile ap WHERE ap.department.departmentId = :departmentId")
    Page<AdminProfile> findByDepartmentId(@Param("departmentId") Integer departmentId, Pageable pageable);
    
    @Query("SELECT ap FROM AdminProfile ap WHERE ap.department.departmentName = :departmentName")
    Page<AdminProfile> findByDepartmentName(@Param("departmentName") String departmentName, Pageable pageable);
    
    @Query("SELECT ap FROM AdminProfile ap WHERE ap.hireDate >= :startDate AND ap.hireDate <= :endDate")
    Page<AdminProfile> findByHireDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
    
    @Query("SELECT ap FROM AdminProfile ap WHERE LOWER(ap.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(ap.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<AdminProfile> searchByName(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT ap FROM AdminProfile ap ORDER BY ap.hireDate ASC")
    List<AdminProfile> findAllOrderByHireDateAsc();
}
