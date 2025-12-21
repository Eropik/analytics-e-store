package com.estore.library.mapper;

import com.estore.library.dto.profile.request.ProfileRequestDto;
import com.estore.library.dto.profile.response.ProfileResponseDto;
import com.estore.library.model.bisentity.CustomerProfile;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {UserMapper.class, OrderMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfileMapper {

    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "cityId", source = "city.cityId")
    @Mapping(target = "cityName", source = "city.cityName")
    @Mapping(target = "orderHistory", ignore = true)
    ProfileResponseDto toResponseDto(CustomerProfile profile);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "totalSpent", defaultValue = "0.00")
    @Mapping(target = "ordersCount", defaultValue = "0")
    @Mapping(target = "city", ignore = true)
    CustomerProfile toEntity(ProfileRequestDto requestDto);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "totalSpent", ignore = true)
    @Mapping(target = "ordersCount", ignore = true)
    @Mapping(target = "city", ignore = true)
    void updateEntityFromDto(ProfileRequestDto requestDto, @MappingTarget CustomerProfile profile);
}







