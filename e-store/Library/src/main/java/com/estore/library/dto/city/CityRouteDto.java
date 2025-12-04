package com.estore.library.dto.city;

// package com.estore.admin.dto; (или подобный пакет)

import java.math.BigDecimal;

import com.estore.library.model.bisentity.CityRoute;
import lombok.Data;

@Data
public class CityRouteDto {
    private Integer routeId;

    // Вложенные DTO
    private CityDto cityA;
    private CityDto cityB;

    private BigDecimal distanceKm;

    /**
     * Преобразование сущности CityRoute в CityRouteDto с вложенными CityDto.
     */
    public static CityRouteDto fromEntity(CityRoute route) {
        CityRouteDto dto = new CityRouteDto();
        dto.setRouteId(route.getRouteId());

        // Используем статический метод CityDto для вложенного преобразования
        dto.setCityA(CityDto.fromEntity(route.getCityA()));
        dto.setCityB(CityDto.fromEntity(route.getCityB()));

        dto.setDistanceKm(route.getDistanceKm());
        return dto;
    }
}