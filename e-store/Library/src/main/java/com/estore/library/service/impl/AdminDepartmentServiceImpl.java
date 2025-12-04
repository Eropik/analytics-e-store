package com.estore.library.service.impl;

import com.estore.library.model.dicts.AdminDepartment;
import com.estore.library.repository.dicts.AdminDepartmentRepository;
import com.estore.library.service.AdminDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// Предполагаем, что есть интерфейс AdminDepartmentService
public class AdminDepartmentServiceImpl implements AdminDepartmentService {

    private final AdminDepartmentRepository adminDepartmentRepository;

    // Константы отделов (ТОЧНО СООТВЕТСТВУЮТ вашей БД)
    public static final String DEPT_ANALYZE = "ANALYZE";
    public static final String DEPT_ORDER_MANAGE = "ORDER_MANAGE";
    public static final String DEPT_PRODUCT_MANAGE = "PRODUCT_MANAGE";
    public static final String DEPT_USER_MANAGE = "USER_MANAGE";

    // Константы разрешений (Permissions)
    public static class Permissions {
        // Аналитика
        public static final String ANALYTICS_VIEW = "ANALYTICS_VIEW";
        public static final String REPORTS_EXPORT = "REPORTS_EXPORT";
        // Заказы
        public static final String ORDER_VIEW = "ORDER_VIEW";
        public static final String ORDER_UPDATE = "ORDER_UPDATE";
        // Продукты
        public static final String PRODUCT_VIEW = "PRODUCT_VIEW";
        public static final String PRODUCT_CREATE = "PRODUCT_CREATE";
        // Пользователи
        public static final String USER_VIEW = "USER_VIEW";
        public static final String USER_ACTIVATE = "USER_ACTIVATE";
        // Роль (Role)
        public static final String ROLE_ADMIN_ACCESS = "ROLE_ADMIN_ACCESS"; // Право, общее для всех ADMIN
    }

    // ЖЕСТКОЕ МАППИРОВАНИЕ: определяет, какие права у какого отдела есть.
    private static final Map<String, Set<String>> DEPARTMENT_PERMISSIONS = new HashMap<>();

    static {
        // Базовые права, общие для всех ADMIN (для удобства)
        Set<String> baseAdminPermissions = Set.of(Permissions.ROLE_ADMIN_ACCESS);

        // 1. ANALYZE
        Set<String> analyzePerms = new HashSet<>(baseAdminPermissions);
        analyzePerms.addAll(Set.of(
                Permissions.ANALYTICS_VIEW,
                Permissions.REPORTS_EXPORT,
                Permissions.ORDER_VIEW,
                Permissions.PRODUCT_VIEW
        ));
        DEPARTMENT_PERMISSIONS.put(DEPT_ANALYZE, analyzePerms);

        // 2. ORDER_MANAGE
        Set<String> orderPerms = new HashSet<>(baseAdminPermissions);
        orderPerms.addAll(Set.of(
                Permissions.ORDER_VIEW,
                Permissions.ORDER_UPDATE
        ));
        DEPARTMENT_PERMISSIONS.put(DEPT_ORDER_MANAGE, orderPerms);

        // 3. PRODUCT_MANAGE
        Set<String> productPerms = new HashSet<>(baseAdminPermissions);
        productPerms.addAll(Set.of(
                Permissions.PRODUCT_VIEW,
                Permissions.PRODUCT_CREATE
        ));
        DEPARTMENT_PERMISSIONS.put(DEPT_PRODUCT_MANAGE, productPerms);

        // 4. USER_MANAGE
        Set<String> userPerms = new HashSet<>(baseAdminPermissions);
        userPerms.addAll(Set.of(
                Permissions.USER_VIEW,
                Permissions.USER_ACTIVATE
        ));
        DEPARTMENT_PERMISSIONS.put(DEPT_USER_MANAGE, userPerms);
    }

    // ===================================
    // CRUD МЕТОДЫ ДЛЯ ADMIN_DEPARTMENT (Опущены для краткости, т.к. были полными)
    // ===================================
    @Transactional
    public AdminDepartment createDepartment(AdminDepartment department) {
        if (adminDepartmentRepository.existsByDepartmentName(department.getDepartmentName())) {
            throw new IllegalArgumentException("Department already exists: " + department.getDepartmentName());
        }
        return adminDepartmentRepository.save(department);
    }

    @Transactional
    public AdminDepartment updateDepartment(Integer departmentId, AdminDepartment department) {
        AdminDepartment existing = adminDepartmentRepository.findById(departmentId)
                .orElseThrow(() -> new IllegalArgumentException("Department not found with id: " + departmentId));

        String newName = department.getDepartmentName();
        // Проверка, если имя изменилось и уже существует
        if (!existing.getDepartmentName().equals(newName) &&
                adminDepartmentRepository.existsByDepartmentName(newName)) {
            throw new IllegalArgumentException("Department name already exists: " + newName);
        }

        existing.setDepartmentName(newName);
        return adminDepartmentRepository.save(existing);
    }

    @Transactional
    public void deleteDepartment(Integer departmentId) {
        if (!adminDepartmentRepository.existsById(departmentId)) {
            throw new IllegalArgumentException("Department not found with id: " + departmentId);
        }
        adminDepartmentRepository.deleteById(departmentId);
    }

    public Optional<AdminDepartment> getDepartmentById(Integer departmentId) {
        return adminDepartmentRepository.findById(departmentId);
    }

    @Override
    public Optional<AdminDepartment> getDepartmentByName(String departmentName) {
        return adminDepartmentRepository.findByDepartmentName(departmentName);
    }

    @Override
    public List<AdminDepartment> getAllDepartments() {
        return adminDepartmentRepository.findAllOrderByDepartmentNameAsc();
    }


    // ===================================
    // ЛОГИКА РАЗРЕШЕНИЙ
    // ===================================

    /**
     * Возвращает полный набор строковых разрешений для заданного отдела.
     */
    public Set<String> getDepartmentPermissionsByName(String departmentName) {
        Set<String> permissions = DEPARTMENT_PERMISSIONS.get(departmentName.toUpperCase());
        return permissions != null ? new HashSet<>(permissions) : Collections.emptySet();
    }

    /**
     * Проверяет, обладает ли отдел конкретным правом.
     */
    public boolean departmentHasPermission(String departmentName, String permission) {
        Set<String> permissions = DEPARTMENT_PERMISSIONS.get(departmentName.toUpperCase());
        return permissions != null && permissions.contains(permission);
    }
}