package com.estore.library.dto.user.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class AdminProfileDto {
    private UUID userId;
    private String firstName;
    private String lastName;
    private LocalDate hireDate;
    private String profilePictureUrl;
    // Сюда можно добавить поля AdminDepartment, если они нужны
    private Integer departmentId;
    private String departmentName;
}