# Admin REST API Controllers

Полная документация по REST API для админ-панели интернет-магазина с разделением прав доступа по отделам.

## Структура отделов администрации

### 1. ANALYZE - Отдел аналитики
**Права**: только чтение, просмотр отчетов и статистики

### 2. USER_MANAGE - Отдел управления пользователями
**Права**: управление пользователями, активация/деактивация, просмотр профилей

### 3. PRODUCT_MANAGE - Отдел управления товарами
**Права**: CRUD товаров, категорий, брендов, складов, городов, маршрутов, изображений

### 4. ORDER_MANAGE - Отдел управления заказами
**Права**: просмотр и управление заказами, изменение статусов, отмены

---

## 1. AuthController
**Base URL**: `/api/admin/auth`

### Endpoints:

#### POST `/login`
Авторизация администратора с получением прав доступа
```json
Request:
{
  "email": "admin@example.com",
  "password": "password123"
}

Response:
{
  "success": true,
  "userId": "uuid",
  "email": "admin@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "department": "ANALYZE",
  "availableEndpoints": ["/api/admin/analytics/sales", ...],
  "permissions": {
    "hasAnalytics": true,
    "hasOrderManagement": false,
    "hasProductManagement": false,
    "hasUserManagement": false
  }
}
```

#### GET `/check-permission`
Проверка конкретного права доступа
- Query: `userId`, `permission`

---

## 2. AnalyticsController
**Base URL**: `/api/admin/analytics`  
**Доступ**: ANALYZE

### Endpoints:

#### GET `/sales`
Статистика продаж за период
- Query: `adminUserId`, `startDate` (optional), `endDate` (optional)
- Response: общая выручка, количество заказов, средний чек

#### GET `/products`
Аналитика по товарам
- Query: `adminUserId`
- Response: топ-рейтинг, низкие остатки, новинки, топ продаж

#### GET `/customers`
Аналитика по клиентам
- Query: `adminUserId`
- Response: топ покупателей, самые активные

#### GET `/orders`
Статистика по заказам
- Query: `adminUserId`
- Response: распределение по статусам

#### GET `/dashboard`
Общая сводка (dashboard)
- Query: `adminUserId`
- Response: ключевые метрики за последние 30 дней

---

## 3. ProductManagementController
**Base URL**: `/api/admin/products`  
**Доступ**: PRODUCT_MANAGE

### Основные операции с товарами:

#### GET `/`
Получить все товары с пагинацией
- Query: `adminUserId`, `page`, `size`

#### POST `/`
Создать товар
- Query: `adminUserId`
- Body: `Product` JSON

#### PUT `/{productId}`
Обновить товар
- Query: `adminUserId`
- Body: `Product` JSON

#### DELETE `/{productId}`
Удалить товар
- Query: `adminUserId`

#### PUT `/{productId}/stock`
Обновить остатки товара
- Query: `adminUserId`, `quantity`

#### GET `/low-stock`
Товары с низкими остатками
- Query: `adminUserId`, `threshold` (default: 10)

### Категории:

#### GET `/categories`
Получить все категории

#### POST `/categories`
Создать категорию

#### PUT `/categories/{categoryId}`
Обновить категорию

#### DELETE `/categories/{categoryId}`
Удалить категорию

#### GET `/categories/search`
Поиск категорий
- Query: `query`

### Бренды:

#### GET `/brands`
Получить все бренды

#### POST `/brands`
Создать бренд

#### PUT `/brands/{brandId}`
Обновить бренд

#### DELETE `/brands/{brandId}`
Удалить бренд

#### GET `/brands/search`
Поиск брендов
- Query: `query`

### Изображения товаров:

#### GET `/{productId}/images`
Получить изображения товара

#### POST `/{productId}/images`
Добавить изображение
```json
{
  "imageUrl": "https://...",
  "sortOrder": 1
}
```

#### DELETE `/images/{imageId}`
Удалить изображение

#### PUT `/images/{imageId}/sort`
Изменить порядок изображения
- Query: `sortOrder`

---

## 4. OrderManagementController
**Base URL**: `/api/admin/orders`  
**Доступ**: ORDER_MANAGE

### Endpoints:

#### GET `/`
Все заказы с пагинацией (сортировка по дате)
- Query: `adminUserId`, `page`, `size`

#### GET `/{orderId}`
Детали заказа с позициями
- Query: `adminUserId`

#### PUT `/{orderId}/status`
Изменить статус заказа
- Query: `adminUserId`, `status`
- Валидные статусы: PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

#### PUT `/{orderId}/cancel`
Отменить заказ
- Query: `adminUserId`

#### GET `/status/{status}`
Фильтр по статусу
- Query: `adminUserId`, `page`, `size`

#### GET `/period`
Заказы за период
- Query: `adminUserId`, `startDate`, `endDate`, `page`, `size`

#### GET `/pending`
Заказы, ожидающие обработки
- Query: `adminUserId`

---

## 5. UserManagementController
**Base URL**: `/api/admin/users`  
**Доступ**: USER_MANAGE

### Endpoints:

#### GET `/`
Все пользователи с пагинацией
- Query: `adminUserId`, `page`, `size`

#### GET `/{userId}`
Пользователь по ID
- Query: `adminUserId`

#### PUT `/{userId}/activate`
Активировать пользователя
- Query: `adminUserId`

#### PUT `/{userId}/deactivate`
Деактивировать пользователя
- Query: `adminUserId`

#### GET `/active`
Фильтр активных/неактивных
- Query: `adminUserId`, `isActive`, `page`, `size`

#### GET `/role/{roleName}`
Пользователи по роли
- Query: `adminUserId`, `page`, `size`

#### GET `/search`
Поиск по email
- Query: `adminUserId`, `email`, `page`, `size`

#### GET `/customers`
Профили клиентов
- Query: `adminUserId`, `page`, `size`

#### GET `/roles`
Все роли
- Query: `adminUserId`

---

## 6. WarehouseController
**Base URL**: `/api/admin/warehouses`  
**Доступ**: PRODUCT_MANAGE + ORDER_MANAGE (для логистики)

### CRUD операции:

#### GET `/`
Все склады
- Query: `adminUserId`

#### GET `/{warehouseId}`
Склад по ID

#### POST `/`
Создать склад (только PRODUCT_MANAGE)
- Body: `Warehouse` JSON

#### PUT `/{warehouseId}`
Обновить склад (только PRODUCT_MANAGE)

#### DELETE `/{warehouseId}`
Удалить склад (только PRODUCT_MANAGE)

#### GET `/search`
Поиск складов
- Query: `adminUserId`, `query`

#### GET `/city/{cityId}`
Склады по городу

### Логистические операции:

#### GET `/nearest/{cityId}`
Ближайший склад к городу

#### GET `/within-distance`
Склады в пределах расстояния
- Query: `adminUserId`, `cityId`, `maxDistance`

#### GET `/route`
Маршрут между складами
- Query: `adminUserId`, `fromWarehouseId`, `toWarehouseId`

#### GET `/optimal-delivery/{cityId}`
Оптимальный склад для доставки

#### GET `/delivery-cost`
Расчет стоимости доставки
- Query: `adminUserId`, `fromWarehouseId`, `toWarehouseId`, `pricePerKm` (default: 10.0)

#### GET `/{warehouseId}/reachable`
Достижимые склады

---

## 7. CityRouteController
**Base URL**: `/api/admin/cities`  
**Доступ**: PRODUCT_MANAGE + ORDER_MANAGE

### Управление городами:

#### GET `/`
Все города
- Query: `adminUserId`

#### POST `/`
Создать город (только PRODUCT_MANAGE)
- Body: `City` JSON

#### PUT `/{cityId}`
Обновить город (только PRODUCT_MANAGE)

#### DELETE `/{cityId}`
Удалить город (только PRODUCT_MANAGE)

#### GET `/search`
Поиск городов
- Query: `adminUserId`, `query`

### Управление маршрутами:

#### GET `/routes`
Все маршруты

#### POST `/routes`
Создать маршрут (только PRODUCT_MANAGE)
- Body: `CityRoute` JSON

#### PUT `/routes/{routeId}`
Обновить маршрут (только PRODUCT_MANAGE)

#### DELETE `/routes/{routeId}`
Удалить маршрут (только PRODUCT_MANAGE)

#### GET `/routes/direct`
Прямые маршруты между городами
- Query: `adminUserId`, `cityAId`, `cityBId`

#### GET `/{cityId}/routes/from`
Маршруты из города

#### GET `/{cityId}/routes/to`
Маршруты в город

### BFS алгоритмы поиска маршрутов:

#### GET `/routes/bfs/all`
Все маршруты из города (BFS)
- Query: `adminUserId`, `startCityName`
```json
Response:
[
  {
    "destinationCity": "Moscow",
    "totalDistance": 500.0,
    "numberOfStops": 2,
    "path": "Kiev -> Minsk -> Moscow"
  }
]
```

#### GET `/routes/bfs/shortest`
Кратчайший маршрут между городами (BFS)
- Query: `adminUserId`, `startCityName`, `endCityName`
```json
Response:
{
  "found": true,
  "destinationCity": "Moscow",
  "totalDistance": 500.0,
  "numberOfStops": 2,
  "path": "Kiev -> Minsk -> Moscow"
}
```

#### GET `/routes/within-distance`
Маршруты в пределах расстояния
- Query: `adminUserId`, `maxDistance`

---

## Общие правила

### Авторизация
Все эндпоинты требуют параметр `adminUserId` для проверки прав доступа.

### Коды ответов
- **200 OK** - успешный запрос
- **201 Created** - ресурс создан
- **400 Bad Request** - ошибка валидации
- **403 Forbidden** - нет прав доступа
- **404 Not Found** - ресурс не найден
- **500 Internal Server Error** - серверная ошибка

### Формат ошибок
```json
{
  "error": "Access denied. PRODUCT_MANAGE department required"
}
```

### Формат успешных ответов
```json
{
  "success": true,
  "message": "Operation completed",
  "data": { ... }
}
```

---

## Бизнес-логика и возможности

### 1. Аналитика и отчетность (ANALYZE)
- Отчеты по продажам за произвольный период
- Анализ товарного ассортимента
- Топ покупателей и активных клиентов
- Распределение заказов по статусам
- Сводный dashboard с ключевыми метриками

### 2. Управление товарами (PRODUCT_MANAGE)
- Полный CRUD товаров с пагинацией
- Управление остатками товаров
- Мониторинг низких остатков (low-stock alerts)
- Управление категориями и брендами
- Управление изображениями товаров с сортировкой
- Поиск по категориям и брендам

### 3. Логистика (PRODUCT_MANAGE + ORDER_MANAGE)
- Управление складами
- Поиск ближайших складов к городу
- Оптимальные маршруты доставки между складами
- Расчет стоимости доставки
- Управление городами и маршрутами
- **BFS алгоритм** для поиска кратчайших путей между городами
- Анализ достижимости складов

### 4. Управление заказами (ORDER_MANAGE)
- Просмотр всех заказов с пагинацией
- Изменение статусов заказов
- Отмена заказов с валидацией
- Фильтрация по статусам и периодам
- Мониторинг pending заказов

### 5. Управление пользователями (USER_MANAGE)
- Просмотр всех пользователей
- Активация/деактивация аккаунтов
- Фильтрация по ролям и статусу
- Поиск по email
- Управление профилями клиентов

---

## Примеры использования

### Авторизация администратора
```bash
curl -X POST http://localhost:8080/api/admin/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@store.com",
    "password": "admin123"
  }'
```

### Получение статистики продаж
```bash
curl -X GET "http://localhost:8080/api/admin/analytics/sales?adminUserId=uuid&startDate=2024-01-01T00:00:00&endDate=2024-12-31T23:59:59"
```

### Создание товара
```bash
curl -X POST "http://localhost:8080/api/admin/products?adminUserId=uuid" \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Laptop",
    "price": 999.99,
    "stockQuantity": 50,
    "category": {"categoryId": 1},
    "brand": {"brandId": 1}
  }'
```

### Изменение статуса заказа
```bash
curl -X PUT "http://localhost:8080/api/admin/orders/uuid/status?adminUserId=uuid&status=SHIPPED"
```

### Поиск кратчайшего маршрута (BFS)
```bash
curl -X GET "http://localhost:8080/api/admin/cities/routes/bfs/shortest?adminUserId=uuid&startCityName=Moscow&endCityName=Paris"
```

---

## Технологический стек

- **Spring Boot** - основной framework
- **Spring Data JPA** - работа с БД
- **PostgreSQL** - СУБД
- **Lombok** - упрощение кода
- **REST API** - архитектура
- **CORS** - поддержка cross-origin запросов

## Безопасность

- Все эндпоинты проверяют права доступа через `AdminProfileService`
- Разделение прав по отделам на уровне бизнес-логики
- Валидация входных данных
- Проверка существования ресурсов перед операциями
- Обработка исключений с корректными HTTP кодами
