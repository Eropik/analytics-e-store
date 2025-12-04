package com.estore.library.dto.city;

import java.math.BigDecimal;

public interface RouteSummaryProjection {
    // Должны соответствовать именам столбцов в финальном SELECT
    String getCityName();            // соответствует 'city_name'
    Long getTransfers();             // соответствует 'transfers' (level - 1)
    BigDecimal getTotalDistance();   // соответствует 'total_distance'
    String getPathName();            // соответствует 'path_name'
}