package com.estore.customer.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {
    
    /**
     * Главная страница
     */
    @GetMapping("/")
    public String home() {
        return "forward:/index.html";
    }
    
    /**
     * Страница входа
     */
    @GetMapping("/login")
    public String login() {
        return "forward:/login.html";
    }
    
    /**
     * Страница регистрации
     */
    @GetMapping("/register")
    public String register() {
        return "forward:/register.html";
    }
    
    /**
     * Страница профиля
     */
    @GetMapping("/profile")
    public String profile() {
        return "forward:/profile.html";
    }
    
    /**
     * Страница каталога товаров
     */
    @GetMapping("/products")
    public String products() {
        return "forward:/products.html";
    }
    
    /**
     * Страница корзины
     */
    @GetMapping("/cart")
    public String cart() {
        return "forward:/cart.html";
    }
    
    /**
     * Страница оформления заказа
     */
    @GetMapping("/checkout")
    public String checkout() {
        return "forward:/checkout.html";
    }
    
    /**
     * Страница заказов
     */
    @GetMapping("/orders")
    public String orders() {
        return "forward:/orders.html";
    }
    
    /**
     * Детали заказа
     */
    @GetMapping("/order/**")
    public String orderDetails() {
        return "forward:/order-details.html";
    }
}
