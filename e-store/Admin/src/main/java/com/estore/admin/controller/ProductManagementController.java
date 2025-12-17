package com.estore.admin.controller;

import com.estore.library.dto.product.dto.BrandDto;
import com.estore.library.dto.product.dto.CategoryDto;
import com.estore.library.dto.product.request.ProductCreateRequest;
import com.estore.library.dto.product.response.ProductImageResponseDto;
import com.estore.library.dto.product.response.ProductResponseDto;
import com.estore.library.model.bisentity.Product;
import com.estore.library.model.dicts.Brand;
import com.estore.library.model.dicts.Category;
import com.estore.library.model.dicts.ProductImage;
import com.estore.library.service.*;
import com.estore.library.utils.ImageUpload;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.estore.library.dto.product.request.ProductCreateRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Контроллер для отдела PRODUCT_MANAGE
 * Доступ: управление товарами, категориями, брендами
 */
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
        //@CrossOrigin(origins = "*")
@CrossOrigin(origins = {"http://localhost:3001", "null"})
public class ProductManagementController {
    
    private final AdminProfileService adminProfileService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final BrandService brandService;
    private final ProductImageService productImageService;

    private final ImageUpload imageUpload;
    
    private boolean checkAccess(UUID adminUserId) {
        return adminProfileService.hasProductManagementAccess(adminUserId);
    }

    /**
     * Получить все товары
     * GET /api/admin/products
     */
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. PRODUCT_MANAGE department required"));
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<Product> productsPage = productService.getAllProducts(pageable);

            // --- ПРЕОБРАЗОВАНИЕ В DTO ---
            List<ProductResponseDto> productDtos = productsPage.getContent().stream()
                    .map(this::convertToDto) // Используем маппер
                    .collect(Collectors.toList());
            // -----------------------------

            return ResponseEntity.ok(Map.of(
                    "products", productDtos, // Возвращаем список DTO
                    "currentPage", productsPage.getNumber(),
                    "totalPages", productsPage.getTotalPages(),
                    "totalItems", productsPage.getTotalElements()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ProductManagementController.java (Добавьте в раздел управления товарами)

    /**
     * Получить товары по ID категории и ID бренда.
     * GET /api/admin/products/filter
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getProductsByCategoryAndBrand(
            @RequestParam UUID adminUserId,
            @RequestParam Integer categoryId,
            @RequestParam Integer brandId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. PRODUCT_MANAGE department required"));
            }

            Pageable pageable = PageRequest.of(page, size);

            // Вызываем готовый метод сервиса
            Page<Product> productsPage = productService.getProductsByCategoryAndBrand(
                    categoryId,
                    brandId,
                    pageable
            );

            // Преобразование в DTO
            List<ProductResponseDto> productDtos = productsPage.getContent().stream()
                    .map(this::convertToDto)
                    .toList();

            return ResponseEntity.ok(Map.of(
                    "products", productDtos,
                    "currentPage", productsPage.getNumber(),
                    "totalPages", productsPage.getTotalPages(),
                    "totalItems", productsPage.getTotalElements()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    /**
     * Вспомогательный метод для преобразования сущности Product в DTO.
     */
    private ProductResponseDto convertToDto(Product product) {
        ProductResponseDto dto = new ProductResponseDto();

        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setMainImageUrl(product.getMainImageUrl());

        if (product.getCategory() != null) {
            CategoryDto category = new CategoryDto(product.getCategory().getCategoryId(),product.getCategory().getCategoryName());
            dto.setCategory(category);

        }

        if (product.getBrand() != null) {
            BrandDto brand = new BrandDto(product.getBrand().getBrandId(),product.getBrand().getBrandName());
            dto.setBrand(brand);

        }




        if (product.getImages() != null) {
            List<ProductImageResponseDto> imageDtos = product.getImages().stream()
                    .map(image -> {
                        ProductImageResponseDto imageDto = new ProductImageResponseDto();
                        imageDto.setImageId(image.getImageId()); // Важно для операций DELETE
                        imageDto.setImageUrl(image.getImageUrl());
                        imageDto.setSortOrder(image.getSortOrder());
                        return imageDto;
                    })
                    .collect(Collectors.toList());
            dto.setImages(imageDtos);
        } else {
            dto.setImages(List.of());
        }

        return dto;
    }


    /**
     * Создать товар с основным и дополнительными изображениями.
     * POST /api/admin/products
     */
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestParam UUID adminUserId,
            // Теперь принимаем DTO, а не саму сущность Product
            @RequestBody ProductCreateRequest request) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }


            Product created = productService.createProductWithImages(request);
            // -------------------
            ProductResponseDto responseDto = convertToDto(created);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "product", responseDto));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Обновить товар
     * PUT /api/admin/products/{productId}
     */
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId,
            @RequestBody Product product) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Product updated = productService.updateProduct(productId, product);
            ProductResponseDto responseDto = convertToDto(updated);
            return ResponseEntity.ok(Map.of("success", true, "product", responseDto));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Удалить товар
     * DELETE /api/admin/products/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            productService.deleteProduct(productId);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Обновить остатки товара
     * PUT /api/admin/products/{productId}/stock
     */
    @PutMapping("/{productId}/stock")
    public ResponseEntity<?> updateStock(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId,
            @RequestParam Integer quantity) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            productService.updateStock(productId, quantity);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Stock updated"));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Товары с низкими остатками
     * GET /api/admin/products/low-stock
     */
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockProducts(
            @RequestParam UUID adminUserId,
            @RequestParam(defaultValue = "10") Integer threshold) {
        
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            
            Pageable pageable = PageRequest.of(0, 100);
            Page<Product> products = productService.getLowStockProducts(threshold, pageable);

            List<ProductResponseDto> productDtos = products.getContent().stream()
                    .map(this::convertToDto) // Используем маппер
                    .toList();

            //return ResponseEntity.ok(products.getContent());

            return ResponseEntity.ok(Map.of(
                    "products", productDtos, // Возвращаем список DTO
                    "currentPage", products.getNumber(),
                    "totalPages", products.getTotalPages(),
                    "totalItems", products.getTotalElements()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== КАТЕГОРИИ ==========
    
    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            return ResponseEntity.ok(categoryService.getAllCategories());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(
            @RequestParam UUID adminUserId,
            @RequestBody Category category) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            Category created = categoryService.createCategory(category);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "category", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(
            @RequestParam UUID adminUserId,
            @PathVariable Integer categoryId,
            @RequestBody Category category) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            Category updated = categoryService.updateCategory(categoryId, category);
            return ResponseEntity.ok(Map.of("success", true, "category", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(
            @RequestParam UUID adminUserId,
            @PathVariable Integer categoryId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Category deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/categories/search")
    public ResponseEntity<?> searchCategories(
            @RequestParam UUID adminUserId,
            @RequestParam String query) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            return ResponseEntity.ok(categoryService.searchCategories(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    // ========== БРЕНДЫ ==========
    
    @GetMapping("/brands")
    public ResponseEntity<?> getAllBrands(@RequestParam UUID adminUserId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            return ResponseEntity.ok(brandService.getAllBrands());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/brands")
    public ResponseEntity<?> createBrand(
            @RequestParam UUID adminUserId,
            @RequestBody Brand brand) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            Brand created = brandService.createBrand(brand);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "brand", created));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/brands/{brandId}")
    public ResponseEntity<?> updateBrand(
            @RequestParam UUID adminUserId,
            @PathVariable Integer brandId,
            @RequestBody Brand brand) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            Brand updated = brandService.updateBrand(brandId, brand);
            return ResponseEntity.ok(Map.of("success", true, "brand", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/brands/{brandId}")
    public ResponseEntity<?> deleteBrand(
            @RequestParam UUID adminUserId,
            @PathVariable Integer brandId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            brandService.deleteBrand(brandId);
            return ResponseEntity.ok(Map.of("success", true, "message", "Brand deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/brands/search")
    public ResponseEntity<?> searchBrands(
            @RequestParam UUID adminUserId,
            @RequestParam String query) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }
            return ResponseEntity.ok(brandService.searchBrands(query));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== ИЗОБРАЖЕНИЯ ТОВАРОВ ==========

    /**
     * Получить изображения товара
     * GET /api/admin/products/{productId}/images
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            var images = productImageService.getProductImagesOrderedBySortOrder(productId);
            return ResponseEntity.ok(images);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    private ProductImageResponseDto convertToImageDto(com.estore.library.model.dicts.ProductImage image) {
        ProductImageResponseDto dto = new ProductImageResponseDto();
        dto.setImageId(image.getImageId());
        dto.setImageUrl(image.getImageUrl());
        dto.setSortOrder(image.getSortOrder());
        return dto;
    }


    /**
     * Получить главное изображение (первое в порядке сортировки) для продукта.
     * GET /api/admin/products/{productId}/image/main
     */
    @GetMapping("/{productId}/image/main")
    public ResponseEntity<?> getMainProductImage(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            Optional<ProductImage> imageOptional = productImageService.findFirstByProductIdOrderBySortOrder(productId);

            if (imageOptional.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Main image not found for product: " + productId));
            }

            // --- ПРЕОБРАЗОВАНИЕ В DTO ---
            // Используем ранее созданный маппер convertToImageDto
            ProductImage image = imageOptional.get();
            ProductImageResponseDto imageDto = convertToImageDto(image);

            return ResponseEntity.ok(imageDto);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Добавить изображение к товару
     * POST /api/admin/products/{productId}/images
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<?> addProductImage(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId,
            @RequestBody ProductImageRequest request) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            var product = productService.getProductById(productId);
            if (product.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var productImage = new com.estore.library.model.dicts.ProductImage();
            productImage.setProduct(product.get());
            productImage.setImageUrl(request.getImageUrl());
            productImage.setSortOrder(request.getSortOrder());

            var created = productImageService.createProductImage(productImage);

            // --- ИСПРАВЛЕНИЕ: Преобразование в DTO перед ответом ---
            ProductImageResponseDto responseDto = convertToImageDto(created);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "image", responseDto)); // Возвращаем DTO

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    /**
     * Удалить изображение товара
     * DELETE /api/admin/products/images/{imageId}
     */
    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @RequestParam UUID adminUserId,
            @PathVariable Integer imageId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            productImageService.deleteProductImage(imageId);

            return ResponseEntity.ok(Map.of("success", true, "message", "Image deleted"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

// ProductManagementController.java

    /**
     * Удалить изображение товара по ID товара и ID изображения.
     * DELETE /api/admin/products/{productId}/images/{imageId}
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    public ResponseEntity<?> deleteProductImageByProduct(
            @RequestParam UUID adminUserId,
            @PathVariable UUID productId,
            @PathVariable Integer imageId) {
        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied"));
            }

            // Вызываем новый метод сервиса
            productImageService.deleteProductImageByProductIdAndImageId(productId, imageId);

            return ResponseEntity.ok(Map.of("success", true, "message",
                    "Image ID " + imageId + " successfully deleted from Product ID " + productId));

        } catch (IllegalArgumentException e) {
            // Ловим ошибки "не найдено" или "не принадлежит"
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ========== DTO ==========

    public static class ProductImageRequest {
        private String imageUrl;
        private Integer sortOrder;

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }

    /**
     * Загрузить файл изображения на сервер и получить публичный URL.
     * POST /api/admin/products/upload-image
     * @param file Файл, отправленный как multipart/form-data.
     */
    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadProductImageFile(
            @RequestParam UUID adminUserId,
            @RequestParam("file") MultipartFile file) {

        try {
            if (!checkAccess(adminUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied. PRODUCT_MANAGE department required"));
            }

            String imageUrl = imageUpload.uploadFileAndGetUrl(file);

            if (imageUrl == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Failed to upload image file"));
            }

            // Возвращаем URL, который будет сохранен в базе данных
            return ResponseEntity.ok(Map.of("success", true, "imageUrl", imageUrl));

        } catch (RuntimeException e) {
            // Ловим исключение, если не удалось создать директорию
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Server storage error: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
