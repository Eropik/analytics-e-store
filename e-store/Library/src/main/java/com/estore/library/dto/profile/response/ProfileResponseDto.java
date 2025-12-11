package com.estore.library.dto.profile.response;

import com.estore.library.dto.order.response.OrderResponseDto;
import com.estore.library.dto.user.response.UserResponseDto;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class ProfileResponseDto {
    private UUID userId;
    private UserResponseDto user;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private BigDecimal totalSpent;
    private Integer ordersCount;
    private Integer cityId;
    private String cityName;
    private String addressText;
    private String profilePictureUrl;
    private List<OrderResponseDto> orderHistory;
}


