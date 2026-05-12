package com.estore.library.dto.analyze.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GroupShareRowDto {
    private String groupName;
    private BigDecimal groupTurnover;
    private BigDecimal totalTurnover;
    private BigDecimal sharePercent;
}
