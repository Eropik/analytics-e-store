package com.estore.library.service;

import com.estore.library.model.bisentity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    
    User createUser(User user);
    
    User updateUser(UUID userId, User user);
    
    void deleteUser(UUID userId);
    
    Optional<User> getUserById(UUID userId);
    
    Optional<User> getUserByEmail(String email);
    
    Page<User> getAllUsers(Pageable pageable);
    
    Page<User> getActiveUsers(Boolean isActive, Pageable pageable);
    
    Page<User> getUsersByRole(Integer roleId, Pageable pageable);
    
    Page<User> getUsersByRoleName(String roleName, Pageable pageable);
    
    Page<User> getUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    Page<User> searchUsersByEmail(String search, Pageable pageable);
    
    boolean existsByEmail(String email);
    
    void activateUser(UUID userId);
    
    void deactivateUser(UUID userId);
    
    void updateLastLogin(UUID userId);
}
