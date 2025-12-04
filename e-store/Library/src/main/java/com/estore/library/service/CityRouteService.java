package com.estore.library.service;

import com.estore.library.dto.city.RouteSummaryDto;
import com.estore.library.model.bisentity.CityRoute;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CityRouteService {
    
    CityRoute createRoute(CityRoute route);
    
    CityRoute updateRoute(Integer routeId, CityRoute route);
    
    void deleteRoute(Integer routeId);
    
    Optional<CityRoute> getRouteById(Integer routeId);
    
    List<CityRoute> getAllRoutes();
    
    List<CityRoute> getRoutesByCity(Integer cityId);
    
    List<CityRoute> getDirectRoutesBetweenCities(Integer cityAId, Integer cityBId);
    
    List<CityRoute> getRoutesWithinDistance(BigDecimal maxDistance);
    
    List<CityRoute> getDirectRoutesFromCity(Integer cityId);
    
    List<CityRoute> getDirectRoutesToCity(Integer cityId);
    
    boolean existsDirectRoute(Integer cityAId, Integer cityBId);
    
    List<Object[]> findAllRoutesBFS(String startCityName);
    
    Object[] findShortestRouteBFS(String startCityName, String endCityName);

    RouteSummaryDto findShortestRouteBFSById(Integer startCityId, Integer endCityId);//todo may  be Object[] return

    RouteSummaryDto findShortestRouteBFSByName(String startCityName, String endCityName);
}
