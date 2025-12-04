package com.estore.library.service.impl;

import com.estore.library.model.bisentity.AdminProfile;
import com.estore.library.repository.bisentity.AdminProfileRepository;
import com.estore.library.repository.bisentity.UserRepository;
import com.estore.library.service.AdminProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminProfileServiceImpl implements AdminProfileService {

    private final AdminProfileRepository adminProfileRepository;
    // Используем его как AdminDepartmentServiceImpl, чтобы избежать циклической зависимости
    private final AdminDepartmentServiceImpl adminDepartmentService;

    // --- Методы CRUD и поиска (Полностью восстановлены) ---

    @Transactional
    public AdminProfile createProfile(AdminProfile profile) {
        if (adminProfileRepository.existsById(profile.getUserId())) {
            throw new IllegalStateException("Admin profile already exists for user: " + profile.getUserId());
        }

        if (profile.getHireDate() == null) {
            profile.setHireDate(LocalDate.now());
        }

        return adminProfileRepository.save(profile);
    }

    @Transactional
    public AdminProfile updateProfile(UUID userId, AdminProfile profile) {
        AdminProfile existingProfile = adminProfileRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found for user: " + userId));

        existingProfile.setFirstName(profile.getFirstName());
        existingProfile.setLastName(profile.getLastName());
        existingProfile.setDepartment(profile.getDepartment());
        existingProfile.setProfilePictureUrl(profile.getProfilePictureUrl());

        return adminProfileRepository.save(existingProfile);
    }

    @Transactional
    public void deleteProfile(UUID userId) {
        if (!adminProfileRepository.existsById(userId)) {
            throw new IllegalArgumentException("Admin profile not found for user: " + userId);
        }
        adminProfileRepository.deleteById(userId);
    }

    public Optional<AdminProfile> getProfileById(UUID userId) {
        return adminProfileRepository.findById(userId);
    }

    public Page<AdminProfile> getAllProfiles(Pageable pageable) {
        return adminProfileRepository.findAll(pageable);
    }

    @Override
    public Page<AdminProfile> getProfilesByDepartment(Integer departmentId, Pageable pageable) {
        return adminProfileRepository.findByDepartmentId(departmentId, pageable);
    }

    public Page<AdminProfile> getProfilesByDepartmentId(Integer departmentId, Pageable pageable) {
        return adminProfileRepository.findByDepartmentId(departmentId, pageable);
    }

    public Page<AdminProfile> getProfilesByDepartmentName(String departmentName, Pageable pageable) {
        return adminProfileRepository.findByDepartmentName(departmentName, pageable);
    }

    public Page<AdminProfile> getProfilesByHireDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return adminProfileRepository.findByHireDateBetween(startDate, endDate, pageable);
    }

    public Page<AdminProfile> searchProfilesByName(String search, Pageable pageable) {
        return adminProfileRepository.searchByName(search, pageable);
    }

    public List<AdminProfile> getAllOrderedByHireDate() {
        return adminProfileRepository.findAllOrderByHireDateAsc();
    }

    // ===================================
    // ЛОГИКА АВТОРИЗАЦИИ (Исправлена)
    // ===================================

    /**
     * Получает все строковые разрешения (Authorities) для данного администратора.
     * ЭТОТ МЕТОД КРИТИЧЕН для интеграции со Spring Security.
     */
    public Set<String> getAdminPermissions(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found for user: " + adminUserId));

        String departmentName = profile.getDepartment().getDepartmentName();

        // Делегируем получение прав сервису отделов.
        return adminDepartmentService.getDepartmentPermissionsByName(departmentName);
    }

    /**
     * Прямая проверка права доступа.
     */
    public boolean hasPermission(UUID adminUserId, String permission) {
        return getAdminPermissions(adminUserId).contains(permission);
    }
    
    /**
     * Проверка доступа к аналитике (отдел ANALYZE)
     */
    public boolean hasAnalyticsAccess(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));
        
        String deptName = profile.getDepartment().getDepartmentName();
        return AdminDepartmentServiceImpl.DEPT_ANALYZE.equals(deptName);
    }
    
    /**
     * Проверка доступа к управлению заказами (отдел ORDER_MANAGE)
     */
    public boolean hasOrderManagementAccess(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));
        
        String deptName = profile.getDepartment().getDepartmentName();
        return AdminDepartmentServiceImpl.DEPT_ORDER_MANAGE.equals(deptName);
    }
    
    /**
     * Проверка доступа к управлению товарами (отдел PRODUCT_MANAGE)
     */
    public boolean hasProductManagementAccess(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));
        
        String deptName = profile.getDepartment().getDepartmentName();
        return AdminDepartmentServiceImpl.DEPT_PRODUCT_MANAGE.equals(deptName);
    }
    
    /**
     * Проверка доступа к управлению пользователями (отдел USER_MANAGE)
     */
    public boolean hasUserManagementAccess(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));
        String deptName = profile.getDepartment().getDepartmentName();
        return AdminDepartmentServiceImpl.DEPT_USER_MANAGE.equals(deptName);
    }
    
    /**
     * Получить список доступных эндпоинтов для админа на основе его отдела
     */
    public List<String> getAvailableEndpoints(UUID adminUserId) {
        AdminProfile profile = adminProfileRepository.findById(adminUserId)
                .orElseThrow(() -> new IllegalArgumentException("Admin profile not found"));
        
        String deptName = profile.getDepartment().getDepartmentName();
        
        return switch (deptName) {
            case AdminDepartmentServiceImpl.DEPT_ANALYZE -> List.of(
                "/api/admin/analytics/sales",
                "/api/admin/analytics/products",
                "/api/admin/analytics/customers",
                "/api/admin/analytics/orders",
                "/api/admin/analytics/dashboard"
            );
            case AdminDepartmentServiceImpl.DEPT_ORDER_MANAGE -> List.of(
                "/api/admin/orders",
                "/api/admin/orders/{id}",
                "/api/admin/orders/{id}/status",
                "/api/admin/orders/{id}/cancel",
                "/api/admin/orders/status/{status}",
                "/api/admin/orders/period",
                "/api/admin/orders/pending",
                // Логистика
                "/api/admin/warehouses",
                "/api/admin/warehouses/nearest",
                "/api/admin/warehouses/route",
                "/api/admin/warehouses/optimal-delivery",
                "/api/admin/cities",
                "/api/admin/cities/routes",
                "/api/admin/cities/routes/bfs"
            );
            case AdminDepartmentServiceImpl.DEPT_PRODUCT_MANAGE -> List.of(
                "/api/admin/products",
                "/api/admin/products/{id}",
                "/api/admin/products/{id}/stock",
                "/api/admin/products/{id}/images",
                "/api/admin/products/low-stock",
                "/api/admin/products/categories",
                "/api/admin/products/brands",
                // Склады и логистика
                "/api/admin/warehouses",
                "/api/admin/warehouses/{id}",
                "/api/admin/warehouses/nearest",
                "/api/admin/warehouses/route",
                "/api/admin/warehouses/optimal-delivery",
                "/api/admin/warehouses/delivery-cost",
                "/api/admin/cities",
                "/api/admin/cities/{id}",
                "/api/admin/cities/routes",
                "/api/admin/cities/routes/{id}",
                "/api/admin/cities/routes/bfs"
            );
            case AdminDepartmentServiceImpl.DEPT_USER_MANAGE -> List.of(
                "/api/admin/users",
                "/api/admin/users/{id}",
                "/api/admin/users/{id}/activate",
                "/api/admin/users/{id}/deactivate",
                "/api/admin/users/active",
                "/api/admin/users/role/{role}",
                "/api/admin/users/search",
                "/api/admin/users/customers",
                "/api/admin/users/roles"
            );
            default -> List.of();
        };
    }
}
