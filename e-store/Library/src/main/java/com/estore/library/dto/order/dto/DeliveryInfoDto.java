package com.estore.library.dto.order.dto;

import lombok.Data;

@Data
public class DeliveryInfoDto {
    private Integer deliveryMethodId;
    private String deliveryMethodName;
    private String description;
    private Integer cityId;
    private String cityName;
    private String shippingAddressText;
}


