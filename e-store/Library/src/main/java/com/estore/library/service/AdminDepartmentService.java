package com.estore.library.service;

import com.estore.library.model.dicts.AdminDepartment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AdminDepartmentService {
    
    AdminDepartment createDepartment(AdminDepartment department);
    
    AdminDepartment updateDepartment(Integer departmentId, AdminDepartment department);
    
    void deleteDepartment(Integer departmentId);
    
    Optional<AdminDepartment> getDepartmentById(Integer departmentId);
    
    Optional<AdminDepartment> getDepartmentByName(String departmentName);
    
    List<AdminDepartment> getAllDepartments();

    // Получение разрешений для отдела

    Set<String> getDepartmentPermissionsByName(String departmentName);
    

    
    boolean departmentHasPermission(String departmentName, String permission);
}
