package com.estore.library.dto.product.response;

import com.estore.library.dto.product.dto.BrandDto;
import com.estore.library.dto.product.dto.CategoryDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    private BigDecimal averageRating;
    private Integer ratingsCount;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private CategoryDto category;
    private BrandDto brand;
    private List<ProductImageResponseDto> images;



}
