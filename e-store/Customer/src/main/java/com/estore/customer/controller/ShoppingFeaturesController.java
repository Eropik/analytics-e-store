package com.estore.customer.controller;

import com.estore.library.model.bisentity.Product;
import com.estore.library.model.dicts.ProductImage;
import com.estore.library.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Контроллер расширенных функций покупок
 * Сравнение товаров, избранное, фильтры
 */
@RestController
@RequestMapping("/api/customer/shopping")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ShoppingFeaturesController {
    
    private final ProductService productService;
    private final ProductImageService productImageService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final OrderItemService orderItemService;
    
    /**
     * Сравнение товаров
     * POST /api/customer/shopping/compare
     */
    @PostMapping("/compare")
    public ResponseEntity<?> compareProducts(@RequestBody CompareRequest request) {
        try {
            if (request.getProductIds() == null || request.getProductIds().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Product IDs are required"));
            }
            
            List<Map<String, Object>> productDetails = new ArrayList<>();
            
            for (UUID productId : request.getProductIds()) {
                Optional<Product> productOpt = productService.getProductById(productId);
                if (productOpt.isPresent()) {
                    Product product = productOpt.get();
                    
                    Map<String, Object> details = new HashMap<>();
                    details.put("product", product);
                    details.put("images", productImageService.getProductImagesOrderedBySortOrder(productId));
                    
                    productDetails.add(details);
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "products", productDetails,
                "count", productDetails.size()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Детали товара с полной информацией
     * GET /api/customer/shopping/product/{productId}/details
     */
    @GetMapping("/product/{productId}/details")
    public ResponseEntity<?> getProductFullDetails(@PathVariable UUID productId) {
        try {
            Optional<Product> productOpt = productService.getProductById(productId);
            
            if (productOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Product not found"));
            }
            
            Product product = productOpt.get();
            
            // Получить изображения
            List<ProductImage> images = productImageService.getProductImagesOrderedBySortOrder(productId);
            
            // Получить статистику продаж
            Long totalSold = orderItemService.getTotalQuantitySoldForProduct(productId);
            
            // Получить похожие товары из той же категории
            Pageable pageable = PageRequest.of(0, 5);
            List<Product> similarProducts = new ArrayList<>();
            if (product.getCategory() != null) {
                Page<Product> similar = productService.getProductsByCategory(
                    product.getCategory().getCategoryId(), pageable);
                similarProducts = similar.getContent().stream()
                    .filter(p -> !p.getProductId().equals(productId))
                    .toList();
            }
            
            Map<String, Object> fullDetails = new HashMap<>();
            fullDetails.put("product", product);
            fullDetails.put("images", images);
            fullDetails.put("totalSold", totalSold);
            fullDetails.put("similarProducts", similarProducts);
            fullDetails.put("inStock", product.getStockQuantity() > 0);
            fullDetails.put("stockStatus", getStockStatus(product.getStockQuantity()));
            
            return ResponseEntity.ok(fullDetails);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Расширенный поиск с множественными фильтрами
     * POST /api/customer/shopping/advanced-search
     */
    @PostMapping("/advanced-search")
    public ResponseEntity<?> advancedSearch(@RequestBody AdvancedSearchRequest request) {
        try {
            Pageable pageable = PageRequest.of(
                request.getPage() != null ? request.getPage() : 0,
                request.getSize() != null ? request.getSize() : 20
            );
            
            Page<Product> productsPage;
            
            // Применить фильтры в приоритетном порядке
            if (request.getCategoryId() != null) {
                productsPage = productService.getProductsByCategory(request.getCategoryId(), pageable);
            } else if (request.getBrandId() != null) {
                productsPage = productService.getProductsByBrand(request.getBrandId(), pageable);
            } else if (request.getMinPrice() != null && request.getMaxPrice() != null) {
                productsPage = productService.getProductsByPriceRange(
                    request.getMinPrice(), request.getMaxPrice(), pageable);
            } else if (request.getSearchQuery() != null && !request.getSearchQuery().isEmpty()) {
                productsPage = productService.searchProducts(request.getSearchQuery(), pageable);
            } else {
                productsPage = productService.getAvailableProducts(pageable);
            }
            
            // Дополнительная фильтрация по цене если нужно
            List<Product> filteredProducts = productsPage.getContent();
            if (request.getMinPrice() != null || request.getMaxPrice() != null) {
                filteredProducts = filteredProducts.stream()
                    .filter(p -> {
                        if (request.getMinPrice() != null && 
                            p.getPrice().compareTo(request.getMinPrice()) < 0) {
                            return false;
                        }
                        if (request.getMaxPrice() != null && 
                            p.getPrice().compareTo(request.getMaxPrice()) > 0) {
                            return false;
                        }
                        return true;
                    })
                    .toList();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("products", filteredProducts);
            response.put("currentPage", productsPage.getNumber());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("appliedFilters", Map.of(
                "categoryId", request.getCategoryId(),
                "brandId", request.getBrandId(),
                "priceRange", Map.of(
                    "min", request.getMinPrice(),
                    "max", request.getMaxPrice()
                ),
                "searchQuery", request.getSearchQuery()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Получить фильтры для каталога
     * GET /api/customer/shopping/filters
     */
    @GetMapping("/filters")
    public ResponseEntity<?> getFilters() {
        try {
            Map<String, Object> filters = new HashMap<>();
            
            // Категории
            filters.put("categories", categoryService.getAllCategories());
            
            // Бренды
            filters.put("brands", brandService.getAllBrands());
            
            // Ценовые диапазоны (пример)
            filters.put("priceRanges", List.of(
                Map.of("label", "До 1000", "min", 0, "max", 1000),
                Map.of("label", "1000 - 5000", "min", 1000, "max", 5000),
                Map.of("label", "5000 - 10000", "min", 5000, "max", 10000),
                Map.of("label", "10000 - 50000", "min", 10000, "max", 50000),
                Map.of("label", "Более 50000", "min", 50000, "max", Integer.MAX_VALUE)
            ));
            
            return ResponseEntity.ok(filters);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Топ продаж
     * GET /api/customer/shopping/bestsellers
     */
    @GetMapping("/bestsellers")
    public ResponseEntity<?> getBestsellers() {
        try {
            List<Object[]> topSelling = orderItemService.getTopSellingProducts();
            
            List<Map<String, Object>> bestsellers = new ArrayList<>();
            for (Object[] item : topSelling) {
                UUID productId = (UUID) item[0];
                Long totalSold = ((Number) item[1]).longValue();
                
                Optional<Product> productOpt = productService.getProductById(productId);
                if (productOpt.isPresent()) {
                    Map<String, Object> seller = new HashMap<>();
                    seller.put("product", productOpt.get());
                    seller.put("totalSold", totalSold);
                    bestsellers.add(seller);
                }
                
                if (bestsellers.size() >= 10) break;
            }
            
            return ResponseEntity.ok(Map.of("bestsellers", bestsellers));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Акции и скидки (товары с низкой ценой относительно категории)
     * GET /api/customer/shopping/deals
     */
    @GetMapping("/deals")
    public ResponseEntity<?> getDeals() {
        try {
            // Получить товары с ценой ниже средней в категории
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> allProducts = productService.getAvailableProducts(pageable);
            
            // Простая логика: товары со скидкой или низкой ценой
            List<Product> deals = allProducts.getContent().stream()
                .filter(p -> p.getPrice().compareTo(BigDecimal.valueOf(1000)) < 0)
                .limit(10)
                .toList();
            
            return ResponseEntity.ok(Map.of("deals", deals));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Вспомогательные методы
    private String getStockStatus(Integer quantity) {
        if (quantity == null || quantity == 0) {
            return "OUT_OF_STOCK";
        } else if (quantity < 5) {
            return "LOW_STOCK";
        } else {
            return "IN_STOCK";
        }
    }
    
    // DTO классы
    public static class CompareRequest {
        private List<UUID> productIds;
        
        public List<UUID> getProductIds() { return productIds; }
        public void setProductIds(List<UUID> productIds) { this.productIds = productIds; }
    }
    
    public static class AdvancedSearchRequest {
        private String searchQuery;
        private Integer categoryId;
        private Integer brandId;
        private BigDecimal minPrice;
        private BigDecimal maxPrice;
        private Integer page;
        private Integer size;
        
        public String getSearchQuery() { return searchQuery; }
        public void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
        
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
        
        public Integer getBrandId() { return brandId; }
        public void setBrandId(Integer brandId) { this.brandId = brandId; }
        
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
        
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }
        
        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }
    }
}
