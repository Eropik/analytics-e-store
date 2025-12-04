package com.estore.library.service;

import com.estore.library.model.dicts.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {
    
    Category createCategory(Category category);
    
    Category updateCategory(Integer categoryId, Category category);
    
    void deleteCategory(Integer categoryId);
    
    Optional<Category> getCategoryById(Integer categoryId);
    
    Optional<Category> getCategoryByName(String categoryName);
    
    List<Category> getAllCategories();
    
    List<Category> searchCategories(String search);
    
    boolean existsByName(String categoryName);
}
