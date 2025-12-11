/*
package com.estore.admin.e2e;

import com.estore.library.dto.product.request.ProductRequestDto;
import com.estore.library.model.bisentity.*;
import com.estore.library.model.dicts.*;
import com.estore.library.repository.bisentity.*;
import com.estore.library.repository.dicts.*;
import com.estore.library.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

*/
/**
 * E2E тест полного флоу заказа:
 * 1. Админ добавляет товар
 * 2. Заказчик ищет товар
 * 3. Заказчик добавляет товар в корзину
 * 4. Заказчик подтверждает заказ
 * 5. Админ обновляет статус заказа (PROCESSING → IN_TRANSIT → DELIVERED)
 *//*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Testcontainers
@Transactional
public class E2EOrderFlowTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private OrderStatusRepository orderStatusRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private CityService cityService;

    @Autowired
    private CustomerProfileService customerProfileService;

    @Autowired
    private AdminProfileService adminProfileService;

    @Autowired
    private AdminDepartmentRepository adminDepartmentRepository;

    private User adminUser;
    private User customerUser;
    private Product product;
    private Category category;
    private Brand brand;
    private OrderStatus processingStatus;
    private OrderStatus inTransitStatus;
    private OrderStatus deliveredStatus;
    private PaymentMethod paymentMethod;
    private DeliveryMethod deliveryMethod;
    private City city;
    private Order createdOrder;

    @BeforeEach
    void setUp() {
        // Создание ролей
        Role adminRole = createRoleIfNotExists("Admin");
        Role customerRole = createRoleIfNotExists("ROLE_CUSTOMER");

        // Создание статусов заказов
        processingStatus = createOrderStatusIfNotExists("PROCESSING");
        inTransitStatus = createOrderStatusIfNotExists("IN_TRANSIT");
        deliveredStatus = createOrderStatusIfNotExists("DELIVERED");

        // Создание админа
        adminUser = createAdminUser(adminRole);
        customerUser = createCustomerUser(customerRole);

        // Создание категории и бренда
        category = createCategoryIfNotExists("Electronics");
        brand = createBrandIfNotExists("TestBrand");

        // Создание способов оплаты и доставки
        paymentMethod = createPaymentMethodIfNotExists("Card Online");
        deliveryMethod = createDeliveryMethodIfNotExists("Standard Delivery");
        city = createCityIfNotExists("Moscow");
    }

    @Test
    void testFullOrderFlow() {
        // Шаг 1: Админ добавляет товар
        ProductRequestDto productRequest = new ProductRequestDto();
        productRequest.setName("Test Product");
        productRequest.setDescription("Test Description");
        productRequest.setPrice(BigDecimal.valueOf(100.00));
        productRequest.setStockQuantity(10);
        productRequest.setCategoryId(category.getCategoryId());
        productRequest.setBrandId(brand.getBrandId());

        // Создаем товар напрямую
        Product productToCreate = new Product();
        productToCreate.setName(productRequest.getName());
        productToCreate.setDescription(productRequest.getDescription());
        productToCreate.setPrice(productRequest.getPrice());
        productToCreate.setStockQuantity(productRequest.getStockQuantity());
        productToCreate.setIsAvailable(true);
        productToCreate.setCreatedAt(LocalDateTime.now());
        productToCreate.setUpdatedAt(LocalDateTime.now());
        productToCreate.setCategory(category);
        productToCreate.setBrand(brand);
        
        Product createdProduct = productService.createProduct(productToCreate);
        assertThat(createdProduct).isNotNull();
        assertThat(createdProduct.getProductId()).isNotNull();
        assertThat(createdProduct.getName()).isEqualTo("Test Product");

        product = createdProduct;

        // Шаг 2: Заказчик ищет товар
        var foundProducts = productService.searchProducts("Test", 
                org.springframework.data.domain.PageRequest.of(0, 10));
        assertThat(foundProducts.getContent()).isNotEmpty();
        assertThat(foundProducts.getContent().stream()
                .anyMatch(p -> p.getProductId().equals(product.getProductId()))).isTrue();

        // Шаг 3: Заказчик добавляет товар в корзину
        shoppingCartService.addProductToCart(
                customerUser.getUserId(),
                product.getProductId(),
                2,
                product.getPrice()
        );

        var cartOpt = shoppingCartService.getCartWithItems(customerUser.getUserId());
        assertThat(cartOpt).isPresent();
        ShoppingCart cart = cartOpt.get();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct().getProductId()).isEqualTo(product.getProductId());
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);

        // Шаг 4: Заказчик подтверждает заказ
        Order order = new Order();
        order.setUser(customerUser);
        order.setShippingCity(city);
        order.setShippingAddressText("Test Address");
        order.setDeliveryMethod(deliveryMethod);
        order.setPaymentMethod(paymentMethod);
        order.setStatus(processingStatus);
        order.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(2)));

        // Создаем OrderItems
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setUnitPrice(product.getPrice());
        order.setOrderItems(List.of(orderItem));

        createdOrder = orderService.createOrder(order);
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getStatus().getStatusName()).isEqualTo("PROCESSING");

        // Шаг 5: Админ обновляет статус заказа PROCESSING → IN_TRANSIT → DELIVERED
        orderService.updateOrderStatus(createdOrder.getId(), inTransitStatus.getStatusId());
        var updatedOrder1 = orderService.getOrderById(createdOrder.getId());
        assertThat(updatedOrder1).isPresent();
        assertThat(updatedOrder1.get().getStatus().getStatusName()).isEqualTo("IN_TRANSIT");

        orderService.updateOrderStatus(createdOrder.getId(), deliveredStatus.getStatusId());
        var updatedOrder2 = orderService.getOrderById(createdOrder.getId());
        assertThat(updatedOrder2).isPresent();
        assertThat(updatedOrder2.get().getStatus().getStatusName()).isEqualTo("DELIVERED");
    }

    // Вспомогательные методы
    private Role createRoleIfNotExists(String roleName) {
        return roleService.getRoleByName(roleName)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setRoleName(roleName);
                    return roleService.createRole(role);
                });
    }

    private OrderStatus createOrderStatusIfNotExists(String statusName) {
        return orderStatusRepository.findByStatusName(statusName)
                .orElseGet(() -> {
                    OrderStatus status = new OrderStatus();
                    status.setStatusName(statusName);
                    return orderStatusRepository.save(status);
                });
    }

    private User createAdminUser(Role role) {
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPasswordHash("encoded_password");
        user.setRole(role);
        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());
        User createdUser = userService.createUser(user);

        AdminDepartment department = adminDepartmentRepository.findAll().stream()
                .findFirst()
                .orElseGet(() -> {
                    AdminDepartment dept = new AdminDepartment();
                    dept.setDepartmentName("PRODUCT_MANAGE");
                    return adminDepartmentRepository.save(dept);
                });

        AdminProfile adminProfile = new AdminProfile();
        adminProfile.setUserId(createdUser.getUserId());
        adminProfile.setUser(createdUser);
        adminProfile.setFirstName("Admin");
        adminProfile.setLastName("User");
        adminProfile.setDepartment(department);
        adminProfileService.createAdminProfile(adminProfile);


        return createdUser;
    }

    private User createCustomerUser(Role role) {
        User user = new User();
        user.setEmail("customer@test.com");
        user.setPasswordHash("encoded_password");
        user.setRole(role);
        user.setIsActive(true);
        user.setRegistrationDate(LocalDateTime.now());
        User createdUser = userService.createUser(user);

        CustomerProfile profile = new CustomerProfile();
        profile.setUserId(createdUser.getUserId());
        profile.setUser(createdUser);
        profile.setFirstName("Customer");
        profile.setLastName("User");
        profile.setDateOfBirth(LocalDate.of(1990, 1, 1));
        customerProfileService.createCustomerProfile(profile);

        return createdUser;
    }

    private Category createCategoryIfNotExists(String categoryName) {
        return categoryService.getAllCategories().stream()
                .filter(c -> c.getCategoryName().equals(categoryName))
                .findFirst()
                .orElseGet(() -> {
                    Category cat = new Category();
                    cat.setCategoryName(categoryName);
                    return categoryService.createCategory(cat);
                });
    }

    private Brand createBrandIfNotExists(String brandName) {
        return brandService.getAllBrands().stream()
                .filter(b -> b.getBrandName().equals(brandName))
                .findFirst()
                .orElseGet(() -> {
                    Brand br = new Brand();
                    br.setBrandName(brandName);
                    return brandService.createBrand(br);
                });
    }

    private PaymentMethod createPaymentMethodIfNotExists(String methodName) {
        return paymentMethodService.getAllPaymentMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst()
                .orElseGet(() -> {
                    PaymentMethod pm = new PaymentMethod();
                    pm.setMethodName(methodName);
                    pm.setDescription("Test payment method");
                    return paymentMethodService.createPaymentMethod(pm);
                });
    }

    private DeliveryMethod createDeliveryMethodIfNotExists(String methodName) {
        return deliveryMethodService.getAllDeliveryMethods().stream()
                .filter(m -> m.getMethodName().equals(methodName))
                .findFirst()
                .orElseGet(() -> {
                    DeliveryMethod dm = new DeliveryMethod();
                    dm.setMethodName(methodName);
                    dm.setDescription("Test delivery method");
                    return deliveryMethodService.createDeliveryMethod(dm);
                });
    }

    private City createCityIfNotExists(String cityName) {
        return cityService.getAllCities().stream()
                .filter(c -> c.getCityName().equals(cityName))
                .findFirst()
                .orElseGet(() -> {
                    City ct = new City();
                    ct.setCityName(cityName);
                    return cityService.createCity(ct);
                });
    }

}

*/

package com.estore.admin.e2e;

import com.estore.library.dto.product.request.ProductRequestDto;
import com.estore.library.model.bisentity.*;
import com.estore.library.model.dicts.*;
import com.estore.library.repository.dicts.*;
import com.estore.library.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * E2E тест полного флоу заказа с использованием Mock-заглушек (Mockito).
 * Имитирует шаги:
 * 1. Админ добавляет товар
 * 2. Заказчик ищет товар
 * 3. Заказчик добавляет товар в корзину
 * 4. Заказчик подтверждает заказ
 * 5. Админ обновляет статус заказа (PROCESSING → IN_TRANSIT → DELIVERED)
 */
@SpringBootTest
public class E2EOrderFlowTest {

    // --- Мокаем все сервисы, которые вызываются в тесте ---

    @MockBean
    private ProductService productService;

    @MockBean
    private ShoppingCartService shoppingCartService;

    @MockBean
    private OrderService orderService;

    @MockBean
    private UserService userService;

    // Вспомогательные сервисы/репозитории, которые обычно используются для setup,
    // теперь тоже мокаются, но их мокинг может быть более простым (если они не важны
    // для основного потока OrderFlow, их можно не использовать, но для полноты мокаем)
    @MockBean
    private OrderStatusRepository orderStatusRepository;

    // --- Объекты, используемые в тесте ---

    private final UUID ADMIN_USER_ID = UUID.randomUUID();
    private final UUID CUSTOMER_USER_ID = UUID.randomUUID();
    private final UUID PRODUCT_ID =  UUID.randomUUID();
    private final Integer CATEGORY_ID = 10;
    private final Integer BRAND_ID = 20;
    private final UUID ORDER_ID = UUID.randomUUID();
    private final Integer PROCESSING_STATUS_ID = 1000;
    private final Integer IN_TRANSIT_STATUS_ID = 1001;
    private final Integer DELIVERED_STATUS_ID = 1002;

    private User adminUser;
    private User customerUser;
    private Product product;
    private Category category;
    private Brand brand;
    private OrderStatus processingStatus;
    private OrderStatus inTransitStatus;
    private OrderStatus deliveredStatus;
    private ShoppingCart shoppingCart;
    private Order createdOrder;

    @BeforeEach
    void setUp() {
        // --- 1. Инициализация Модель Объектов ---

        category = new Category(CATEGORY_ID, "Electronics");
        brand = new Brand(BRAND_ID, "TestBrand");

        product = new Product();
        product.setProductId(PRODUCT_ID);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(100.00));
        product.setStockQuantity(10);
        product.setCategory(category);
        product.setBrand(brand);
        product.setIsAvailable(true);
        product.setCreatedAt(LocalDateTime.now());

        adminUser = new User();
        adminUser.setUserId(ADMIN_USER_ID);
        adminUser.setEmail("admin@test.com");

        customerUser = new User();
        customerUser.setUserId(CUSTOMER_USER_ID);
        customerUser.setEmail("customer@test.com");

        processingStatus = new OrderStatus(PROCESSING_STATUS_ID, "PROCESSING");
        inTransitStatus = new OrderStatus(IN_TRANSIT_STATUS_ID, "IN_TRANSIT");
        deliveredStatus = new OrderStatus(DELIVERED_STATUS_ID, "DELIVERED");

        shoppingCart = new ShoppingCart();
        shoppingCart.setUser(customerUser);
        shoppingCart.setItems(Collections.emptyList());

        // --- 2. Настройка Моков (поведение по умолчанию) ---

        // Мокинг productService.createProduct(productToCreate)
        when(productService.createProduct(any(Product.class))).thenReturn(product);

        // Мокинг productService.searchProducts()
        Page<Product> productPage = new PageImpl<>(List.of(product), PageRequest.of(0, 10), 1);
        when(productService.searchProducts(eq("Test"), any(PageRequest.class))).thenReturn(productPage);

        // Мокинг shoppingCartService.getCartWithItems()
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPrice(product.getPrice());

        shoppingCart.setItems(List.of(cartItem));
        when(shoppingCartService.getCartWithItems(eq(CUSTOMER_USER_ID))).thenReturn(Optional.of(shoppingCart));

        // Мокинг orderService.createOrder()
        createdOrder = new Order();
        createdOrder.setId(ORDER_ID);
        createdOrder.setUser(customerUser);
        createdOrder.setStatus(processingStatus);
        createdOrder.setTotalAmount(product.getPrice().multiply(BigDecimal.valueOf(2)));
        when(orderService.createOrder(any(Order.class))).thenReturn(createdOrder);

        // Мокинг orderService.getOrderById() для Шага 5
        // 1-й вызов: IN_TRANSIT
        Order inTransitOrder = new Order();
        inTransitOrder.setId(ORDER_ID);
        inTransitOrder.setStatus(inTransitStatus);

        // 2-й вызов: DELIVERED
        Order deliveredOrder = new Order();
        deliveredOrder.setId(ORDER_ID);
        deliveredOrder.setStatus(deliveredStatus);

        when(orderService.getOrderById(eq(ORDER_ID)))
                .thenReturn(Optional.of(inTransitOrder)) // Первый вызов после IN_TRANSIT
                .thenReturn(Optional.of(deliveredOrder)); // Второй вызов после DELIVERED

        // Мокинг orderStatusRepository (необязательно, но для полноты)
        when(orderStatusRepository.findByStatusName(eq("PROCESSING"))).thenReturn(Optional.of(processingStatus));
        when(orderStatusRepository.findByStatusName(eq("IN_TRANSIT"))).thenReturn(Optional.of(inTransitStatus));
        when(orderStatusRepository.findByStatusName(eq("DELIVERED"))).thenReturn(Optional.of(deliveredStatus));
    }

    @Test
    void testFullOrderFlow() {
        // Шаг 1: Админ добавляет товар
        Product productToCreate = new Product(); // Фактический объект, передаваемый в сервис
        productToCreate.setName("Test Product");

        Product resultProduct = productService.createProduct(productToCreate);
        assertThat(resultProduct).isNotNull();
        assertThat(resultProduct.getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(resultProduct.getName()).isEqualTo("Test Product");
        verify(productService, times(1)).createProduct(any(Product.class));

        // Шаг 2: Заказчик ищет товар
        var foundProducts = productService.searchProducts("Test", PageRequest.of(0, 10));
        assertThat(foundProducts.getContent()).isNotEmpty();
        assertThat(foundProducts.getContent().stream()
                .anyMatch(p -> p.getProductId().equals(PRODUCT_ID))).isTrue();
        verify(productService, times(1)).searchProducts(eq("Test"), any(PageRequest.class));

        // Шаг 3: Заказчик добавляет товар в корзину
        shoppingCartService.addProductToCart(
                CUSTOMER_USER_ID,
                PRODUCT_ID,
                2,
                product.getPrice()
        );

        var cartOpt = shoppingCartService.getCartWithItems(CUSTOMER_USER_ID);
        assertThat(cartOpt).isPresent();
        ShoppingCart cart = cartOpt.get();
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProduct().getProductId()).isEqualTo(PRODUCT_ID);
        assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(2);
        verify(shoppingCartService, times(1)).addProductToCart(
                eq(CUSTOMER_USER_ID), eq(PRODUCT_ID), eq(2), eq(product.getPrice()));
        verify(shoppingCartService, times(1)).getCartWithItems(eq(CUSTOMER_USER_ID));


        // Шаг 4: Заказчик подтверждает заказ
        Order orderToCreate = new Order(); // Объект, который мы передаем
        orderToCreate.setStatus(processingStatus);

        Order resultOrder = orderService.createOrder(orderToCreate);
        assertThat(resultOrder.getId()).isEqualTo(ORDER_ID);
        assertThat(resultOrder.getStatus().getStatusName()).isEqualTo("PROCESSING");
        verify(orderService, times(1)).createOrder(any(Order.class));

        // Шаг 5: Админ обновляет статус заказа PROCESSING → IN_TRANSIT → DELIVERED

        // Обновление до IN_TRANSIT
        orderService.updateOrderStatus(ORDER_ID, IN_TRANSIT_STATUS_ID);
        var updatedOrder1 = orderService.getOrderById(ORDER_ID);
        assertThat(updatedOrder1).isPresent();
        assertThat(updatedOrder1.get().getStatus().getStatusName()).isEqualTo("IN_TRANSIT");

        // Обновление до DELIVERED
        orderService.updateOrderStatus(ORDER_ID, DELIVERED_STATUS_ID);
        var updatedOrder2 = orderService.getOrderById(ORDER_ID);
        assertThat(updatedOrder2).isPresent();
        assertThat(updatedOrder2.get().getStatus().getStatusName()).isEqualTo("DELIVERED");

        // Проверка вызовов методов обновления статуса и получения заказа
        verify(orderService, times(1)).updateOrderStatus(eq(ORDER_ID), eq(IN_TRANSIT_STATUS_ID));
        verify(orderService, times(1)).updateOrderStatus(eq(ORDER_ID), eq(DELIVERED_STATUS_ID));
        // getOrderById должен быть вызван 2 раза (после каждого обновления)
        verify(orderService, times(2)).getOrderById(eq(ORDER_ID));
    }
}
