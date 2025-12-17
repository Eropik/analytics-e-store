package com.estore.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class AdminSecurityConfiguration {

    private final UserDetailsService userDetailsService;

    public AdminSecurityConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // ============================================================
        // ВРЕМЕННАЯ КОНФИГУРАЦИЯ: ВСЕ ЭНДПОИНТЫ ДОСТУПНЫ БЕЗ АВТОРИЗАЦИИ
        // ДЛЯ ТЕСТИРОВАНИЯ - РАСКОММЕНТИРУЙТЕ БЛОК НИЖЕ И УДАЛИТЕ ЭТУ СЕКЦИЮ
        // ============================================================
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()  // ВРЕМЕННО: ВСЕ ДОСТУПНО
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();

        // ============================================================
        // ПРАВИЛЬНАЯ КОНФИГУРАЦИЯ С РОЛЯМИ И РАЗРЕШЕНИЯМИ
        // РАСКОММЕНТИРУЙТЕ ЭТОТ БЛОК КОГДА БУДЕТЕ ГОТОВЫ К ПРОДАКШЕНУ
        // ============================================================
        
        /*
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Публичные эндпоинты (авторизация)
                .requestMatchers("/api/admin/auth/login").permitAll()
                .requestMatchers("/api/admin/auth/register").permitAll()
                .requestMatchers("/api/admin/auth/logout").permitAll()
                
                // Swagger/OpenAPI документация
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // Actuator endpoints (если используются)
                .requestMatchers("/actuator/**").hasRole("SUPER_ADMIN")
                
                // ========== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ==========
                // Только администраторы могут управлять пользователями
                .requestMatchers("/api/admin/users/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/admin/users/delete/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/users/role/**").hasRole("SUPER_ADMIN")
                
                // ========== УПРАВЛЕНИЕ ТОВАРАМИ ==========
                .requestMatchers("/api/admin/products/create").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/products/update/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/products/delete/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/products/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SALES_MANAGER")
                
                // ========== УПРАВЛЕНИЕ СКЛАДАМИ ==========
                .requestMatchers("/api/admin/warehouses/create").hasAnyRole("ADMIN", "SUPER_ADMIN")
                .requestMatchers("/api/admin/warehouses/update/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/warehouses/delete/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/admin/warehouses/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "SALES_MANAGER")
                
                // ========== УПРАВЛЕНИЕ ЗАПАСАМИ ==========
                .requestMatchers("/api/admin/stock/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                
                // ========== УПРАВЛЕНИЕ ЗАКАЗАМИ ==========
                .requestMatchers("/api/admin/orders/create").hasAnyRole("ADMIN", "SALES_MANAGER")
                .requestMatchers("/api/admin/orders/update/**").hasAnyRole("ADMIN", "SALES_MANAGER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/orders/delete/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/orders/status/**").hasAnyRole("ADMIN", "SALES_MANAGER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/orders/**").hasAnyRole("ADMIN", "SALES_MANAGER", "WAREHOUSE_MANAGER")
                
                // ========== УПРАВЛЕНИЕ ДОСТАВКОЙ ==========
                .requestMatchers("/api/admin/delivery/routes/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                .requestMatchers("/api/admin/delivery/methods/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                .requestMatchers("/api/admin/delivery/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER", "WAREHOUSE_MANAGER")
                
                // ========== УПРАВЛЕНИЕ ГОРОДАМИ И МАРШРУТАМИ ==========
                .requestMatchers("/api/admin/cities/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                .requestMatchers("/api/admin/routes/**").hasAnyRole("ADMIN", "LOGISTICS_MANAGER")
                
                // ========== СПРАВОЧНИКИ ==========
                .requestMatchers("/api/admin/dictionaries/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                
                // ========== ОТЧЕТЫ И АНАЛИТИКА ==========
                .requestMatchers("/api/admin/reports/**").hasAnyRole("ADMIN", "SALES_MANAGER", "WAREHOUSE_MANAGER")
                .requestMatchers("/api/admin/analytics/**").hasAnyRole("ADMIN", "SALES_MANAGER")
                .requestMatchers("/api/admin/statistics/**").hasAnyRole("ADMIN", "SALES_MANAGER", "WAREHOUSE_MANAGER")
                
                // ========== ПРОФИЛЬ АДМИНИСТРАТОРА ==========
                .requestMatchers("/api/admin/profile/**").hasAnyRole("ADMIN", "SUPER_ADMIN", "WAREHOUSE_MANAGER", "SALES_MANAGER", "LOGISTICS_MANAGER")
                
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
        */
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3001", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
