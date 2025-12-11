package com.estore.library.dto.cart.response;

import com.estore.library.dto.cart.dto.CartItemDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class CartResponseDto {
    private UUID cartId;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemDto> items;
    private BigDecimal totalAmount;
    private Integer totalItems;
}


