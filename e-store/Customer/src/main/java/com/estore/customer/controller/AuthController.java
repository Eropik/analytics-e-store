package com.estore.customer.controller;

import com.estore.library.model.bisentity.CustomerProfile;
import com.estore.library.model.bisentity.User;
import com.estore.library.model.dicts.Role;
import com.estore.library.service.CustomerProfileService;
import com.estore.library.service.RoleService;
import com.estore.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:3000")

@CrossOrigin(origins = {"http://localhost:8020", "null"})
public class AuthController {
    
    private final UserService userService;
    private final CustomerProfileService customerProfileService;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * Регистрация нового клиента
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Валидация
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (request.getPassword() == null || request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password must be at least 6 characters"));
            }
            
            // Проверка существования email
            if (userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

            Role customerRole = roleService.getRoleByName("ROLE_CUSTOMER")
                    .orElseThrow(() -> new IllegalStateException("Customer role not found"));
            user.setRole(customerRole);

            User createdUser = userService.createUser(user); // <-- User теперь имеет сгенерированный UUID

            // 2. Создание Профиля Клиента и связывание его с созданным Пользователем
            CustomerProfile profile = new CustomerProfile();

            // Устанавливаем двустороннюю связь
            profile.setUser(createdUser);
            // createdUser.setCustomerProfile(profile); // Эта строка не нужна для сохранения профиля, но хороша для целостности.
            // Мы сделаем сохранение профиля явно.

            profile.setFirstName(request.getFirstName());
            profile.setLastName(request.getLastName());
            profile.setPhoneNumber(request.getPhoneNumber());


            customerProfileService.createProfile(profile);
            // *************************************************

            Map<String, Object> response = new HashMap<>();

            response.put("success", true);
            response.put("message", "Registration successful");
            response.put("userId", createdUser.getUserId());
            response.put("email", createdUser.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Авторизация клиента
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Optional<User> userOpt = userService.getUserByEmail(request.getEmail());
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }
            
            User user = userOpt.get();
            
            // Проверка пароля
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid email or password"));
            }
            
            // Проверка активности
            if (!user.getIsActive()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Account is inactive"));
            }
            
            // Обновление времени последнего входа
            userService.updateLastLogin(user.getUserId());
            
            // Получение профиля
            Optional<CustomerProfile> profileOpt = customerProfileService.getProfileById(user.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("userId", user.getUserId());
            response.put("email", user.getEmail());
            response.put("role", user.getRole().getRoleName());
            
            if (profileOpt.isPresent()) {
                CustomerProfile profile = profileOpt.get();
                response.put("firstName", profile.getFirstName());
                response.put("lastName", profile.getLastName());
                response.put("phoneNumber", profile.getPhoneNumber());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Выход из системы
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok(Map.of("success", true, "message", "Logout successful"));
    }
    
    /**
     * Проверка доступности email
     * GET /api/auth/check-email?email=test@example.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(Map.of("exists", exists, "available", !exists));
    }
    
    // DTO классы
    public static class RegisterRequest {
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
    
    public static class LoginRequest {
        private String email;
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
