package com.estore.library.dto.cart.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;
import java.util.List;

@Data
public class CartRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private List<CartItemRequestDto> items;

    @Data
    public static class CartItemRequestDto {
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}







