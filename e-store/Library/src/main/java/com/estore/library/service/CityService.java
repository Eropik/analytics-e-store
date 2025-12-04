package com.estore.library.service;

import com.estore.library.model.dicts.City;

import java.util.List;
import java.util.Optional;

public interface CityService {
    
    City createCity(City city);
    
    City updateCity(Integer cityId, City city);
    
    void deleteCity(Integer cityId);
    
    Optional<City> getCityById(Integer cityId);
    
    Optional<City> getCityByName(String cityName);
    
    List<City> getAllCities();
    
    List<City> searchCities(String search);
    
    boolean existsByName(String cityName);


}
