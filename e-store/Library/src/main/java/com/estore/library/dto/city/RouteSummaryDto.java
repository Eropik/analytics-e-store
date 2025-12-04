package com.estore.library.dto.city;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

// RouteSummaryDto.java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummaryDto {
    private boolean found;
    private CityDto startCity;
    private CityDto endCity;
    private List<CityDto> pathCities; // Полный список городов в пути (включая start и end)
    private List<CityDto> intermediateCities; // Только промежуточные
    private BigDecimal totalDistance;
    private Long numberOfStops;
    private String pathName;
}