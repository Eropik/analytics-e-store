package com.estore.library.service.impl;

import com.estore.library.model.bisentity.CartItem;
import com.estore.library.model.bisentity.Product;
import com.estore.library.model.bisentity.ShoppingCart;
import com.estore.library.model.bisentity.User;
import com.estore.library.repository.bisentity.CartItemRepository;
import com.estore.library.repository.bisentity.ProductRepository;
import com.estore.library.repository.bisentity.ShoppingCartRepository;
import com.estore.library.repository.bisentity.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShoppingCartServiceImpl implements com.estore.library.service.ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
        private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional
    public ShoppingCart createCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        
        if (shoppingCartRepository.existsByUserId(userId)) {
            throw new IllegalStateException("Cart already exists for user: " + userId);
        }
        
        ShoppingCart cart = new ShoppingCart();
        cart.setUser(user);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        
        return shoppingCartRepository.save(cart);
    }
    
    @Override
    public Optional<ShoppingCart> getCartById(UUID cartId) {
        return shoppingCartRepository.findById(cartId);
    }
    
    @Override
    public Optional<ShoppingCart> getCartByUserId(UUID userId) {
        return shoppingCartRepository.findByUserId(userId);
    }
    
    @Override
    public Optional<ShoppingCart> getCartWithItems(UUID userId) {
        return shoppingCartRepository.findByUserIdWithItems(userId);
    }
    
    @Override
    @Transactional
    public void deleteCart(UUID cartId) {
        if (!shoppingCartRepository.existsById(cartId)) {
            throw new IllegalArgumentException("Cart not found with id: " + cartId);
        }
        shoppingCartRepository.deleteById(cartId);
    }
    
    @Override
    @Transactional
    public void clearCart(UUID cartId) {
        if (!shoppingCartRepository.existsById(cartId)) {
            throw new IllegalArgumentException("Cart not found with id: " + cartId);
        }
        cartItemRepository.deleteByCartId(cartId);
        
        ShoppingCart cart = shoppingCartRepository.findById(cartId).orElseThrow();
        cart.setUpdatedAt(LocalDateTime.now());
        shoppingCartRepository.save(cart);
    }
    
    @Override
    public BigDecimal getCartTotal(UUID cartId) {
        BigDecimal total = cartItemRepository.calculateCartTotal(cartId);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    @Override
    public Integer getCartItemsCount(UUID cartId) {
        Integer count = cartItemRepository.sumQuantityByCartId(cartId);
        return count != null ? count : 0;
    }
    
    @Override
    @Transactional
    public void addProductToCart(UUID userId, UUID productId, Integer quantity, BigDecimal unitPrice) {
        ShoppingCart cart = getOrCreateCart(userId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        if (!product.getIsAvailable()) {
            throw new IllegalStateException("Product is not available: " + productId);
        }
        
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + productId);
        }
        
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), productId);
        
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            item.setUnitPrice(unitPrice);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            newItem.setUnitPrice(unitPrice);
            cartItemRepository.save(newItem);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        shoppingCartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void updateProductQuantity(UUID userId, UUID productId, Integer quantity) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        
        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getCartId(), productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found in cart: " + productId));
        
        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            Product product = item.getProduct();
            if (product.getStockQuantity() < quantity) {
                throw new IllegalStateException("Insufficient stock for product: " + productId);
            }
            
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }
        
        cart.setUpdatedAt(LocalDateTime.now());
        shoppingCartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public void removeProductFromCart(UUID userId, UUID productId) {
        ShoppingCart cart = shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        
        cartItemRepository.deleteByCartIdAndProductId(cart.getCartId(), productId);
        
        cart.setUpdatedAt(LocalDateTime.now());
        shoppingCartRepository.save(cart);
    }
    
    @Override
    @Transactional
    public ShoppingCart getOrCreateCart(UUID userId) {
        return shoppingCartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
    }
}
