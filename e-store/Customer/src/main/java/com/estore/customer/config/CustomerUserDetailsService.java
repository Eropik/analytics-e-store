package com.estore.customer.config;

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
 * Реализация UserDetailsService для загрузки пользователей-клиентов
 */
@Service
@RequiredArgsConstructor
public class CustomerUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Проверяем, что пользователь является клиентом
        String roleName = user.getRole().getRoleName();
        if (!roleName.equals("Customer")) {
            throw new UsernameNotFoundException("User is not a customer: " + email);
        }

        return new CustomerUserDetails(user);
    }

    /**
     * Внутренний класс для представления деталей клиента
     */
    public static class CustomerUserDetails implements UserDetails {

        private final User user;

        public CustomerUserDetails(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            // Добавляем роль с префиксом ROLE_
            String roleName = user.getRole().getRoleName().toUpperCase();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
            
            // Базовые права для клиента
            authorities.add(new SimpleGrantedAuthority("PERMISSION_CREATE_ORDER"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_VIEW_OWN_ORDERS"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_CANCEL_OWN_ORDER"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_TRACK_ORDER"));
            authorities.add(new SimpleGrantedAuthority("PERMISSION_UPDATE_PROFILE"));
            
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
