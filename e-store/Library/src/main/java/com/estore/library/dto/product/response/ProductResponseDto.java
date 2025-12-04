package com.estore.library.dto.product.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ProductResponseDto {
    private UUID productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String mainImageUrl;

    // Включаем только ID и Имя для связанных сущностей
    private Integer categoryId;
    private String categoryName;

    private Integer brandId;
    private String brandName;

    // Включаем DTO для списка изображений
    private List<ProductImageResponseDto> images;

}

