package com.estore.library.service;

import com.estore.library.model.bisentity.ShoppingCart;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartService {
    
    ShoppingCart createCart(UUID userId);
    
    Optional<ShoppingCart> getCartById(UUID cartId);
    
    Optional<ShoppingCart> getCartByUserId(UUID userId);
    
    Optional<ShoppingCart> getCartWithItems(UUID userId);
    
    void deleteCart(UUID cartId);
    
    void clearCart(UUID cartId);
    
    BigDecimal getCartTotal(UUID cartId);
    
    Integer getCartItemsCount(UUID cartId);
    
    void addProductToCart(UUID userId, UUID productId, Integer quantity, BigDecimal unitPrice);
    
    void updateProductQuantity(UUID userId, UUID productId, Integer quantity);
    
    void removeProductFromCart(UUID userId, UUID productId);
    
    ShoppingCart getOrCreateCart(UUID userId);
}
