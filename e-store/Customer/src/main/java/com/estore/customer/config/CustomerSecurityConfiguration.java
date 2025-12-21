package com.estore.customer.config;

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
public class CustomerSecurityConfiguration {

    private final UserDetailsService userDetailsService;

    public CustomerSecurityConfiguration(UserDetailsService userDetailsService) {
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
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/register").permitAll()
                .requestMatchers("/api/auth/logout").permitAll()
                .requestMatchers("/api/auth/check-email").permitAll()
                
                // Swagger/OpenAPI документация
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                
                // ========== УПРАВЛЕНИЕ ЗАКАЗАМИ ==========
                // Все эндпоинты заказов доступны только для Customer
                .requestMatchers("/api/customer/orders/**").hasRole("CUSTOMER")
                
                // Создание заказа
                .requestMatchers("/api/customer/orders").hasRole("CUSTOMER")
                
                // Получение заказов пользователя
                .requestMatchers("/api/customer/orders/user/**").hasRole("CUSTOMER")
                
                // Детали конкретного заказа (доступ только к своим заказам проверяется в контроллере)
                .requestMatchers("/api/customer/orders/*").hasRole("CUSTOMER")
                
                // Отмена заказа (доступ только к своим заказам проверяется в контроллере)
               // .requestMatchers("/api/customer/orders/cancel").hasRole("CUSTOMER")
                
                // Статистика заказов пользователя
               // .requestMatchers("/api/customer/orders/user/statistics").hasRole("CUSTOMER")
                
                // Фильтрация заказов по статусу
               // .requestMatchers("/api/customer/orders/user/status/*").hasRole("CUSTOMER")
                
                // Отслеживание заказа
                .requestMatchers("/api/customer/orders/track").hasRole("CUSTOMER")
                
                // ========== СПРАВОЧНИКИ ДЛЯ ОФОРМЛЕНИЯ ЗАКАЗА ==========
                // Методы доставки и оплаты доступны для просмотра
                .requestMatchers("/api/customer/orders/delivery-methods").hasRole("CUSTOMER")
                .requestMatchers("/api/customer/orders/payment-methods").hasRole("CUSTOMER")
                
                // Расчет стоимости доставки
                .requestMatchers("/api/customer/orders/calculate-delivery").hasRole("CUSTOMER")
                
                // Поиск ближайшего склада
                .requestMatchers("/api/customer/orders/nearest-warehouse").hasRole("CUSTOMER")
                
                // ========== ПРОФИЛЬ КЛИЕНТА ==========
                .requestMatchers("/api/customer/profile/**").hasRole("CUSTOMER")
                
                // Все остальные запросы требуют аутентификации
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        return http.build();
        */ // там везде почти была доп */ в варпе глянуть
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
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8020", "null"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
