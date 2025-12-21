package com.estore.library.dto.order.dto;

import lombok.Data;

@Data
public class PaymentInfoDto {
    private Integer paymentMethodId;
    private String paymentMethodName;
    private String description;
}







