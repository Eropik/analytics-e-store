package com.estore.library.service.impl;

import com.estore.library.model.dicts.Brand;
import com.estore.library.repository.dicts.BrandRepository;
import com.estore.library.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BrandServiceImpl implements BrandService {
    
    private final BrandRepository brandRepository;
    
    @Override
    @Transactional
    public Brand createBrand(Brand brand) {
        if (brandRepository.existsByBrandName(brand.getBrandName())) {
            throw new IllegalArgumentException("Brand already exists: " + brand.getBrandName());
        }
        return brandRepository.save(brand);
    }
    
    @Override
    @Transactional
    public Brand updateBrand(Integer brandId, Brand brand) {
        Brand existing = brandRepository.findById(brandId)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + brandId));
        
        if (!existing.getBrandName().equals(brand.getBrandName()) && 
            brandRepository.existsByBrandName(brand.getBrandName())) {
            throw new IllegalArgumentException("Brand name already exists");
        }
        
        existing.setBrandName(brand.getBrandName());
        return brandRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteBrand(Integer brandId) {
        if (!brandRepository.existsById(brandId)) {
            throw new IllegalArgumentException("Brand not found with id: " + brandId);
        }
        brandRepository.deleteById(brandId);
    }
    
    @Override
    public Optional<Brand> getBrandById(Integer brandId) {
        return brandRepository.findById(brandId);
    }
    
    @Override
    public Optional<Brand> getBrandByName(String brandName) {
        return brandRepository.findByBrandName(brandName);
    }
    
    @Override
    public List<Brand> getAllBrands() {
        return brandRepository.findAllOrderByBrandNameAsc();
    }
    
    @Override
    public List<Brand> searchBrands(String search) {
        return brandRepository.searchByBrandName(search);
    }
    
    @Override
    public boolean existsByName(String brandName) {
        return brandRepository.existsByBrandName(brandName);
    }
}
