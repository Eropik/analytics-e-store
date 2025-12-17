package com.estore.library.mapper;

import com.estore.library.dto.cart.dto.CartItemDto;
import com.estore.library.model.bisentity.CartItem;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.productId")
    @Mapping(target = "productName", source = "product.name")
    @Mapping(target = "productImageUrl", source = "product.mainImageUrl")
    @Mapping(target = "productPrice", source = "product.price")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(cartItem))")
    CartItemDto toDto(CartItem cartItem);

    List<CartItemDto> toDtoList(List<CartItem> cartItems);

    @Mapping(target = "cartItemId", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "product", ignore = true)
    CartItem toEntity(CartItemDto dto);

    default BigDecimal calculateSubtotal(CartItem cartItem) {
        if (cartItem.getQuantity() != null && cartItem.getUnitPrice() != null) {
            return cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
        }
        return BigDecimal.ZERO;
    }
}



