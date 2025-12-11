package com.estore.library.dto.analyze.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CategoryBrandAnalysisDto {
    private Integer categoryId;
    private String categoryName;
    private Integer brandId;
    private String brandName;
    private Long ordersCount;
    private Long unitsSold;
    private BigDecimal totalRevenue;
}


