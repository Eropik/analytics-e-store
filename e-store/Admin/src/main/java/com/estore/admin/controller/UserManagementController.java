package com.estore.admin.controller;

import com.estore.library.dto.user.response.AdminProfileDto;
import com.estore.library.dto.user.response.CustomerProfileDto;
import com.estore.library.dto.user.response.UserFullInfoDto;
import com.estore.library.dto.user.response.UserResponseDto;
import com.estore.library.model.bisentity.AdminProfile;
import com.estore.library.model.bisentity.CustomerProfile;
import com.estore.library.model.bisentity.User;
import com.estore.library.service.AdminProfileService;
import com.estore.library.service.UserService;
import com.estore.library.service.CustomerProfileService;
import com.estore.library.service.RoleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Контроллер для отдела USER_MANAGE
 * Доступ: управление пользователями, активация, роли
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
//@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:8019", "null"})
public class UserManagementController {
    
    private final AdminProfileService adminProfileService;
    private final UserService userService;
    private final CustomerProfileService customerProfileService;
    private final RoleService roleService;
    
    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasUserManagementAccess(adminUserId);
    }
    
    /**
     * Получить всех пользователей
     * GET /api/admin/users
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. USER_MANAGE department required"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getAllUsers(pageable);

            // --- ИСПРАВЛЕНИЕ ---
            List<UserResponseDto> userDtos = users.getContent().stream()
                    .map(this::convertToDto) // Используем DTO
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "users", userDtos, // Возвращаем DTO
                    "currentPage", users.getNumber(),
                    "totalPages", users.getTotalPages(),
                    "totalItems", users.getTotalElements()
            ));


            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить пользователя по ID
     * GET /api/admin/users/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(
            @RequestParam UUID adminUserId,
            @PathVariable UUID userId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            // --- ИСПРАВЛЕНИЕ ---
            UserResponseDto userDto = convertToDto(userOpt.get());
            return ResponseEntity.ok(userDto); // Возвращаем DTO
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Получить пользователя с полной информацией по ID
     * GET /api/admin/userfullinfo/{userId}
     */
    @GetMapping("/userfullinfo/{userId}") // Относительный URL: /api/admin/userfullinfo/{userId}
    public ResponseEntity<?> getUserFullInfoById(
            @RequestParam UUID adminUserId,
            @PathVariable UUID userId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. USER_MANAGE department required"));
            }

            User user = userService.getUserById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

            UserFullInfoDto userDto = convertToFullInfoDto(user);

            return ResponseEntity.ok(userDto);

        } catch (EntityNotFoundException e) {
            // Если пользователь не найден
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Логирование ошибки (рекомендуется)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Получить всех пользователей с полной информацией (пагинация)
     * GET /api/admin/users/userfullinfo
     */
    @GetMapping("/userfullinfo")
    public ResponseEntity<?> getAllUsersFullInfo(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. USER_MANAGE department required"));
            }

            Pageable pageable = PageRequest.of(page, size);
            // Предполагаем, что userService.getAllUsers возвращает Page<User>
            Page<User> usersPage = userService.getAllUsers(pageable);

            // Преобразование списка сущностей в список DTO UserFullInfo
            List<UserFullInfoDto> userDtos = usersPage.getContent().stream()
                    .map(this::convertToFullInfoDto) // Используем DTO UserFullInfo
                    .toList();

            // Формирование ответа с метаданными пагинации
            return ResponseEntity.ok(Map.of(
                    "users", userDtos, // Возвращаем список DTO
                    "currentPage", usersPage.getNumber(),
                    "totalPages", usersPage.getTotalPages(),
                    "totalItems", usersPage.getTotalElements()
            ));

        } catch (Exception e) {
            // Логирование ошибки (рекомендуется)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    /**
     * Активировать пользователя
     * PUT /api/admin/users/{userId}/activate
     */
    @PutMapping("/{userId}/activate")
    public ResponseEntity<?> activateUser(
            @RequestParam UUID adminUserId,
            @PathVariable UUID userId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            userService.activateUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User activated"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Деактивировать пользователя
     * PUT /api/admin/users/{userId}/deactivate
     */
    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<?> deactivateUser(
            @RequestParam UUID adminUserId,
            @PathVariable UUID userId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            userService.deactivateUser(userId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User deactivated"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Фильтр активных/неактивных пользователей
     * GET /api/admin/users/active
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveUsers(
            @RequestParam UUID adminUserId,
            @RequestParam Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getActiveUsers(isActive, pageable);

            List<UserResponseDto> userDtos = users.getContent().stream()
                    .map(this::convertToDto) // Используем DTO
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "users", userDtos, // Возвращаем DTO
                    "currentPage", users.getNumber(),
                    "totalPages", users.getTotalPages(),
                    "totalItems", users.getTotalElements()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Пользователи по роли
     * GET /api/admin/users/role/{roleName}
     */
    @GetMapping("/role/{roleName}")
    public ResponseEntity<?> getUsersByRole(
            @RequestParam UUID adminUserId,
            @PathVariable String roleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.getUsersByRoleName(roleName, pageable);

            List<UserResponseDto> userDtos = users.getContent().stream()
                    .map(this::convertToDto) // Используем DTO
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "users", userDtos, // Возвращаем DTO
                    "currentPage", users.getNumber(),
                    "totalPages", users.getTotalPages(),
                    "totalItems", users.getTotalElements()
            ));


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Поиск пользователей по email
     * GET /api/admin/users/search
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(
            @RequestParam UUID adminUserId,
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.searchUsersByEmail(email, pageable);

            List<UserResponseDto> userDtos = users.getContent().stream()
                    .map(this::convertToDto) // Используем DTO
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "users", userDtos, // Возвращаем DTO
                    "currentPage", users.getNumber(),
                    "totalPages", users.getTotalPages(),
                    "totalItems", users.getTotalElements()
            ));


        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Профили клиентов
     * GET /api/admin/users/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomerProfiles(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Pageable pageable = PageRequest.of(page, size);
            var profiles = customerProfileService.getAllProfiles(pageable);
            
            return ResponseEntity.ok(Map.of(
                "profiles", profiles.getContent(),
                "currentPage", profiles.getNumber(),
                "totalPages", profiles.getTotalPages(),
                "totalItems", profiles.getTotalElements()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Все роли
     * GET /api/admin/users/roles
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getAllRoles(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            return ResponseEntity.ok(roleService.getAllRoles());
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // UserManagementController.java

    private UserResponseDto convertToDto(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());

        // Роль (EAGER)
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getRoleId());
            dto.setRoleName(user.getRole().getRoleName());
        }


        if (user.getCustomerProfile() != null && user.getCustomerProfile().getUserId() != null) {
            dto.setProfileId(user.getCustomerProfile().getUserId());
            dto.setProfileType("CUSTOMER");
        } else if (user.getAdminProfile() != null && user.getAdminProfile().getUserId() != null) {
            dto.setProfileId(user.getAdminProfile().getUserId());
            dto.setProfileType("ADMIN");
        }


        return dto;
    }

    /**
     * Вспомогательный метод для преобразования сущности User в Full DTO.
     */
    private UserFullInfoDto convertToFullInfoDto(User user) {
        UserFullInfoDto dto = new UserFullInfoDto();

        // Основные поля User
        dto.setUserId(user.getUserId());
        dto.setEmail(user.getEmail());
        dto.setIsActive(user.getIsActive());
        dto.setRegistrationDate(user.getRegistrationDate());
        dto.setLastLogin(user.getLastLogin());

        // Роль (EAGER - безопасно)
        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getRoleId());
            dto.setRoleName(user.getRole().getRoleName());
        }

        // CustomerProfile (LAZY - требуется загрузка)
        if (user.getCustomerProfile() != null) {
            // Мы предполагаем, что профиль ИНИЦИАЛИЗИРОВАН (либо через JOIN FETCH, либо через @Transactional)
            CustomerProfile profile = user.getCustomerProfile();
            CustomerProfileDto profileDto = new CustomerProfileDto();

            profileDto.setUserId(profile.getUserId());
            profileDto.setFirstName(profile.getFirstName());
            profileDto.setLastName(profile.getLastName());
            profileDto.setPhoneNumber(profile.getPhoneNumber());
            profileDto.setTotalSpent(profile.getTotalSpent());
            profileDto.setOrdersCount(profile.getOrdersCount());
            profileDto.setProfilePictureUrl(profile.getProfilePictureUrl());

            // City (LAZY, если не JOIN FETCH, может быть null или вызвать ошибку)
            if (profile.getCity() != null) {
                profileDto.setCityId(profile.getCity().getCityId());
                // profileDto.setCityName(profile.getCity().getName()); // Только если City загружен
            }

            dto.setCustomerProfile(profileDto);
        }

        // AdminProfile (LAZY - требуется загрузка)
        if (user.getAdminProfile() != null) {

            AdminProfile profile = user.getAdminProfile();
            AdminProfileDto profileDto = new AdminProfileDto();

            profileDto.setUserId(profile.getUserId());
            profileDto.setFirstName(profile.getFirstName());
            profileDto.setLastName(profile.getLastName());
            profileDto.setHireDate(profile.getHireDate());
            profileDto.setProfilePictureUrl(profile.getProfilePictureUrl());

            // Department (LAZY)
            if (profile.getDepartment() != null) {
                profileDto.setDepartmentId(profile.getDepartment().getDepartmentId());
                profileDto.setDepartmentName(profile.getDepartment().getDepartmentName()); // Только если Department загружен
            }

            dto.setAdminProfile(profileDto);
        }

        return dto;
    }
}
