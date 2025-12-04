package com.estore.library.service.impl;

import com.estore.library.model.bisentity.User;
import com.estore.library.repository.bisentity.UserRepository;
import com.estore.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public User createUser(User user) {


        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("User with email " + user.getEmail() + " already exists");
        }
        user.setRegistrationDate(LocalDateTime.now());
        user.setIsActive(true);
        return userRepository.save(user);
    }
    
    @Override
    @Transactional
    public User updateUser(UUID userId, User user) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        if (!existingUser.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        existingUser.setEmail(user.getEmail());
        existingUser.setPasswordHash(user.getPasswordHash());
        existingUser.setRole(user.getRole());
        
        return userRepository.save(existingUser);
    }
    
    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }
    
    @Override
    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }
    
    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
    
    @Override
    public Page<User> getActiveUsers(Boolean isActive, Pageable pageable) {
        return userRepository.findByIsActive(isActive, pageable);
    }
    
    @Override
    public Page<User> getUsersByRole(Integer roleId, Pageable pageable) {
        return userRepository.findByRoleId(roleId, pageable);
    }
    
    @Override
    public Page<User> getUsersByRoleName(String roleName, Pageable pageable) {
        return userRepository.findByRoleName(roleName, pageable);
    }
    
    @Override
    public Page<User> getUsersRegisteredBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return userRepository.findByRegistrationDateBetween(startDate, endDate, pageable);
    }
    
    @Override
    public Page<User> searchUsersByEmail(String search, Pageable pageable) {
        return userRepository.searchByEmail(search, pageable);
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional
    public void activateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setIsActive(true);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void deactivateUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void updateLastLogin(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
}
