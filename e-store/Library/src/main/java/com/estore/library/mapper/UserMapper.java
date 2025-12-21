package com.estore.library.mapper;

import com.estore.library.dto.user.request.UserRequestDto;
import com.estore.library.dto.user.response.UserResponseDto;
import com.estore.library.model.bisentity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "roleId", source = "role.roleId")
    @Mapping(target = "roleName", source = "role.roleName")
    @Mapping(target = "profileId", source = "userId")
    @Mapping(target = "profileType", expression = "java(determineProfileType(user))")
    @Mapping(target = "adminProfile", ignore = true)
    @Mapping(target = "customerProfile", ignore = true)
    UserResponseDto toResponseDto(User user);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", source = "password")
    @Mapping(target = "registrationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "isActive", defaultValue = "true")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "customerProfile", ignore = true)
    @Mapping(target = "adminProfile", ignore = true)
    User toEntity(UserRequestDto requestDto);

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "passwordHash", source = "password", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "customerProfile", ignore = true)
    @Mapping(target = "adminProfile", ignore = true)
    void updateEntityFromDto(UserRequestDto requestDto, @MappingTarget User user);

    default String determineProfileType(User user) {
        if (user.getAdminProfile() != null) {
            return "ADMIN";
        } else if (user.getCustomerProfile() != null) {
            return "CUSTOMER";
        }
        return null;
    }
}







