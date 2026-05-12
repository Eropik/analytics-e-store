package com.estore.library.dto.analyze.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TurnoverPlanRowDto {
    private String month;
    private BigDecimal actualTurnover;
    private BigDecimal plannedTurnover;
    private BigDecimal planExecutionPercent;
    private BigDecimal sigma;
    private BigDecimal variationPercent;
}
