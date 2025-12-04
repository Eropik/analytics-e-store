package com.estore.customer.controller;

import com.estore.library.model.bisentity.ShoppingCart;
import com.estore.library.service.ShoppingCartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/customer/cart")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ShoppingCartController {
    
    private final ShoppingCartService shoppingCartService;
    
    /**
     * Получить корзину пользователя
     * GET /api/customer/cart/{userId}
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getCart(@PathVariable UUID userId) {
        try {
            Optional<ShoppingCart> cartOpt = shoppingCartService.getCartWithItems(userId);
            
            if (cartOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of("cart", null, "items", new Object[]{}));
            }
            
            ShoppingCart cart = cartOpt.get();
            BigDecimal total = shoppingCartService.getCartTotal(cart.getCartId());
            Integer itemsCount = shoppingCartService.getCartItemsCount(cart.getCartId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("cart", cart);
            response.put("total", total);
            response.put("itemsCount", itemsCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Добавить товар в корзину
     * POST /api/customer/cart/add
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody AddToCartRequest request) {
        try {
            shoppingCartService.addProductToCart(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity(),
                request.getUnitPrice()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product added to cart"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить количество товара в корзине
     * PUT /api/customer/cart/update
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(@RequestBody UpdateCartRequest request) {
        try {
            shoppingCartService.updateProductQuantity(
                request.getUserId(),
                request.getProductId(),
                request.getQuantity()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart updated"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Удалить товар из корзины
     * DELETE /api/customer/cart/remove
     */
    @DeleteMapping("/remove")
    public ResponseEntity<?> removeFromCart(@RequestBody RemoveFromCartRequest request) {
        try {
            shoppingCartService.removeProductFromCart(
                request.getUserId(),
                request.getProductId()
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Product removed from cart"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Очистить корзину
     * DELETE /api/customer/cart/{userId}/clear
     */
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<?> clearCart(@PathVariable UUID userId) {
        try {
            Optional<ShoppingCart> cartOpt = shoppingCartService.getCartByUserId(userId);
            
            if (cartOpt.isPresent()) {
                shoppingCartService.clearCart(cartOpt.get().getCartId());
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cart cleared"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // DTO классы
    public static class AddToCartRequest {
        private UUID userId;
        private UUID productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
    
    public static class UpdateCartRequest {
        private UUID userId;
        private UUID productId;
        private Integer quantity;
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
    
    public static class RemoveFromCartRequest {
        private UUID userId;
        private UUID productId;
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }
    }
}
