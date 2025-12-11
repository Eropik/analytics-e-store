package com.estore.library.dto.order.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class OrderRequestDto {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private Integer shippingCityId;
    private String shippingAddressText;

    @NotNull(message = "Delivery method ID is required")
    private Integer deliveryMethodId;

    @NotNull(message = "Payment method ID is required")
    private Integer paymentMethodId;

    private List<OrderItemRequestDto> items;

    private Double discountApplied;

    @Data
    public static class OrderItemRequestDto {
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;
    }
}


