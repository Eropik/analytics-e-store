package com.estore.library.dto.user.response;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserFullInfoDto {
    private UUID userId;
    private String email;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private Boolean isActive;

    // Роль
    private Integer roleId;
    private String roleName;

    // Профили (используем DTO)
    private CustomerProfileDto customerProfile;
    private AdminProfileDto adminProfile;
}
