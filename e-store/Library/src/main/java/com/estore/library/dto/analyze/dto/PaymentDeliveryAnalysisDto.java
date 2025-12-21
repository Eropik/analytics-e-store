package com.estore.library.dto.analyze.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDeliveryAnalysisDto {
    private Integer methodId;
    private String methodName;
    private String methodType; // "PAYMENT" or "DELIVERY"
    private Long ordersCount;
    private BigDecimal totalRevenue;
}







