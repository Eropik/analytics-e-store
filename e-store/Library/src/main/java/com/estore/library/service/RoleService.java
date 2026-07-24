package com.estore.library.service;

import com.estore.library.model.dicts.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {
    
    Role createRole(Role role);
    
    Optional<Role> getRoleByName(String roleName);
    
    List<Role> getAllRoles();
    
    boolean existsByName(String roleName);
}
