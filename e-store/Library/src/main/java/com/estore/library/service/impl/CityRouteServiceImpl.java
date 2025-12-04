package com.estore.library.service.impl;

import com.estore.library.dto.city.CityDto;
import com.estore.library.dto.city.RoutePathProjection;
import com.estore.library.dto.city.RouteSummaryDto;
import com.estore.library.model.bisentity.CityRoute;
import com.estore.library.model.dicts.City;
import com.estore.library.repository.bisentity.CityRouteRepository;
import com.estore.library.service.CityRouteService;
import com.estore.library.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityRouteServiceImpl implements CityRouteService {
    
    private final CityRouteRepository cityRouteRepository;
    private final CityService cityService;;
    
    @Override
    @Transactional
    public CityRoute createRoute(CityRoute route) {
        if (route.getDistanceKm().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero");
        }
        return cityRouteRepository.save(route);
    }
    
    @Override
    @Transactional
    public CityRoute updateRoute(Integer routeId, CityRoute route) {
        CityRoute existingRoute = cityRouteRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with id: " + routeId));
        
        existingRoute.setCityA(route.getCityA());
        existingRoute.setCityB(route.getCityB());
        existingRoute.setDistanceKm(route.getDistanceKm());
        
        return cityRouteRepository.save(existingRoute);
    }
    
    @Override
    @Transactional
    public void deleteRoute(Integer routeId) {
        if (!cityRouteRepository.existsById(routeId)) {
            throw new IllegalArgumentException("Route not found with id: " + routeId);
        }
        cityRouteRepository.deleteById(routeId);
    }
    
    @Override
    public Optional<CityRoute> getRouteById(Integer routeId) {
        return cityRouteRepository.findById(routeId);
    }
    
    @Override
    public List<CityRoute> getAllRoutes() {
        return cityRouteRepository.findAll();
    }
    
    @Override
    public List<CityRoute> getRoutesByCity(Integer cityId) {
        return cityRouteRepository.findByCityId(cityId);
    }
    
    @Override
    public List<CityRoute> getDirectRoutesBetweenCities(Integer cityAId, Integer cityBId) {
        return cityRouteRepository.findByCityAAndCityB(cityAId, cityBId);
    }
    
    @Override
    public List<CityRoute> getRoutesWithinDistance(BigDecimal maxDistance) {
        return cityRouteRepository.findByMaxDistance(maxDistance);
    }
    
    @Override
    public List<CityRoute> getDirectRoutesFromCity(Integer cityId) {
        return cityRouteRepository.findDirectRoutesFromCity(cityId);
    }
    
    @Override
    public List<CityRoute> getDirectRoutesToCity(Integer cityId) {
        return cityRouteRepository.findDirectRoutesToCity(cityId);
    }
    
    @Override
    public boolean existsDirectRoute(Integer cityAId, Integer cityBId) {
        return cityRouteRepository.existsDirectRoute(cityAId, cityBId);
    }
    
    @Override
    public List<Object[]> findAllRoutesBFS(String startCityName) {
        if (startCityName == null || startCityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Start city name cannot be empty");
        }
        return cityRouteRepository.findAllRoutesBFS(startCityName);
    }
    
    @Override
    public Object[] findShortestRouteBFS(String startCityName, String endCityName) {
        if (startCityName == null || startCityName.trim().isEmpty()) {
            throw new IllegalArgumentException("Start city name cannot be empty");
        }
        if (endCityName == null || endCityName.trim().isEmpty()) {
            throw new IllegalArgumentException("End city name cannot be empty");
        }
        return cityRouteRepository.findShortestRouteBFS(startCityName, endCityName);
    }

    @Override
    public RouteSummaryDto findShortestRouteBFSById(Integer startCityId, Integer endCityId) {
        if (startCityId == null || endCityId == null) {
            throw new IllegalArgumentException("City IDs cannot be null");
        }

        // 1. Получаем проекцию из репозитория
        RoutePathProjection projection = cityRouteRepository.findShortestRouteBFSById(startCityId, endCityId);

        if (projection == null) {
            return null; // Маршрут не найден
        }

        // 2. Получаем полный список городов по ID
        List<City> fullPathEntities = new ArrayList<>();
        if (projection.getPathIds() != null) {
            for (Integer cityId : projection.getPathIds()) {
                cityService.getCityById(cityId).ifPresent(fullPathEntities::add);
            }
        }

        // 3. Преобразуем в DTO
        List<CityDto> pathDtos = fullPathEntities.stream()
                .map(CityDto::fromEntity)
                .toList();

        // 4. Определяем стартовый, конечный и промежуточные города
        CityDto startCityDto = pathDtos.get(0);
        CityDto endCityDto = pathDtos.get(pathDtos.size() - 1);

        List<CityDto> intermediateCities = pathDtos.size() > 2
                ? pathDtos.subList(1, pathDtos.size() - 1)
                : List.of();

        // 5. Создаем финальный RouteSummaryDto
        return new RouteSummaryDto(
                true,
                startCityDto,
                endCityDto,
                pathDtos,
                intermediateCities,
                projection.getTotalDistance(),
                projection.getTransfers(),
                projection.getPathName()
        );
    }

    // В CityRouteServiceImpl.java

    @Override
    public RouteSummaryDto findShortestRouteBFSByName(String startCityName, String endCityName) {
        if (startCityName == null || startCityName.trim().isEmpty() ||
                endCityName == null || endCityName.trim().isEmpty()) {
            throw new IllegalArgumentException("City names cannot be empty");
        }

        // 1. Получаем проекцию из репозитория
        RoutePathProjection projection = cityRouteRepository.findShortestRouteByName(startCityName, endCityName);

        if (projection == null) {
            return null; // Маршрут не найден
        }

        // 2. Получаем полный список городов по ID (из массива path_ids)
        List<City> fullPathEntities = new ArrayList<>();
        if (projection.getPathIds() != null) {
            for (Integer cityId : projection.getPathIds()) {
                cityService.getCityById(cityId).ifPresent(fullPathEntities::add);
            }
        }

        // 3. Строим RouteSummaryDto (используя ту же логику, что и раньше)
        List<CityDto> pathDtos = fullPathEntities.stream()
                .map(CityDto::fromEntity)
                .toList();

        if (pathDtos.isEmpty()) {
            return null;
        }

        CityDto startCityDto = pathDtos.get(0);
        CityDto endCityDto = pathDtos.get(pathDtos.size() - 1);

        List<CityDto> intermediateCities = pathDtos.size() > 2
                ? pathDtos.subList(1, pathDtos.size() - 1)
                : List.of();

        return new RouteSummaryDto(
                true,
                startCityDto,
                endCityDto,
                pathDtos,
                intermediateCities,
                projection.getTotalDistance(),
                projection.getTransfers(),
                projection.getPathName()
        );
    }
}
