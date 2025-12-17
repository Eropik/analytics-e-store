package com.estore.library.dto.analyze.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RouteAnalysisDto {
    private Integer cityId;
    private String cityName;
    private Long ordersCount;
    private BigDecimal totalRevenue;
}



