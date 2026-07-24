package com.estore.library.service;

import com.estore.library.model.bisentity.ShoppingCart;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingCartService {
    
    Optional<ShoppingCart> getCartByUserId(UUID userId);
    
    Optional<ShoppingCart> getCartWithItems(UUID userId);
    
    void clearCart(UUID cartId);
    
    BigDecimal getCartTotal(UUID cartId);
    
    Integer getCartItemsCount(UUID cartId);
    
    void addProductToCart(UUID userId, UUID productId, Integer quantity, BigDecimal unitPrice);
    
    void updateProductQuantity(UUID userId, UUID productId, Integer quantity);
    
    void removeProductFromCart(UUID userId, UUID productId);
    
}
