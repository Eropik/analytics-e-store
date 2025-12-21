package com.estore.library.mapper;

import com.estore.library.dto.order.dto.DeliveryInfoDto;
import com.estore.library.dto.order.dto.OrderItemDto;
import com.estore.library.dto.order.dto.PaymentInfoDto;
import com.estore.library.dto.order.request.OrderRequestDto;
import com.estore.library.dto.order.response.OrderResponseDto;
import com.estore.library.model.bisentity.Order;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "userEmail", source = "user.email")
    @Mapping(target = "statusName", source = "status.statusName")
    @Mapping(target = "statusId", source = "status.statusId")
    @Mapping(target = "deliveryInfo", expression = "java(mapDeliveryInfo(order))")
    @Mapping(target = "paymentInfo", expression = "java(mapPaymentInfo(order))")
    @Mapping(target = "items", source = "orderItems")
    OrderResponseDto toResponseDto(Order order);

    List<OrderResponseDto> toResponseDtoList(List<Order> orders);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", expression = "java(new java.util.Date())")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "shippingCity", ignore = true)
    @Mapping(target = "deliveryMethod", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "discountApplied", defaultValue = "0.0")
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "sourceWarehouse", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    Order toEntity(OrderRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "shippingCity", ignore = true)
    @Mapping(target = "deliveryMethod", ignore = true)
    @Mapping(target = "paymentMethod", ignore = true)
    @Mapping(target = "actualDeliveryDate", ignore = true)
    @Mapping(target = "sourceWarehouse", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    void updateEntityFromDto(OrderRequestDto requestDto, @MappingTarget Order order);

    default DeliveryInfoDto mapDeliveryInfo(Order order) {
        if (order == null) {
            return null;
        }
        DeliveryInfoDto dto = new DeliveryInfoDto();
        if (order.getDeliveryMethod() != null) {
            dto.setDeliveryMethodId(order.getDeliveryMethod().getMethodId());
            dto.setDeliveryMethodName(order.getDeliveryMethod().getMethodName());
            dto.setDescription(order.getDeliveryMethod().getDescription());
        }
        if (order.getShippingCity() != null) {
            dto.setCityId(order.getShippingCity().getCityId());
            dto.setCityName(order.getShippingCity().getCityName());
        }
        dto.setShippingAddressText(order.getShippingAddressText());
        return dto;
    }

    default PaymentInfoDto mapPaymentInfo(Order order) {
        if (order == null || order.getPaymentMethod() == null) {
            return null;
        }
        PaymentInfoDto dto = new PaymentInfoDto();
        dto.setPaymentMethodId(order.getPaymentMethod().getMethodId());
        dto.setPaymentMethodName(order.getPaymentMethod().getMethodName());
        dto.setDescription(order.getPaymentMethod().getDescription());
        return dto;
    }
}







