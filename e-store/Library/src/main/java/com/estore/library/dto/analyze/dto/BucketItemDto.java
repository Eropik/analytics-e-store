package com.estore.library.dto.analyze.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BucketItemDto {
    private String label;
    private BigDecimal value;
}

