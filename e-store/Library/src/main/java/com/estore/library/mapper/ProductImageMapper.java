package com.estore.library.mapper;

import com.estore.library.dto.product.response.ProductImageResponseDto;
import com.estore.library.model.dicts.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductImageMapper {
    ProductImageResponseDto toDto(ProductImage image);
    List<ProductImageResponseDto> toDtoList(List<ProductImage> images);
    ProductImage toEntity(ProductImageResponseDto dto);
}



