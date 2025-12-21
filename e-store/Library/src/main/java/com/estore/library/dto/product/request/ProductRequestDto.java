package com.estore.library.dto.product.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequestDto {
    @NotBlank(message = "Product name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    private String mainImageUrl;

    private Integer categoryId;

    private Integer brandId;

    private Boolean isAvailable;

    private List<ProductImageRequestDto> images;

    @Data
    public static class ProductImageRequestDto {
        private String imageUrl;
        private Integer sortOrder;
    }
}







