package com.estore.library.dto.city;

import lombok.Data;
import com.estore.library.model.dicts.City; // Предполагая, что City тут

@Data
public class CityDto {
    private Integer cityId;
    private String cityName;

    public static CityDto fromEntity(City city) {
        if (city == null) return null;
        CityDto dto = new CityDto();
        dto.setCityId(city.getCityId());
        dto.setCityName(city.getCityName());
        return dto;
    }
}