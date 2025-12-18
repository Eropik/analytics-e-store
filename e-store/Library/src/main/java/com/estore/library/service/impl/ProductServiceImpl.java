package com.estore.library.service.impl;

import com.estore.library.dto.product.request.ProductCreateRequest;
import com.estore.library.model.bisentity.Product;
import com.estore.library.model.dicts.Brand;
import com.estore.library.model.dicts.Category;
import com.estore.library.model.dicts.ProductImage;
import com.estore.library.repository.bisentity.ProductRepository;
import com.estore.library.service.BrandService;
import com.estore.library.service.CategoryService;
import com.estore.library.service.ProductImageService;
import com.estore.library.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductImageService productImageService;
    private final ProductRepository productRepository;
    
    @Override
    @Transactional
    public Product createProduct(Product product) {
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsAvailable(true);
        product.setAverageRating(BigDecimal.ZERO);
        product.setRatingsCount(0);
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product updateProduct(UUID productId, Product product) {
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setStockQuantity(product.getStockQuantity());
        existingProduct.setCategory(product.getCategory());
        existingProduct.setBrand(product.getBrand());
        existingProduct.setMainImageUrl(product.getMainImageUrl());
        existingProduct.setUpdatedAt(LocalDateTime.now());
        
        return productRepository.save(existingProduct);
    }
    @Override
    @Transactional
    public Product createProductWithImages(ProductCreateRequest request) {

        // 1. Поиск Category и Brand по ID
        Category category = categoryService.getCategoryById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + request.getCategoryId()));

        Brand brand = brandService.getBrandById(request.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found with id: " + request.getBrandId()));

        // 2. Создание сущности Product из DTO
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setMainImageUrl(request.getMainImageUrl());

        // Привязка найденных сущностей
        product.setCategory(category);
        product.setBrand(brand);

        // Установка стандартных полей (как в вашем createProduct)
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        product.setIsAvailable(true);
        product.setAverageRating(BigDecimal.ZERO);
        product.setRatingsCount(0);

        // Сохраняем продукт, чтобы получить его ID (ОЧЕНЬ ВАЖНО)
        Product savedProduct = productRepository.save(product);

        // 3. Создание и сохранение ProductImage
        if (request.getImages() != null && !request.getImages().isEmpty()) {

            int defaultSortOrder = 1;

            for (ProductCreateRequest.ProductImageDto imageDto : request.getImages()) {
                ProductImage productImage = new ProductImage();
                productImage.setProduct(savedProduct); // Привязка к только что созданному продукту
                productImage.setImageUrl(imageDto.getImageUrl());

                // Используем порядок сортировки из DTO, если не задан, используем инкрементальный
                productImage.setSortOrder(imageDto.getSortOrder() != null
                        ? imageDto.getSortOrder()
                        : defaultSortOrder++);

                // Используем сервис для сохранения (это может быть и пакетное сохранение, но пока используем по одному)
                productImageService.createProductImage(productImage);
            }
        }

        return savedProduct;
    }
    @Override
    @Transactional
    public void deleteProduct(UUID productId) {
        if (!productRepository.existsById(productId)) {
            throw new IllegalArgumentException("Product not found with id: " + productId);
        }
        productRepository.deleteById(productId);
    }
    
    @Override
    public Optional<Product> getProductById(UUID productId) {
        return productRepository.findById(productId);
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {

        //todo check repo there is some logics with FETCH for name of category and brand
        return productRepository.findAllWithCategoryAndBrand(pageable);
    }
    
    @Override
    public Page<Product> getAvailableProducts(Pageable pageable) {
        return productRepository.findByIsAvailable(true, pageable);
    }
    
    @Override
    public Page<Product> getProductsByCategory(Integer categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }
    
    @Override
    public Page<Product> getProductsByBrand(Integer brandId, Pageable pageable) {
        return productRepository.findByBrandId(brandId, pageable);
    }
    
    @Override
    public Page<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceRange(minPrice, maxPrice, pageable);
    }
    
    @Override
    public Page<Product> searchProducts(String search, Pageable pageable) {
        return productRepository.searchByNameOrDescription(search, pageable);
    }

    @Override
    public Page<Product> searchProductsAdvanced(UUID productId,
                                                Integer categoryId,
                                                Integer brandId,
                                                BigDecimal minPrice,
                                                BigDecimal maxPrice,
                                                String search,
                                                Pageable pageable) {
        return productRepository.searchAdvanced(productId, categoryId, brandId, minPrice, maxPrice, search, pageable);
    }
    
    @Override
    public Page<Product> getProductsByMinRating(BigDecimal minRating, Pageable pageable) {
        return productRepository.findByMinRating(minRating, pageable);
    }
    
    @Override
    public Page<Product> getInStockProducts(Pageable pageable) {
        return productRepository.findInStock(pageable);
    }
    
    @Override
    public Page<Product> getLowStockProducts(Integer threshold, Pageable pageable) {
        return productRepository.findLowStock(threshold, pageable);
    }
    
    @Override
    public Page<Product> getTopRatedProducts(Pageable pageable) {
        return productRepository.findTopRated(pageable);
    }
    
    @Override
    public Page<Product> getNewestProducts(Pageable pageable) {
        return productRepository.findNewest(pageable);
    }
    
    @Override
    public Page<Product> getProductsByCategoryAndBrand(Integer categoryId, Integer brandId, Pageable pageable) {
        return productRepository.findByCategoryAndBrand(categoryId, brandId, pageable);
    }
    
    @Override
    @Transactional
    public void updateStock(UUID productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        product.setStockQuantity(quantity);
        product.setUpdatedAt(LocalDateTime.now());
        
        if (quantity <= 0) {
            product.setIsAvailable(false);
        }
        
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void updateRating(UUID productId, BigDecimal newRating) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        
        Integer currentCount = product.getRatingsCount();
        BigDecimal currentRating = product.getAverageRating();
        
        BigDecimal totalRating = currentRating.multiply(BigDecimal.valueOf(currentCount)).add(newRating);
        Integer newCount = currentCount + 1;
        BigDecimal averageRating = totalRating.divide(BigDecimal.valueOf(newCount), 1, BigDecimal.ROUND_HALF_UP);
        
        product.setAverageRating(averageRating);
        product.setRatingsCount(newCount);
        product.setUpdatedAt(LocalDateTime.now());
        
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void markAsAvailable(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        product.setIsAvailable(true);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void markAsUnavailable(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        product.setIsAvailable(false);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);
    }



}
