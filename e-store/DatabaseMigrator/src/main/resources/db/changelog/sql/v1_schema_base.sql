
-- Активация расширения для генерации UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ===================================
-- 1. СПРАВОЧНЫЕ ТАБЛИЦЫ
-- ===================================
-- Города
CREATE TABLE city (
                      city_id SERIAL PRIMARY KEY,
                      city_name VARCHAR(100) UNIQUE NOT NULL
);

-- Роли пользователей
CREATE TABLE role (
                      role_id SERIAL PRIMARY KEY,
                      role_name VARCHAR(50) UNIQUE NOT NULL
);
INSERT INTO role (role_name) VALUES
                                 ('ROLE_CUSTOMER'), ('ROLE_ADMIN')
    ON CONFLICT (role_name) DO NOTHING;

-- Категории продуктов
CREATE TABLE category (
                          category_id SERIAL PRIMARY KEY,
                          category_name VARCHAR(100) UNIQUE NOT NULL
);

-- Бренды продуктов
CREATE TABLE brand (
                       brand_id SERIAL PRIMARY KEY,
                       brand_name VARCHAR(100) UNIQUE NOT NULL
);

-- Отделы администраторов
CREATE TABLE admin_department (
                                  department_id SERIAL PRIMARY KEY,
                                  department_name VARCHAR(50) UNIQUE NOT NULL
);
INSERT INTO admin_department (department_name) VALUES
                                                   ('ANALYZE'), ('USER_MANAGE'), ('PRODUCT_MANAGE'), ('ORDER_MANAGE')
    ON CONFLICT (department_name) DO NOTHING;

-- Способы доставки
CREATE TABLE delivery_method (
                                 method_id SERIAL PRIMARY KEY,
                                 method_name VARCHAR(50) UNIQUE NOT NULL,
                                 description TEXT
);
INSERT INTO delivery_method (method_name, description) VALUES
                                                           ('Self-Pickup', 'Получение заказа в пункте выдачи.'),
                                                           ('Standard Delivery', 'Стандартная доставка курьером.'),
                                                           ('Express Delivery', 'Ускоренная доставка курьером.')
    ON CONFLICT (method_name) DO NOTHING;

-- Способы оплаты
CREATE TABLE payment_method (
                                method_id SERIAL PRIMARY KEY,
                                method_name VARCHAR(50) UNIQUE NOT NULL,
                                description TEXT
);
INSERT INTO payment_method (method_name, description) VALUES
                                                          ('Card Online', 'Оплата банковской картой на сайте.'),
                                                          ('Cash on Delivery', 'Оплата наличными при получении.'),
                                                          ('Bank Transfer', 'Оплата банковским переводом.')
    ON CONFLICT (method_name) DO NOTHING;

-- Склады (привязаны к городу)
CREATE TABLE warehouse (
                           warehouse_id SERIAL PRIMARY KEY,
                           warehouse_name VARCHAR(100) NOT NULL,
                           city_id INT REFERENCES city(city_id) NOT NULL,
                           address TEXT,
                           UNIQUE (warehouse_name, city_id)
);

-- Справочник статусов заказов
CREATE TABLE order_status (
                              status_id SERIAL PRIMARY KEY,
                              status_name VARCHAR(50) UNIQUE NOT NULL CHECK (status_name IN ('PROCESSING', 'IN_TRANSIT', 'DELIVERED'))
);
INSERT INTO order_status (status_name) VALUES
                                           ('PROCESSING'), ('IN_TRANSIT'), ('DELIVERED')
    ON CONFLICT (status_name) DO NOTHING;

-- ===================================
-- 2. ПОЛЬЗОВАТЕЛИ И ПРОФИЛИ
-- ===================================
CREATE TABLE "user" (
                        user_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password_hash TEXT NOT NULL,
                        registration_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                        last_login TIMESTAMP WITHOUT TIME ZONE,
                        is_active BOOLEAN DEFAULT TRUE,
                        role_id INT REFERENCES role(role_id) NOT NULL
);

-- Профиль клиента (адрес денормализован для простоты)
CREATE TABLE customer_profile (
                                  user_id UUID PRIMARY KEY REFERENCES "user"(user_id) ON DELETE CASCADE,
                                  first_name VARCHAR(50) NOT NULL,
                                  last_name VARCHAR(50) NOT NULL,
                                  phone_number VARCHAR(20),
                                  total_spent NUMERIC(15, 2) DEFAULT 0.00,
                                  orders_count INT DEFAULT 0,
                                  city_id INT REFERENCES city(city_id),
                                  address_text TEXT,  -- простой текстовый адрес
                                  profile_picture_url TEXT,
                                  date_of_birth DATE
);

-- Профиль администратора
CREATE TABLE admin_profile (
                               user_id UUID PRIMARY KEY REFERENCES "user"(user_id) ON DELETE CASCADE,
                               first_name VARCHAR(50) NOT NULL,
                               last_name VARCHAR(50) NOT NULL,
                               hire_date DATE DEFAULT CURRENT_DATE,
                               department_id INT REFERENCES admin_department(department_id) NOT NULL,
                               profile_picture_url TEXT
);

-- ===================================
-- 3. ТОВАРЫ (без отслеживания количества)
-- ===================================
CREATE TABLE product (
                         product_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
                         price NUMERIC(10, 2) NOT NULL CHECK (price > 0),
                         category_id INT REFERENCES category(category_id),
                         brand_id INT REFERENCES brand(brand_id),
                         main_image_url TEXT,
                         average_rating NUMERIC(2, 1) DEFAULT 0.0,
                         ratings_count INT DEFAULT 0,
                         is_available BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Изображения товаров
CREATE TABLE product_image (
                               image_id SERIAL PRIMARY KEY,
                               product_id UUID REFERENCES product(product_id) ON DELETE CASCADE NOT NULL,
                               image_url TEXT NOT NULL,
                               sort_order INT DEFAULT 0,
                               CONSTRAINT uc_product_image UNIQUE (product_id, image_url)
);

-- ===================================
-- 4. КОРЗИНА И ЗАКАЗЫ
-- ===================================
CREATE TABLE shopping_cart (
                               cart_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                               user_id UUID REFERENCES "user"(user_id) ON DELETE CASCADE UNIQUE NOT NULL,
                               created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE cart_item (
                           cart_item_id SERIAL PRIMARY KEY,
                           cart_id UUID REFERENCES shopping_cart(cart_id) ON DELETE CASCADE NOT NULL,
                           product_id UUID REFERENCES product(product_id) ON DELETE CASCADE NOT NULL,
                           quantity INT NOT NULL CHECK (quantity > 0),
                           unit_price NUMERIC(10, 2) NOT NULL,
                           CONSTRAINT uc_cart_product UNIQUE (cart_id, product_id)
);

-- Заказ (адрес денормализован для простоты, добавлено поле status)
CREATE TABLE "order" (
                         order_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         user_id UUID REFERENCES "user"(user_id) NOT NULL,
                         order_date TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
                         status_id INT NOT NULL DEFAULT 1 REFERENCES order_status(status_id),
                         total_amount NUMERIC(15, 2) NOT NULL CHECK (total_amount >= 0),
                         shipping_city_id INT REFERENCES city(city_id),
                         shipping_address_text TEXT,  -- простой текстовый адрес
                         delivery_method_id INT REFERENCES delivery_method(method_id),
                         payment_method_id INT REFERENCES payment_method(method_id),
                         discount_applied NUMERIC(5, 2) DEFAULT 0.00,
                         actual_delivery_date DATE,
                         source_warehouse_id INT REFERENCES warehouse(warehouse_id)  -- для BFS
);
CREATE TABLE order_item (
                            order_item_id SERIAL PRIMARY KEY,
                            order_id UUID REFERENCES "order"(order_id) ON DELETE CASCADE NOT NULL,
                            product_id UUID REFERENCES product(product_id) NOT NULL,
                            quantity INT NOT NULL CHECK (quantity > 0),
                            unit_price NUMERIC(10, 2) NOT NULL,
                            CONSTRAINT uc_order_product UNIQUE (order_id, product_id)
);

-- ===================================
-- 5. ГРАФ МАРШРУТОВ (для BFS)
-- ===================================
CREATE TABLE city_route (
                            route_id SERIAL PRIMARY KEY,
                            city_a_id INT REFERENCES city(city_id) NOT NULL,
                            city_b_id INT REFERENCES city(city_id) NOT NULL,
                            distance_km NUMERIC(10, 2) NOT NULL CHECK (distance_km > 0),
                            CONSTRAINT chk_city_order CHECK (city_a_id < city_b_id),
                            UNIQUE (city_a_id, city_b_id)
);
