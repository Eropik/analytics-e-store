package com.estore.library.dto.cart.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CartItemDto {
    private Integer cartItemId;
    private UUID productId;
    private String productName;
    private String productImageUrl;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}



