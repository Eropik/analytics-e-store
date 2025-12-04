package com.estore.library.dto.user.response;

import com.estore.library.model.bisentity.AdminProfile;
import lombok.Data;
import java.util.UUID;

@Data
public class UserResponseDto {
    private UUID userId;
    private String email;
    private Boolean isActive;

    // Поля роли
    private Integer roleId;
    private String roleName;

    // Идентификация профиля (мы не вкладываем сюда полный профиль)
    private UUID profileId;
    private String profileType; // "CUSTOMER" или "ADMIN"

    private AdminProfileDto adminProfile;
    private CustomerProfileDto customerProfile;


}