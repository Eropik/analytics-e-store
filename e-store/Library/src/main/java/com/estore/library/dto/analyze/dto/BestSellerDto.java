package com.estore.library.dto.analyze.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class BestSellerDto {
    private UUID productId;
    private String productName;
    private Long totalQuantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
}







