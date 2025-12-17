package com.estore.admin.config;

import com.estore.library.model.bisentity.User;
import com.estore.library.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Реализация UserDetailsService для загрузки пользователей-администраторов
 */
@Service
@RequiredArgsConstructor
public class AdminUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Проверяем, что пользователь имеет административные права
        String normalizedRole = normalizeRole(user.getRole().getRoleName());
        if (!isAdminRole(normalizedRole)) {
            throw new UsernameNotFoundException("User does not have admin privileges: " + email);
        }

        return new AdminUserDetails(user);
    }

    /**
     * Проверка, является ли роль административной
     */
    private boolean isAdminRole(String roleName) {
        String normalized = normalizeRole(roleName);
        return normalized.equals("ADMIN") ||
               normalized.equals("SUPER_ADMIN") ||
               normalized.equals("WAREHOUSE_MANAGER") ||
               normalized.equals("SALES_MANAGER") ||
               normalized.equals("LOGISTICS_MANAGER");
    }

    private static String normalizeRole(String roleName) {
        String normalized = roleName.toUpperCase().replace(" ", "_");
        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }
        return normalized;
    }

    /**
     * Внутренний класс для представления деталей администратора
     */
    public static class AdminUserDetails implements UserDetails {

        private final User user;

        public AdminUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Добавляем роль с префиксом ROLE_
            String normalizedRole = normalizeRole(user.getRole().getRoleName());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + normalizedRole));
            
            // Можно добавить дополнительные права на основе роли
            switch (normalizedRole) {
                case "SUPER_ADMIN":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_ALL"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_DELETE_USERS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ROLES"));
                    break;
                case "ADMIN":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_PRODUCTS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ORDERS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_WAREHOUSES"));
                    break;
                case "WAREHOUSE_MANAGER":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_STOCK"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_ORDERS"));
                    break;
                case "SALES_MANAGER":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ORDERS"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_REPORTS"));
                    break;
                case "LOGISTICS_MANAGER":
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_DELIVERY"));
                    authorities.add(new SimpleGrantedAuthority("PERMISSION_MANAGE_ROUTES"));
                    break;
            }
            
            return authorities;
        }

        @Override
        public String getPassword() {
            return user.getPasswordHash();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return user.getIsActive();
        }

        public User getUser() {
            return user;
        }
    }
}
