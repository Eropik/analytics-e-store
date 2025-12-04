package com.estore.library.service.impl;

import com.estore.library.model.bisentity.CustomerProfile;
import com.estore.library.repository.bisentity.CustomerProfileRepository;
import com.estore.library.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerProfileServiceImpl implements CustomerProfileService {
    
    private final CustomerProfileRepository customerProfileRepository;
    
    @Override
    @Transactional
    public CustomerProfile createProfile(CustomerProfile profile) {
        UUID userId = profile.getUser().getUserId();

        if (customerProfileRepository.existsById(userId)) {
            throw new IllegalStateException("Profile already exists for user: " + userId);
        }


        profile.setTotalSpent(BigDecimal.ZERO);
        profile.setOrdersCount(0);
        return customerProfileRepository.save(profile);
    }
    
    @Override
    @Transactional
    public CustomerProfile updateProfile(UUID userId, CustomerProfile profile) {
        CustomerProfile existingProfile = customerProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        existingProfile.setFirstName(profile.getFirstName());
        existingProfile.setLastName(profile.getLastName());
        existingProfile.setPhoneNumber(profile.getPhoneNumber());
        existingProfile.setCity(profile.getCity());
        existingProfile.setDateOfBirth(profile.getDateOfBirth());
        existingProfile.setProfilePictureUrl(profile.getProfilePictureUrl());
        
        return customerProfileRepository.save(existingProfile);
    }
    
    @Override
    @Transactional
    public void deleteProfile(UUID userId) {
        if (!customerProfileRepository.existsById(userId)) {
            throw new IllegalArgumentException("Profile not found for user: " + userId);
        }
        customerProfileRepository.deleteById(userId);
    }
    
    @Override
    public Optional<CustomerProfile> getProfileById(UUID userId) {
        return customerProfileRepository.findById(userId);
    }
    
    @Override
    public Optional<CustomerProfile> getProfileByPhoneNumber(String phoneNumber) {
        return customerProfileRepository.findByPhoneNumber(phoneNumber);
    }
    
    @Override
    public Page<CustomerProfile> getAllProfiles(Pageable pageable) {
        return customerProfileRepository.findAll(pageable);
    }
    
    @Override
    public Page<CustomerProfile> getProfilesByCity(Integer cityId, Pageable pageable) {
        return customerProfileRepository.findByCityId(cityId, pageable);
    }
    
    @Override
    public Page<CustomerProfile> getProfilesByMinTotalSpent(BigDecimal minAmount, Pageable pageable) {
        return customerProfileRepository.findByTotalSpentGreaterThanEqual(minAmount, pageable);
    }
    
    @Override
    public Page<CustomerProfile> getProfilesByMinOrdersCount(Integer minOrders, Pageable pageable) {
        return customerProfileRepository.findByOrdersCountGreaterThanEqual(minOrders, pageable);
    }
    
    @Override
    public Page<CustomerProfile> searchProfilesByName(String search, Pageable pageable) {
        return customerProfileRepository.searchByName(search, pageable);
    }
    
    @Override
    public Page<CustomerProfile> getTopSpenders(Pageable pageable) {
        return customerProfileRepository.findTopSpenders(pageable);
    }
    
    @Override
    public Page<CustomerProfile> getMostActiveCustomers(Pageable pageable) {
        return customerProfileRepository.findMostActiveCustomers(pageable);
    }
    
    @Override
    @Transactional
    public void updateTotalSpent(UUID userId, BigDecimal amount) {
        CustomerProfile profile = customerProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        BigDecimal currentTotal = profile.getTotalSpent() != null ? profile.getTotalSpent() : BigDecimal.ZERO;
        profile.setTotalSpent(currentTotal.add(amount));
        
        customerProfileRepository.save(profile);
    }
    
    @Override
    @Transactional
    public void incrementOrdersCount(UUID userId) {
        CustomerProfile profile = customerProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Profile not found for user: " + userId));
        
        Integer currentCount = profile.getOrdersCount() != null ? profile.getOrdersCount() : 0;
        profile.setOrdersCount(currentCount + 1);
        
        customerProfileRepository.save(profile);
    }
}
