package com.estore.library.service.impl;

import com.estore.library.model.dicts.Category;
import com.estore.library.repository.dicts.CategoryRepository;
import com.estore.library.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    
    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new IllegalArgumentException("Category already exists: " + category.getCategoryName());
        }
        return categoryRepository.save(category);
    }
    
    @Override
    @Transactional
    public Category updateCategory(Integer categoryId, Category category) {
        Category existing = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + categoryId));
        
        if (!existing.getCategoryName().equals(category.getCategoryName()) && 
            categoryRepository.existsByCategoryName(category.getCategoryName())) {
            throw new IllegalArgumentException("Category name already exists");
        }
        
        existing.setCategoryName(category.getCategoryName());
        return categoryRepository.save(existing);
    }
    
    @Override
    @Transactional
    public void deleteCategory(Integer categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new IllegalArgumentException("Category not found with id: " + categoryId);
        }
        categoryRepository.deleteById(categoryId);
    }
    
    @Override
    public Optional<Category> getCategoryById(Integer categoryId) {
        return categoryRepository.findById(categoryId);
    }
    
    @Override
    public Optional<Category> getCategoryByName(String categoryName) {
        return categoryRepository.findByCategoryName(categoryName);
    }
    
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAllOrderByCategoryNameAsc();
    }
    
    @Override
    public List<Category> searchCategories(String search) {
        return categoryRepository.searchByCategoryName(search);
    }
    
    @Override
    public boolean existsByName(String categoryName) {
        return categoryRepository.existsByCategoryName(categoryName);
    }
}
