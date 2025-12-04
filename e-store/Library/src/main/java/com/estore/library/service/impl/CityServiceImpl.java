package com.estore.library.service.impl;

import com.estore.library.model.dicts.City;
import com.estore.library.repository.dicts.CityRepository;
import com.estore.library.service.CityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CityServiceImpl implements CityService {
    
    private final CityRepository cityRepository;
    
    @Override
    @Transactional
    public City createCity(City city) {
        if (cityRepository.existsByCityName(city.getCityName())) {
            throw new IllegalArgumentException("City already exists: " + city.getCityName());
        }
        return cityRepository.save(city);
    }
    
    @Override
    @Transactional
    public City updateCity(Integer cityId, City city) {
        City existing = cityRepository.findById(cityId)
                .orElseThrow(() -> new IllegalArgumentException("City not found with id: " + cityId));
        
        if (!existing.getCityName().equals(city.getCityName()) && 
            cityRepository.existsByCityName(city.getCityName())) {
            throw new IllegalArgumentException("City name already exists");
        }
        
        existing.setCityName(city.getCityName());
        return cityRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteCity(Integer cityId) {
        if (!cityRepository.existsById(cityId)) {
            throw new IllegalArgumentException("City not found with id: " + cityId);
        }
        cityRepository.deleteById(cityId);
    }
    
    @Override
    public Optional<City> getCityById(Integer cityId) {
        return cityRepository.findById(cityId);
    }
    
    @Override
    public Optional<City> getCityByName(String cityName) {
        return cityRepository.findByCityName(cityName);
    }
    
    @Override
    public List<City> getAllCities() {
        return cityRepository.findAllOrderByCityNameAsc();
    }
    
    @Override
    public List<City> searchCities(String search) {
        return cityRepository.searchByCityName(search);
    }
    
    @Override
    public boolean existsByName(String cityName) {
        return cityRepository.existsByCityName(cityName);
    }


}
