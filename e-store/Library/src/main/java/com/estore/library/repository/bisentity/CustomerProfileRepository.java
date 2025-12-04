package com.estore.library.repository.bisentity;

import com.estore.library.model.bisentity.CustomerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, UUID> {
    
    Optional<CustomerProfile> findByPhoneNumber(String phoneNumber);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.city.cityId = :cityId")
    Page<CustomerProfile> findByCityId(@Param("cityId") Integer cityId, Pageable pageable);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.totalSpent >= :minAmount")
    Page<CustomerProfile> findByTotalSpentGreaterThanEqual(@Param("minAmount") BigDecimal minAmount, Pageable pageable);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE cp.ordersCount >= :minOrders")
    Page<CustomerProfile> findByOrdersCountGreaterThanEqual(@Param("minOrders") Integer minOrders, Pageable pageable);
    
    @Query("SELECT cp FROM CustomerProfile cp WHERE LOWER(cp.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(cp.lastName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<CustomerProfile> searchByName(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT cp FROM CustomerProfile cp ORDER BY cp.totalSpent DESC")
    Page<CustomerProfile> findTopSpenders(Pageable pageable);
    
    @Query("SELECT cp FROM CustomerProfile cp ORDER BY cp.ordersCount DESC")
    Page<CustomerProfile> findMostActiveCustomers(Pageable pageable);
}
