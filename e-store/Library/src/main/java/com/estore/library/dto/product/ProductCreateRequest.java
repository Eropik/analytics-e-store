package com.estore.library.dto.product.request;
import java.math.BigDecimal;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO для приема данных при создании нового продукта.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String mainImageUrl; // Главное изображение


    private Integer categoryId;
    private Integer brandId;

    // Список для галереи (дополнительных изображений)
    private List<ProductImageDto> images;

    // Вложенный DTO для данных изображения (URL и порядок сортировки)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private String imageUrl;
        private Integer sortOrder; // Порядок сортировки для галереи
    }
}