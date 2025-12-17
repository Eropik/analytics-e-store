package com.estore.customer.controller;

import com.estore.library.service.DeliveryMethodService;
import com.estore.library.service.PaymentMethodService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/customer/delivery")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:8020", "null"})
public class DeliveryController {
    
    private final DeliveryMethodService deliveryMethodService;
    private final PaymentMethodService paymentMethodService;
    
    /**
     * Получить все методы доставки
     * GET /api/customer/delivery/methods
     */
    @GetMapping("/methods")
    public ResponseEntity<?> getAllDeliveryMethods() {
        try {
            return ResponseEntity.ok(deliveryMethodService.getAllDeliveryMethods());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить все методы оплаты
     * GET /api/customer/delivery/payment-methods
     */
    @GetMapping("/payment-methods")
    public ResponseEntity<?> getAllPaymentMethods() {
        try {
            return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
