package com.estore.library.service;

import com.estore.library.model.bisentity.CustomerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface CustomerProfileService {
    
    CustomerProfile createProfile(CustomerProfile profile);
    
    CustomerProfile updateProfile(UUID userId, CustomerProfile profile);
    
    void deleteProfile(UUID userId);
    
    Optional<CustomerProfile> getProfileById(UUID userId);
    
    Optional<CustomerProfile> getProfileByPhoneNumber(String phoneNumber);
    
    Page<CustomerProfile> getAllProfiles(Pageable pageable);
    
    Page<CustomerProfile> getProfilesByCity(Integer cityId, Pageable pageable);
    
    Page<CustomerProfile> getProfilesByMinTotalSpent(BigDecimal minAmount, Pageable pageable);
    
    Page<CustomerProfile> getProfilesByMinOrdersCount(Integer minOrders, Pageable pageable);
    
    Page<CustomerProfile> searchProfilesByName(String search, Pageable pageable);
    
    Page<CustomerProfile> getTopSpenders(Pageable pageable);
    
    Page<CustomerProfile> getMostActiveCustomers(Pageable pageable);
    
    void updateTotalSpent(UUID userId, BigDecimal amount);
    
    void incrementOrdersCount(UUID userId);
}
