# E-Store Service Layer

Полный набор бизнес-логики для e-commerce приложения с Service и ServiceImpl реализациями.

## 📦 Структура

```
service/
├── UserService.java
├── ProductService.java
├── OrderService.java
├── ShoppingCartService.java
├── CustomerProfileService.java
├── CityRouteService.java
├── CategoryService.java
├── BrandService.java
├── CityService.java
├── RoleService.java
└── impl/
    ├── UserServiceImpl.java
    ├── ProductServiceImpl.java
    ├── OrderServiceImpl.java
    ├── ShoppingCartServiceImpl.java
    ├── CustomerProfileServiceImpl.java
    ├── CityRouteServiceImpl.java
    ├── CategoryServiceImpl.java
    ├── BrandServiceImpl.java
    ├── CityServiceImpl.java
    └── RoleServiceImpl.java
```

---

## 🔐 UserService

**Управление пользователями**

### Основные методы:
- `createUser(User user)` - создание нового пользователя
  - Валидация уникальности email
  - Автоматическая установка даты регистрации
  - Активация пользователя по умолчанию
  
- `updateUser(UUID userId, User user)` - обновление данных
  - Проверка уникальности email при изменении
  
- `getUserByEmail(String email)` - поиск по email
- `activateUser(UUID userId)` / `deactivateUser(UUID userId)` - управление статусом
- `updateLastLogin(UUID userId)` - обновление времени входа
- Пагинация по ролям, активности, дате регистрации

---

## 🛍️ ProductService

**Управление товарами**

### Основные методы:
- `createProduct(Product product)` - создание товара
  - Автоматическая установка timestamps
  - Инициализация рейтинга и доступности
  
- `updateStock(UUID productId, Integer quantity)` - обновление остатков
  - Автоматическое изменение доступности при нулевых остатках
  
- `updateRating(UUID productId, BigDecimal newRating)` - обновление рейтинга
  - Вычисление среднего рейтинга
  - Увеличение счетчика оценок
  
- `searchProducts(String search, Pageable)` - поиск по названию/описанию
- `getTopRatedProducts(Pageable)` - лучшие товары
- `getNewestProducts(Pageable)` - новинки
- `getLowStockProducts(Integer threshold, Pageable)` - низкие остатки

### Бизнес-логика:
- Фильтрация по категориям, брендам, ценам
- Управление доступностью
- Автоматический расчет рейтинга

---

## 📦 OrderService

**Управление заказами**

### Основные методы:
- `createOrder(Order order)` - создание заказа
  - Установка статуса PENDING
  - Автоматический расчет суммы
  - Применение скидок
  
- `updateOrderStatus(UUID orderId, String newStatus)` - изменение статуса
- `cancelOrder(UUID orderId)` - отмена заказа
  - Валидация: нельзя отменить доставленные/отмененные
  
- `getUserOrderCount(UUID userId)` - количество заказов
- `getUserTotalSpent(UUID userId)` - общая сумма покупок
- `calculateOrderTotal(UUID orderId)` - расчет суммы с учетом скидок

### Бизнес-логика:
- Автоматический расчет итоговой суммы
- Применение скидок в процентах
- Фильтрация по статусам, датам, методам доставки/оплаты

---

## 🛒 ShoppingCartService

**Управление корзиной покупок**

### Основные методы:
- `getOrCreateCart(UUID userId)` - получить или создать корзину
- `addProductToCart(UUID userId, UUID productId, Integer quantity, BigDecimal unitPrice)`
  - Проверка доступности товара
  - Проверка остатков на складе
  - Автоматическое обновление quantity если товар уже в корзине
  
- `updateProductQuantity(UUID userId, UUID productId, Integer quantity)`
  - Автоматическое удаление при quantity <= 0
  - Проверка остатков
  
- `removeProductFromCart(UUID userId, UUID productId)` - удаление товара
- `clearCart(UUID cartId)` - очистка корзины
- `getCartTotal(UUID cartId)` - сумма корзины
- `getCartItemsCount(UUID cartId)` - количество товаров

### Бизнес-логика:
- Проверка наличия товара на складе
- Автоматическое обновление timestamps
- Умное добавление (increment существующих или create новых)

---

## 🚗 CityRouteService

**Управление маршрутами между городами**

### Основные методы:
- `createRoute(CityRoute route)` - создание маршрута
  - Валидация: расстояние > 0
  
- `findAllRoutesBFS(String startCityName)` - поиск всех маршрутов (BFS)
  - Использует рекурсивный CTE
  - Возвращает: город, кол-во пересадок, расстояние, путь
  
- `findShortestRouteBFS(String startCityName, String endCityName)` - кратчайший путь
  - BFS алгоритм для оптимального маршрута
  
- `existsDirectRoute(Integer cityAId, Integer cityBId)` - проверка прямого маршрута
- `getDirectRoutesFromCity(Integer cityId)` - исходящие маршруты
- `getDirectRoutesToCity(Integer cityId)` - входящие маршруты

### Бизнес-логика:
- BFS поиск оптимальных маршрутов
- Учет пересадок и расстояния
- Валидация входных данных

---

## 👤 CustomerProfileService

**Управление профилями клиентов**

### Основные методы:
- `createProfile(CustomerProfile profile)` - создание профиля
  - Инициализация totalSpent = 0
  - Инициализация ordersCount = 0
  
- `updateTotalSpent(UUID userId, BigDecimal amount)` - обновление суммы покупок
  - Добавление к существующей сумме
  
- `incrementOrdersCount(UUID userId)` - увеличение счетчика заказов
- `getTopSpenders(Pageable)` - ТОП покупателей
- `getMostActiveCustomers(Pageable)` - самые активные

### Бизнес-логика:
- Автоматический подсчет статистики
- Фильтрация по городам, сумме, активности
- Поиск по имени/фамилии

---

## 📚 Справочные сервисы

### CategoryService, BrandService, CityService, RoleService

Универсальная реализация для справочников:

**Общие методы:**
- `create*(Entity entity)` - создание с валидацией уникальности
- `update*(Integer id, Entity entity)` - обновление
- `delete*(Integer id)` - удаление
- `getById(Integer id)` - получение по ID
- `getByName(String name)` - получение по названию
- `getAll*()` - получение всех (отсортированных)
- `search*(String search)` - поиск по части названия
- `existsByName(String name)` - проверка существования

**Бизнес-логика:**
- Валидация уникальности названий
- Автоматическая сортировка
- Поиск с LIKE запросами

---

## 🎯 Общие принципы

### Транзакции
- `@Transactional(readOnly = true)` на уровне класса
- `@Transactional` на методах изменения данных

### Валидация
- Проверка существования сущностей перед операциями
- Валидация бизнес-правил (email, stock, статусы)
- Throw `IllegalArgumentException` для ошибок валидации
- Throw `IllegalStateException` для нарушений бизнес-логики

### Обработка ошибок
```java
// Сущность не найдена
throw new IllegalArgumentException("Entity not found with id: " + id);

// Нарушение бизнес-правила
throw new IllegalStateException("Cannot perform action in current state");

// Конфликт данных
throw new IllegalArgumentException("Entity already exists");
```

### Null Safety
- Проверка null для Optional результатов
- Возврат пустых коллекций вместо null
- Дефолтные значения для агрегаций (BigDecimal.ZERO, 0)

---

## 📝 Примеры использования

### Создание пользователя
```java
@Autowired
private UserService userService;

User user = new User();
user.setEmail("user@example.com");
user.setPasswordHash("hashed_password");
user.setRole(role);

User created = userService.createUser(user);
// Автоматически установлены: registrationDate, isActive=true
```

### Добавление товара в корзину
```java
@Autowired
private ShoppingCartService cartService;

// Проверит наличие, создаст корзину если нужно
cartService.addProductToCart(userId, productId, 2, product.getPrice());
```

### Создание заказа с расчетом
```java
@Autowired
private OrderService orderService;

Order order = new Order();
order.setUser(user);
order.setItems(orderItems);
order.setDiscountApplied(BigDecimal.valueOf(10)); // 10%

Order created = orderService.createOrder(order);
// Автоматически: orderDate, status=PENDING, totalAmount (с учетом скидки)
```

### Поиск маршрута BFS
```java
@Autowired
private CityRouteService cityRouteService;

// Кратчайший путь между городами
Object[] route = cityRouteService.findShortestRouteBFS("Москва", "Санкт-Петербург");
String cityName = (String) route[0];
Integer transfers = (Integer) route[1];
BigDecimal distance = (BigDecimal) route[2];
String path = (String) route[3];
```

### Обновление профиля при заказе
```java
@Autowired
private CustomerProfileService profileService;
@Autowired
private OrderService orderService;

Order order = orderService.createOrder(order);
profileService.updateTotalSpent(userId, order.getTotalAmount());
profileService.incrementOrdersCount(userId);
```

---

## ✅ Checklist создания

- ✅ **10 Service интерфейсов**
- ✅ **10 ServiceImpl реализаций**
- ✅ **@Transactional** управление
- ✅ **Валидация** бизнес-правил
- ✅ **Обработка ошибок**
- ✅ **Автоматические вычисления** (рейтинги, суммы, скидки)
- ✅ **BFS алгоритм** для маршрутов
- ✅ **Пагинация** поддержка
- ✅ **Lombok** @RequiredArgsConstructor
- ✅ **Best practices** Spring Service Layer

---

**Версия:** 1.0  
**Дата создания:** 2025-11-12  
**Spring Boot:** 3.x  
**Паттерн:** Service-Repository
