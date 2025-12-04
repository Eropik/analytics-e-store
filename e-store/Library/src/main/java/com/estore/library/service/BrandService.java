package com.estore.library.service;

import com.estore.library.model.dicts.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandService {
    
    Brand createBrand(Brand brand);
    
    Brand updateBrand(Integer brandId, Brand brand);
    
    void deleteBrand(Integer brandId);
    
    Optional<Brand> getBrandById(Integer brandId);
    
    Optional<Brand> getBrandByName(String brandName);
    
    List<Brand> getAllBrands();
    
    List<Brand> searchBrands(String search);
    
    boolean existsByName(String brandName);
}
