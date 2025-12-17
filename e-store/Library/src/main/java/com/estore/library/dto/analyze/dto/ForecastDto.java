package com.estore.library.dto.analyze.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ForecastDto {
    private List<BigDecimal> monthlySales;      // Исторические продажи по месяцам
    private List<BigDecimal> movingAverage;     // Скользящее среднее
    private BigDecimal forecast;                // Прогноз на следующий месяц
}
