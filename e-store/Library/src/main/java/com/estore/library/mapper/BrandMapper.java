package com.estore.library.mapper;

import com.estore.library.dto.product.dto.BrandDto;
import com.estore.library.model.dicts.Brand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BrandMapper {
    BrandDto toDto(Brand brand);
    Brand toEntity(BrandDto dto);
}



