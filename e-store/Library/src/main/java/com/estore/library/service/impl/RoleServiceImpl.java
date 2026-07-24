package com.estore.library.service.impl;

import com.estore.library.model.dicts.Role;
import com.estore.library.repository.dicts.RoleRepository;
import com.estore.library.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoleServiceImpl implements RoleService {
    
    private final RoleRepository roleRepository;
    
    @Override
    @Transactional
    public Role createRole(Role role) {
        if (roleRepository.existsByRoleName(role.getRoleName())) {
            throw new IllegalArgumentException("Role already exists: " + role.getRoleName());
        }
        return roleRepository.save(role);
    }
    
    @Override
    public Optional<Role> getRoleByName(String roleName) {
        return roleRepository.findByRoleName(roleName);
    }
    
    @Override
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    @Override
    public boolean existsByName(String roleName) {
        return roleRepository.existsByRoleName(roleName);
    }
}
