package com.estore.library.service;

import com.estore.library.model.bisentity.AdminProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AdminProfileService {

    boolean hasPermission(UUID adminUserId, String permission);
    
    // Проверки доступа по отделам
    boolean hasAnalyticsAccess(UUID adminUserId);
    boolean hasOrderManagementAccess(UUID adminUserId);
    boolean hasProductManagementAccess(UUID adminUserId);
    boolean hasUserManagementAccess(UUID adminUserId);
    
    // Получение списка доступных эндпоинтов
    List<String> getAvailableEndpoints(UUID adminUserId);

    AdminProfile createProfile(AdminProfile profile);
    
    AdminProfile updateProfile(UUID userId, AdminProfile profile);
    
    void deleteProfile(UUID userId);
    
    Optional<AdminProfile> getProfileById(UUID userId);
    
    Page<AdminProfile> getAllProfiles(Pageable pageable);
    
    Page<AdminProfile> getProfilesByDepartment(Integer departmentId, Pageable pageable);
    
    Page<AdminProfile> getProfilesByDepartmentName(String departmentName, Pageable pageable);
    
    Page<AdminProfile> getProfilesByHireDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    Page<AdminProfile> searchProfilesByName(String search, Pageable pageable);
    
    List<AdminProfile> getAllOrderedByHireDate();

}
