# E-Store Repository Layer

Полный набор JPA Repository интерфейсов для e-commerce приложения.

## 📦 Бизнес-сущности (bisentity)

### UserRepository
**Сущность:** `User`  
**ID:** `UUID`

**Основные методы:**
- `findByEmail(String email)` - поиск по email
- `existsByEmail(String email)` - проверка существования email
- `findByIsActive(Boolean isActive, Pageable)` - активные/неактивные пользователи
- `findByRoleId(Integer roleId, Pageable)` - пользователи по роли
- `findByRegistrationDateBetween(...)` - фильтр по дате регистрации
- `searchByEmail(String search, Pageable)` - поиск по части email

---

### ProductRepository
**Сущность:** `Product`  
**ID:** `UUID`

**Основные методы:**
- `findByIsAvailable(Boolean, Pageable)` - доступные товары
- `findByCategoryId(Integer categoryId, Pageable)` - по категории
- `findByBrandId(Integer brandId, Pageable)` - по бренду
- `findByPriceRange(BigDecimal min, BigDecimal max, Pageable)` - по ценовому диапазону
- `searchByNameOrDescription(String search, Pageable)` - полнотекстовый поиск
- `findByMinRating(BigDecimal minRating, Pageable)` - по рейтингу
- `findInStock(Pageable)` - в наличии
- `findLowStock(Integer threshold, Pageable)` - товары с низким остатком
- `findTopRated(Pageable)` - лучшие по рейтингу
- `findNewest(Pageable)` - новинки

---

### OrderRepository
**Сущность:** `Order`  
**ID:** `UUID`

**Основные методы:**
- `findByUserId(UUID userId, Pageable)` - заказы пользователя
- `findByStatus(String status, Pageable)` - по статусу
- `findByUserIdAndStatus(UUID, String, Pageable)` - по пользователю и статусу
- `findByOrderDateBetween(...)` - по периоду
- `findByTotalAmountGreaterThanEqual(BigDecimal, Pageable)` - по сумме
- `findByShippingCityId(Integer, Pageable)` - по городу доставки
- `findByDeliveryMethodId(Integer, Pageable)` - по методу доставки
- `findByPaymentMethodId(Integer, Pageable)` - по методу оплаты
- `countByUserId(UUID userId)` - количество заказов пользователя
- `sumTotalAmountByUserId(UUID userId)` - общая сумма заказов
- `findByStatusIn(List<String> statuses, Pageable)` - по списку статусов

---

### CustomerProfileRepository
**Сущность:** `CustomerProfile`  
**ID:** `UUID`

**Основные методы:**
- `findByPhoneNumber(String phoneNumber)` - по телефону
- `findByCityId(Integer cityId, Pageable)` - по городу
- `findByTotalSpentGreaterThanEqual(BigDecimal, Pageable)` - по сумме покупок
- `findByOrdersCountGreaterThanEqual(Integer, Pageable)` - по количеству заказов
- `searchByName(String search, Pageable)` - поиск по имени/фамилии
- `findTopSpenders(Pageable)` - ТОП по тратам
- `findMostActiveCustomers(Pageable)` - самые активные

---

### AdminProfileRepository
**Сущность:** `AdminProfile`  
**ID:** `UUID`

**Основные методы:**
- `findByDepartmentId(Integer, Pageable)` - по отделу
- `findByDepartmentName(String, Pageable)` - по названию отдела
- `findByHireDateBetween(...)` - по дате найма
- `searchByName(String search, Pageable)` - поиск по имени
- `findAllOrderByHireDateAsc()` - все по дате найма

---

### OrderItemRepository
**Сущность:** `OrderItem`  
**ID:** `Integer`

**Основные методы:**
- `findByOrderId(UUID orderId)` - элементы заказа
- `findByProductId(UUID productId)` - по товару
- `sumQuantityByProductId(UUID productId)` - общее количество проданных единиц
- `findByOrderIdAndProductId(UUID, UUID)` - конкретный элемент
- `findTopSellingProducts()` - топ продаж

---

### ShoppingCartRepository
**Сущность:** `ShoppingCart`  
**ID:** `UUID`

**Основные методы:**
- `findByUserId(UUID userId)` - корзина пользователя
- `existsByUserId(UUID userId)` - проверка наличия корзины
- `findInactiveCarts(LocalDateTime date)` - неактивные корзины
- `countNonEmptyCarts()` - количество непустых корзин
- `findByUserIdWithItems(UUID userId)` - с загрузкой товаров (eager)
- `findByIdWithItems(UUID cartId)` - по ID с товарами
- `deleteEmptyCartsOlderThan(LocalDateTime date)` - очистка старых пустых корзин

---

### CartItemRepository
**Сущность:** `CartItem`  
**ID:** `Integer`

**Основные методы:**
- `findByCartId(UUID cartId)` - все элементы корзины
- `findByCartIdAndProductId(UUID, UUID)` - конкретный элемент
- `findByProductId(UUID productId)` - товар во всех корзинах
- `countByCartId(UUID cartId)` - количество позиций
- `calculateCartTotal(UUID cartId)` - общая сумма корзины
- `sumQuantityByCartId(UUID cartId)` - общее количество товаров
- `deleteByCartId(UUID cartId)` - очистка корзины
- `deleteByCartIdAndProductId(UUID, UUID)` - удаление товара
- `existsByCartIdAndProductId(UUID, UUID)` - проверка наличия
- `findMostAddedToCartProducts()` - популярные товары в корзинах

---

### CityRouteRepository
**Сущность:** `CityRoute`  
**ID:** `Integer`

**Основные методы:**
- `findByCityId(Integer cityId)` - все маршруты города
- `findByCityAAndCityB(Integer, Integer)` - прямой маршрут
- `findByMaxDistance(BigDecimal maxDistance)` - по максимальному расстоянию
- `findByCityName(String cityName)` - по названию города
- `findDirectRoutesFromCity(Integer cityId)` - исходящие маршруты
- `findDirectRoutesToCity(Integer cityId)` - входящие маршруты
- `existsDirectRoute(Integer cityAId, Integer cityBId)` - проверка прямого маршрута

**Специальные методы (BFS):**
- `findAllRoutesBFS(String startCityName)` - все маршруты из города (BFS алгоритм)
  - Возвращает: `[city_name, transfers, total_distance, path_name]`
- `findShortestRouteBFS(String startCityName, String endCityName)` - кратчайший маршрут
  - Использует рекурсивный CTE для поиска оптимального пути

---

## 📚 Справочники (dicts)

### RoleRepository
**Сущность:** `Role`  
**ID:** `Integer`

**Методы:**
- `findByRoleName(String roleName)`
- `existsByRoleName(String roleName)`

---

### CategoryRepository
**Сущность:** `Category`  
**ID:** `Integer`

**Методы:**
- `findByCategoryName(String categoryName)`
- `existsByCategoryName(String categoryName)`
- `searchByCategoryName(String search)`
- `findAllOrderByCategoryNameAsc()`

---

### BrandRepository
**Сущность:** `Brand`  
**ID:** `Integer`

**Методы:**
- `findByBrandName(String brandName)`
- `existsByBrandName(String brandName)`
- `searchByBrandName(String search)`
- `findAllOrderByBrandNameAsc()`

---

### CityRepository
**Сущность:** `City`  
**ID:** `Integer`

**Методы:**
- `findByCityName(String cityName)`
- `existsByCityName(String cityName)`
- `searchByCityName(String search)`
- `findAllOrderByCityNameAsc()`

---

### DeliveryMethodRepository
**Сущность:** `DeliveryMethod`  
**ID:** `Integer`

**Методы:**
- `findByMethodName(String methodName)`
- `existsByMethodName(String methodName)`
- `findAllOrderByMethodNameAsc()`

---

### PaymentMethodRepository
**Сущность:** `PaymentMethod`  
**ID:** `Integer`

**Методы:**
- `findByMethodName(String methodName)`
- `existsByMethodName(String methodName)`
- `findAllOrderByMethodNameAsc()`

---

### AdminDepartmentRepository
**Сущность:** `AdminDepartment`  
**ID:** `Integer`

**Методы:**
- `findByDepartmentName(String departmentName)`
- `existsByDepartmentName(String departmentName)`
- `findAllOrderByDepartmentNameAsc()`

---

### ProductImageRepository
**Сущность:** `ProductImage`  
**ID:** `Integer`

**Методы:**
- `findByProductIdOrderBySortOrder(UUID productId)` - изображения по порядку
- `findByProductId(UUID productId)` - все изображения продукта
- `deleteByProductId(UUID productId)` - удаление всех изображений (требует @Transactional)
- `countByProductId(UUID productId)` - количество изображений

---

## 🎯 Особенности

### Пагинация
Все основные репозитории (Product, Order, User, etc.) возвращают `Page<T>` для поддержки пагинации в web-приложении.

### JPQL запросы
Используются кастомные JPQL запросы с `@Query` для:
- Сложных фильтраций
- JOIN операций
- Агрегаций (COUNT, SUM)
- Поиска по связанным сущностям

### Native SQL
Используется для:
- Рекурсивных CTE (BFS поиск маршрутов)
- Специфичных PostgreSQL функций (ARRAY)

### Модифицирующие запросы
Методы с `@Modifying` требуют аннотации `@Transactional` на уровне сервиса:
- `CartItemRepository.deleteByCartId()`
- `CartItemRepository.deleteByCartIdAndProductId()`
- `ProductImageRepository.deleteByProductId()`
- `ShoppingCartRepository.deleteEmptyCartsOlderThan()`

---

## 📝 Примеры использования

### Пагинация продуктов
```java
Pageable pageable = PageRequest.of(0, 20, Sort.by("price").ascending());
Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
```

### Поиск маршрутов (BFS)
```java
List<Object[]> routes = cityRouteRepository.findAllRoutesBFS("Москва");
// routes[i][0] = city_name (String)
// routes[i][1] = transfers (Integer)
// routes[i][2] = total_distance (BigDecimal)
// routes[i][3] = path_name (String)
```

### Расчет суммы корзины
```java
BigDecimal total = cartItemRepository.calculateCartTotal(cartId);
```

### Статистика заказов пользователя
```java
Long orderCount = orderRepository.countByUserId(userId);
BigDecimal totalSpent = orderRepository.sumTotalAmountByUserId(userId);
```

---

## ✅ Checklist создания

- ✅ **13 репозиториев** для бизнес-сущностей
- ✅ **8 репозиториев** для справочников
- ✅ Поддержка **пагинации** (Page<T>)
- ✅ **JPQL** и **Native SQL** запросы
- ✅ **BFS алгоритм** для поиска маршрутов
- ✅ Методы **агрегации** (COUNT, SUM)
- ✅ **Eager loading** опции (JOIN FETCH)
- ✅ **Модифицирующие** запросы (@Modifying)

---

**Версия:** 1.0  
**Дата создания:** 2025-11-12  
**Spring Boot:** 3.x  
**JPA/Hibernate:** Compatible
