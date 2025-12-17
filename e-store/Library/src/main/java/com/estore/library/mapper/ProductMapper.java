package com.estore.library.mapper;

import com.estore.library.dto.product.dto.BrandDto;
import com.estore.library.dto.product.dto.CategoryDto;
import com.estore.library.dto.product.request.ProductRequestDto;
import com.estore.library.dto.product.response.ProductImageResponseDto;
import com.estore.library.dto.product.response.ProductResponseDto;
import com.estore.library.model.bisentity.Product;
import com.estore.library.model.dicts.Brand;
import com.estore.library.model.dicts.Category;
import com.estore.library.model.dicts.ProductImage;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CategoryMapper.class, BrandMapper.class, ProductImageMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "category", source = "category")
    @Mapping(target = "brand", source = "brand")
    @Mapping(target = "images", source = "images")
    ProductResponseDto toResponseDto(Product product);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingsCount", ignore = true)
    @Mapping(target = "isAvailable", defaultValue = "true")
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "images", ignore = true)
    Product toEntity(ProductRequestDto requestDto);

    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "brand", ignore = true)
    @Mapping(target = "averageRating", ignore = true)
    @Mapping(target = "ratingsCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "images", ignore = true)
    void updateEntityFromDto(ProductRequestDto requestDto, @MappingTarget Product product);
}



