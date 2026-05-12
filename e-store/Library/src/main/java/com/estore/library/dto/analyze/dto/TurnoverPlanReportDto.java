package com.estore.library.dto.analyze.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TurnoverPlanReportDto {
    private String metric;
    private String startMonth;
    private String endMonth;
    private BigDecimal totalActual;
    private BigDecimal totalPlanned;
    private BigDecimal avgActual;
    private BigDecimal sigma;
    private BigDecimal variationPercent;
    private List<TurnoverPlanRowDto> rows;
    private List<TimeSeriesItemDto> executionSeries;
    private List<TimeSeriesItemDto> sigmaSeries;
    private List<TimeSeriesItemDto> variationSeries;
}
