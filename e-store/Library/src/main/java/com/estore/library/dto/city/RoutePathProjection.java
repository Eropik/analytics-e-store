package com.estore.library.dto.city;

import java.math.BigDecimal;

public interface RoutePathProjection {
    String getCityName();
    Integer getEndCityId();         // Новое поле
    Integer[] getPathIds();         // Новое поле для массива ID
    Long getTransfers();
    BigDecimal getTotalDistance();
    String getPathName();
}