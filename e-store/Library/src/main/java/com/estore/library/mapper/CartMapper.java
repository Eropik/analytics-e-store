package com.estore.library.mapper;

import com.estore.library.dto.cart.request.CartRequestDto;
import com.estore.library.dto.cart.response.CartResponseDto;
import com.estore.library.model.bisentity.ShoppingCart;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartMapper {

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(calculateTotalAmount(cart))")
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    CartResponseDto toResponseDto(ShoppingCart cart);

    List<CartResponseDto> toResponseDtoList(List<ShoppingCart> carts);

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "items", ignore = true)
    ShoppingCart toEntity(CartRequestDto requestDto);

    @Mapping(target = "cartId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "items", ignore = true)
    void updateEntityFromDto(CartRequestDto requestDto, @MappingTarget ShoppingCart cart);

    default BigDecimal calculateTotalAmount(ShoppingCart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default Integer calculateTotalItems(ShoppingCart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return 0;
        }
        return cart.getItems().stream()
                .mapToInt(CartItem -> CartItem.getQuantity())
                .sum();
    }
}







