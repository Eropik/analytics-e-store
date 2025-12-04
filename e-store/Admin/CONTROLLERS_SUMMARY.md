# Summary: Admin Controllers

## Созданные контроллеры

### 1. **AuthController** - Авторизация
- **Путь**: `/api/admin/auth`
- **Файл**: `AuthController.java`
- **Функционал**:
  - Авторизация администратора с получением прав доступа
  - Проверка конкретных permissions
  - Возврат доступных endpoints по отделу

---

### 2. **AnalyticsController** - Аналитика (ANALYZE)
- **Путь**: `/api/admin/analytics`
- **Файл**: `AnalyticsController.java`
- **Отдел**: ANALYZE
- **Функционал**:
  - Статистика продаж (выручка, средний чек, количество заказов)
  - Аналитика по товарам (топ-рейтинг, низкие остатки, новинки, топ продаж)
  - Аналитика по клиентам (топ покупатели, активные пользователи)
  - Статистика по заказам (распределение по статусам)
  - Общий dashboard

---

### 3. **ProductManagementController** - Управление товарами (PRODUCT_MANAGE)
- **Путь**: `/api/admin/products`
- **Файл**: `ProductManagementController.java`
- **Отдел**: PRODUCT_MANAGE
- **Функционал**:
  - **Товары**: CRUD, обновление остатков, поиск товаров с низкими остатками
  - **Категории**: CRUD, поиск категорий
  - **Бренды**: CRUD, поиск брендов
  - **Изображения товаров**: добавление, удаление, изменение порядка сортировки

---

### 4. **OrderManagementController** - Управление заказами (ORDER_MANAGE)
- **Путь**: `/api/admin/orders`
- **Файл**: `OrderManagementController.java`
- **Отдел**: ORDER_MANAGE
- **Функционал**:
  - Просмотр всех заказов с пагинацией
  - Детали заказа с позициями
  - Изменение статусов заказов (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
  - Отмена заказов
  - Фильтрация по статусу и периоду
  - Мониторинг pending заказов

---

### 5. **UserManagementController** - Управление пользователями (USER_MANAGE)
- **Путь**: `/api/admin/users`
- **Файл**: `UserManagementController.java`
- **Отдел**: USER_MANAGE
- **Функционал**:
  - Просмотр всех пользователей с пагинацией
  - Активация/деактивация пользователей
  - Фильтрация по активности и ролям
  - Поиск пользователей по email
  - Просмотр профилей клиентов
  - Получение списка ролей

---

### 6. **WarehouseController** - Управление складами и логистика (PRODUCT_MANAGE + ORDER_MANAGE)
- **Путь**: `/api/admin/warehouses`
- **Файл**: `WarehouseController.java`
- **Отделы**: PRODUCT_MANAGE (CRUD) + ORDER_MANAGE (логистика)
- **Функционал**:
  - **CRUD складов**: создание, редактирование, удаление (только PRODUCT_MANAGE)
  - **Поиск**: по названию, по городу
  - **Логистика**:
    - Поиск ближайшего склада к городу
    - Поиск складов в пределах расстояния от города
    - Оптимальный маршрут между складами
    - Расчет стоимости доставки между складами
    - Поиск достижимых складов
    - Оптимальный склад для доставки в город

---

### 7. **CityRouteController** - Управление городами и маршрутами (PRODUCT_MANAGE + ORDER_MANAGE)
- **Путь**: `/api/admin/cities`
- **Файл**: `CityRouteController.java`
- **Отделы**: PRODUCT_MANAGE (CRUD) + ORDER_MANAGE (просмотр)
- **Функционал**:
  - **CRUD городов**: создание, редактирование, удаление (только PRODUCT_MANAGE)
  - **Поиск городов**
  - **CRUD маршрутов между городами**: создание, редактирование, удаление (только PRODUCT_MANAGE)
  - **Прямые маршруты**: между двумя городами, из города, в город
  - **BFS алгоритмы**:
    - Поиск всех маршрутов из города с расстояниями и путями
    - Поиск кратчайшего маршрута между городами (оптимальный путь)
  - **Фильтрация**: маршруты в пределах расстояния

---

## Статистика

- **Всего контроллеров**: 7
- **Всего эндпоинтов**: ~80+
- **Отделы с доступом**: 4 (ANALYZE, USER_MANAGE, PRODUCT_MANAGE, ORDER_MANAGE)

## Ключевые особенности

### 1. Разделение прав доступа
- Каждый контроллер проверяет права через `AdminProfileService`
- `hasAnalyticsAccess()` - для ANALYZE
- `hasOrderManagementAccess()` - для ORDER_MANAGE
- `hasProductManagementAccess()` - для PRODUCT_MANAGE
- `hasUserManagementAccess()` - для USER_MANAGE

### 2. Комбинированный доступ
- **WarehouseController** и **CityRouteController** доступны для двух отделов:
  - PRODUCT_MANAGE - полный CRUD
  - ORDER_MANAGE - только просмотр и логистика

### 3. Расширенная бизнес-логика

#### Аналитика
- Продажи за произвольный период
- Топ товаров и клиентов
- Распределение заказов

#### Логистика
- **BFS алгоритм** для поиска оптимальных маршрутов между городами
- Поиск ближайших складов
- Расчет стоимости доставки
- Оптимизация доставки

#### Управление товарами
- Мониторинг низких остатков (low-stock alerts)
- Управление изображениями с сортировкой
- Поиск по категориям и брендам

#### Управление заказами
- Изменение статусов с валидацией
- Фильтрация по статусам и периодам
- Детальная информация с позициями заказа

### 4. Консистентность API
- Все контроллеры используют единый формат ответов
- Корректные HTTP статус-коды (200, 201, 400, 403, 404, 500)
- Обработка исключений с понятными сообщениями
- CORS настроен для всех контроллеров
- Пагинация для больших выборок данных

---

## Обновленные сервисы

### AdminProfileService
Добавлены методы:
- `hasAnalyticsAccess(UUID adminUserId)`
- `hasOrderManagementAccess(UUID adminUserId)`
- `hasProductManagementAccess(UUID adminUserId)`
- `hasUserManagementAccess(UUID adminUserId)`
- `getAvailableEndpoints(UUID adminUserId)` - возвращает список доступных эндпоинтов по отделу

### Интеграция с существующими сервисами
Контроллеры используют все созданные сервисы:
- `UserService`
- `ProductService`
- `OrderService`
- `OrderItemService`
- `CustomerProfileService`
- `AdminProfileService`
- `CategoryService`
- `BrandService`
- `WarehouseService`
- `CityService`
- `CityRouteService`
- `ProductImageService`
- `RoleService`

---

## Технологии

- **Spring Boot** - основной framework
- **Spring Data JPA** - ORM для работы с БД
- **PostgreSQL** - СУБД с BFS алгоритмами
- **Lombok** - упрощение кода
- **REST API** - архитектурный стиль
- **JSON** - формат данных

---

## Файлы

1. `AuthController.java` - авторизация
2. `AnalyticsController.java` - аналитика
3. `ProductManagementController.java` - товары, категории, бренды, изображения
4. `OrderManagementController.java` - заказы
5. `UserManagementController.java` - пользователи
6. `WarehouseController.java` - склады и логистика
7. `CityRouteController.java` - города и маршруты с BFS

**Обновленные сервисы**:
- `AdminProfileService.java` - интерфейс
- `AdminProfileServiceImpl.java` - реализация с методами проверки доступа

**Документация**:
- `README.md` - полная документация с примерами
- `CONTROLLERS_SUMMARY.md` - краткий обзор (этот файл)
