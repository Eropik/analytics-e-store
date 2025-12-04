package com.estore.library.dto.product.response;

import lombok.Data;

// Вложенный/Отдельный DTO для изображений
@Data
public class ProductImageResponseDto {
    private Integer imageId;
    private String imageUrl;
    private Integer sortOrder;
}