package com.estore.library.dto.order.response;

import com.estore.library.dto.order.dto.DeliveryInfoDto;
import com.estore.library.dto.order.dto.OrderItemDto;
import com.estore.library.dto.order.dto.PaymentInfoDto;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class OrderResponseDto {
    private UUID id;
    private UUID userId;
    private String userEmail;
    private Date orderDate;
    private String statusName;
    private Integer statusId;
    private BigDecimal totalAmount;
    private Double discountApplied;
    private Date actualDeliveryDate;
    private DeliveryInfoDto deliveryInfo;
    private PaymentInfoDto paymentInfo;
    private List<OrderItemDto> items;
}



