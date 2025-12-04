package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Page<User> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.roleId = :roleId")
    Page<User> findByRoleId(@Param("roleId") Integer roleId, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role.roleName = :roleName")
    Page<User> findByRoleName(@Param("roleName") String roleName, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.registrationDate >= :startDate AND u.registrationDate <= :endDate")
    Page<User> findByRegistrationDateBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    @Query("SELECT u FROM User u WHERE u.lastLogin >= :date")
    Page<User> findByLastLoginAfter(@Param("date") LocalDateTime date, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchByEmail(@Param("search") String search, Pageable pageable);
}
