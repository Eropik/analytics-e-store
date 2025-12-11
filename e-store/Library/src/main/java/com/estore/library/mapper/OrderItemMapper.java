package com.estore.library.mapper;

import com.estore.library.dto.order.dto.OrderItemDto;
import com.estore.library.model.bisentity.OrderItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.mainImageUrl")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(orderItem))")
    OrderItemDto toDto(OrderItem orderItem);

    List<OrderItemDto> toDtoList(List<OrderItem> orderItems);

    @Mapping(target = "orderItemId", ignore = true)
    @Mapping(target = "order", ignore = true)
    @Mapping(target = "product", ignore = true)
    OrderItem toEntity(OrderItemDto dto);

    default BigDecimal calculateSubtotal(OrderItem orderItem) {
        if (orderItem.getQuantity() != null && orderItem.getUnitPrice() != null) {
            return orderItem.getUnitPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
        }
        return BigDecimal.ZERO;
    }
}


