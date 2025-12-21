package com.estore.library.dto.user.response;


import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CustomerProfileDto {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private BigDecimal totalSpent;
    private Integer ordersCount;
    // Сюда можно добавить поля City, если они нужны
    private Integer cityId;
    private LocalDate dateOfBirth;
    private String profilePictureUrl;
    private String gender;
}
