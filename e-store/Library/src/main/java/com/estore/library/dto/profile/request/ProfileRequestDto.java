package com.estore.library.dto.profile.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProfileRequestDto {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Integer cityId;
    private String addressText;
    private String profilePictureUrl;
    private Integer ordersCount;
    private BigDecimal totalSpent;
}


