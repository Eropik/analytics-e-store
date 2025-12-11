package com.estore.library.dto.analyze.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AgeGroupAnalysisDto {
    private String ageGroup;
    private Long customersCount;
    private Long ordersCount;
    private BigDecimal totalRevenue;
}


