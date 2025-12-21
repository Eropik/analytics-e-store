-- ===================================
-- INSERT тестовых данных
-- ===================================

-- Категории (только электроника и электроприборы)
INSERT INTO category (category_name) VALUES
('Смартфоны'),
('Планшеты'),
('Ноутбуки'),
('Телевизоры'),
('Наушники и аудио'),
('Фото и видеокамеры'),
('Игровые консоли'),
('Умная техника'),
('Бытовая техника'),
('Компьютерная периферия')
ON CONFLICT (category_name) DO NOTHING;

-- Бренды (только электроника)
INSERT INTO brand (brand_name) VALUES
('Samsung'),
('Apple'),
('Sony'),
('LG'),
('Xiaomi'),
('Huawei'),
('Canon'),
('Nikon'),
('Asus'),
('Lenovo')
ON CONFLICT (brand_name) DO NOTHING;

-- 10 городов
INSERT INTO city (city_name) VALUES
('Москва'),
('Санкт-Петербург'),
('Новосибирск'),
('Екатеринбург'),
('Казань'),
('Нижний Новгород'),
('Челябинск'),
('Самара'),
('Омск'),
('Ростов-на-Дону')
ON CONFLICT (city_name) DO NOTHING;

-- 10 складов (привязываем к городам 1-10)
INSERT INTO warehouse (warehouse_name, city_id, address) VALUES
('Склад Центральный', 1, 'ул. Ленина, д. 1'),
('Склад Северный', 2, 'пр. Невский, д. 25'),
('Склад Восточный', 3, 'ул. Красный проспект, д. 10'),
('Склад Западный', 4, 'ул. Ленина, д. 50'),
('Склад Южный', 5, 'ул. Баумана, д. 15'),
('Склад Промышленный', 6, 'ул. Покровка, д. 8'),
('Склад Торговый', 7, 'ул. Кирова, д. 30'),
('Склад Логистический', 8, 'ул. Московское шоссе, д. 5'),
('Склад Распределительный', 9, 'ул. Ленина, д. 100'),
('Склад Региональный', 10, 'пр. Буденновский, д. 20')
ON CONFLICT (warehouse_name, city_id) DO NOTHING;

-- 20 маршрутов между городами (city_a_id < city_b_id)
INSERT INTO city_route (city_a_id, city_b_id, distance_km) VALUES
(1, 2, 635.0),   -- Москва - Санкт-Петербург
(1, 3, 2810.0),  -- Москва - Новосибирск
(1, 4, 1417.0),  -- Москва - Екатеринбург
(1, 5, 815.0),   -- Москва - Казань
(1, 6, 411.0),   -- Москва - Нижний Новгород
(2, 3, 3445.0),  -- Санкт-Петербург - Новосибирск
(2, 4, 1832.0),  -- Санкт-Петербург - Екатеринбург
(2, 5, 1445.0),  -- Санкт-Петербург - Казань
(3, 4, 1393.0),  -- Новосибирск - Екатеринбург
(3, 5, 1995.0),  -- Новосибирск - Казань
(3, 6, 2399.0),  -- Новосибирск - Нижний Новгород
(4, 5, 602.0),   -- Екатеринбург - Казань
(4, 6, 1006.0),  -- Екатеринбург - Нижний Новгород
(4, 7, 200.0),   -- Екатеринбург - Челябинск
(5, 6, 404.0),   -- Казань - Нижний Новгород
(5, 7, 802.0),   -- Казань - Челябинск
(6, 7, 1206.0),  -- Нижний Новгород - Челябинск
(7, 8, 1200.0),  -- Челябинск - Самара
(8, 9, 1500.0),  -- Самара - Омск
(9, 10, 2100.0)  -- Омск - Ростов-на-Дону
ON CONFLICT (city_a_id, city_b_id) DO NOTHING;

-- 15 товаров с UUID (только электроника и электроприборы)
-- Используем подзапросы для получения category_id и brand_id по названиям
INSERT INTO product (product_id, name, description, price, category_id, brand_id, main_image_url, average_rating, ratings_count, is_available, stock_quantity) VALUES
('a1b2c3d4-e5f6-4789-a012-b3c4d5e6f789', 'Смартфон Samsung Galaxy S23', 'Флагманский смартфон с камерой 108 МП и процессором Snapdragon 8 Gen 2', 89999.00, (SELECT category_id FROM category WHERE category_name = 'Смартфоны'), (SELECT brand_id FROM brand WHERE brand_name = 'Samsung'), NULL, 4.8, 1250, true, 50),
('c3d4e5f6-a7b8-4901-c234-d5e6f7a8b901', 'iPhone 15 Pro Max', 'Премиум смартфон Apple с титановым корпусом и камерой Pro', 149999.00, (SELECT category_id FROM category WHERE category_name = 'Смартфоны'), (SELECT brand_id FROM brand WHERE brand_name = 'Apple'), NULL, 4.9, 2100, true, 30),
('a7b8c9d0-e1f2-4345-a678-b9c0d1e2f345', 'Смартфон Xiaomi 13 Pro', 'Флагманский смартфон с камерой Leica и быстрой зарядкой', 59999.00, (SELECT category_id FROM category WHERE category_name = 'Смартфоны'), (SELECT brand_id FROM brand WHERE brand_name = 'Xiaomi'), NULL, 4.6, 1120, true, 60),
('a3b4c5d6-e7f8-4901-a234-b5c6d7e8f901', 'Смартфон Huawei P60 Pro', 'Флагманский смартфон с камерой XMAGE', 79999.00, (SELECT category_id FROM category WHERE category_name = 'Смартфоны'), (SELECT brand_id FROM brand WHERE brand_name = 'Huawei'), NULL, 4.6, 650, true, 40),
('c5d6e7f8-a9b0-4123-c456-d7e8f9a0b123', 'Планшет Samsung Galaxy Tab S9', 'Планшет с экраном AMOLED и S Pen', 69999.00, (SELECT category_id FROM category WHERE category_name = 'Планшеты'), (SELECT brand_id FROM brand WHERE brand_name = 'Samsung'), NULL, 4.8, 580, true, 35),
('f1a2b3c4-d5e6-4789-f012-a3b4c5d6e789', 'iPad Pro 12.9"', 'Планшет Apple с чипом M2 и дисплеем Liquid Retina XDR', 129999.00, (SELECT category_id FROM category WHERE category_name = 'Планшеты'), (SELECT brand_id FROM brand WHERE brand_name = 'Apple'), NULL, 4.9, 890, true, 25),
('a2b3c4d5-e6f7-4890-a123-b4c5d6e7f890', 'Ноутбук Asus ROG Strix', 'Игровой ноутбук с процессором Intel Core i9 и видеокартой RTX 4080', 199999.00, (SELECT category_id FROM category WHERE category_name = 'Ноутбуки'), (SELECT brand_id FROM brand WHERE brand_name = 'Asus'), NULL, 4.7, 450, true, 20),
('b3c4d5e6-f7a8-4901-b234-c5d6e7f8a901', 'Ноутбук Lenovo ThinkPad X1', 'Бизнес-ноутбук с процессором Intel Core i7 и дисплеем 14"', 149999.00, (SELECT category_id FROM category WHERE category_name = 'Ноутбуки'), (SELECT brand_id FROM brand WHERE brand_name = 'Lenovo'), NULL, 4.6, 320, true, 30),
('f6a7b8c9-d0e1-4234-f567-a8b9c0d1e234', 'Телевизор LG OLED 55"', 'OLED телевизор с разрешением 4K и поддержкой HDR', 89999.00, (SELECT category_id FROM category WHERE category_name = 'Телевизоры'), (SELECT brand_id FROM brand WHERE brand_name = 'LG'), NULL, 4.8, 750, true, 25),
('c4d5e6f7-a8b9-4012-c345-d6e7f8a9b012', 'Телевизор Samsung QLED 65"', 'QLED телевизор с технологией Quantum HDR и Smart TV', 129999.00, (SELECT category_id FROM category WHERE category_name = 'Телевизоры'), (SELECT brand_id FROM brand WHERE brand_name = 'Samsung'), NULL, 4.8, 680, true, 20),
('e5f6a7b8-c9d0-4123-e456-f7a8b9c0d123', 'Наушники Sony WH-1000XM5', 'Беспроводные наушники с активным шумоподавлением', 34999.00, (SELECT category_id FROM category WHERE category_name = 'Наушники и аудио'), (SELECT brand_id FROM brand WHERE brand_name = 'Sony'), NULL, 4.7, 980, true, 45),
('d5e6f7a8-b9c0-4123-d456-e7f8a9b0c123', 'AirPods Pro 2', 'Беспроводные наушники Apple с активным шумоподавлением', 24999.00, (SELECT category_id FROM category WHERE category_name = 'Наушники и аудио'), (SELECT brand_id FROM brand WHERE brand_name = 'Apple'), NULL, 4.8, 1200, true, 60),
('b8c9d0e1-f2a3-4456-b789-c0d1e2f3a456', 'Фотоаппарат Canon EOS R6', 'Беззеркальная камера с полнокадровым сенсором', 199999.00, (SELECT category_id FROM category WHERE category_name = 'Фото и видеокамеры'), (SELECT brand_id FROM brand WHERE brand_name = 'Canon'), NULL, 4.9, 450, true, 15),
('e6f7a8b9-c0d1-4234-e567-f8a9b0c1d234', 'Фотоаппарат Nikon Z6 II', 'Беззеркальная камера с матрицей 24.5 МП', 189999.00, (SELECT category_id FROM category WHERE category_name = 'Фото и видеокамеры'), (SELECT brand_id FROM brand WHERE brand_name = 'Nikon'), NULL, 4.8, 380, true, 18),
('a8b9c0d1-e2f3-4567-a890-b9c0d1e2f345', 'PlayStation 5', 'Игровая консоль нового поколения с поддержкой 4K и Ray Tracing', 59999.00, (SELECT category_id FROM category WHERE category_name = 'Игровые консоли'), (SELECT brand_id FROM brand WHERE brand_name = 'Sony'), NULL, 4.9, 2500, true, 40)
ON CONFLICT (product_id) DO NOTHING;

-- Логи входа для пользователей (login_log)
-- Генерируем логи для каждого пользователя с разными датами и источниками
INSERT INTO login_log (user_id, logged_at, source) VALUES
-- Пользователь 1906953b-83e1-4bc2-9225-6b6f11f6b066
('1906953b-83e1-4bc2-9225-6b6f11f6b066', '2025-12-01 08:15:00', 'Web'),
('1906953b-83e1-4bc2-9225-6b6f11f6b066', '2025-12-05 14:30:00', 'Web'),
('1906953b-83e1-4bc2-9225-6b6f11f6b066', '2025-12-10 09:45:00', 'Mobile'),
('1906953b-83e1-4bc2-9225-6b6f11f6b066', '2025-12-15 16:20:00', 'Web'),
('1906953b-83e1-4bc2-9225-6b6f11f6b066', '2025-12-20 11:00:00', 'Web'),

-- Пользователь 7a2d8986-8428-4e2b-8cde-1ffa89044f15
('7a2d8986-8428-4e2b-8cde-1ffa89044f15', '2025-12-02 10:20:00', 'Mobile'),
('7a2d8986-8428-4e2b-8cde-1ffa89044f15', '2025-12-07 15:10:00', 'Web'),
('7a2d8986-8428-4e2b-8cde-1ffa89044f15', '2025-12-12 08:30:00', 'Web'),
('7a2d8986-8428-4e2b-8cde-1ffa89044f15', '2025-12-18 13:45:00', 'Mobile'),
('7a2d8986-8428-4e2b-8cde-1ffa89044f15', '2025-12-21 17:00:00', 'Web'),

-- Пользователь 1340431d-2a25-47b0-aa95-e7e4cfc7f103
('1340431d-2a25-47b0-aa95-e7e4cfc7f103', '2025-12-03 09:00:00', 'Web'),
('1340431d-2a25-47b0-aa95-e7e4cfc7f103', '2025-12-08 14:15:00', 'Web'),
('1340431d-2a25-47b0-aa95-e7e4cfc7f103', '2025-12-13 10:30:00', 'Mobile'),
('1340431d-2a25-47b0-aa95-e7e4cfc7f103', '2025-12-19 16:00:00', 'Web'),

-- Пользователь ede4b3bf-2ed2-4710-84d8-af5e505a8cff
('ede4b3bf-2ed2-4710-84d8-af5e505a8cff', '2025-12-04 11:25:00', 'Mobile'),
('ede4b3bf-2ed2-4710-84d8-af5e505a8cff', '2025-12-09 08:50:00', 'Web'),
('ede4b3bf-2ed2-4710-84d8-af5e505a8cff', '2025-12-14 15:20:00', 'Web'),
('ede4b3bf-2ed2-4710-84d8-af5e505a8cff', '2025-12-20 12:10:00', 'Mobile'),

-- Пользователь 6081e8c1-706a-4092-8748-2c1a6fca41d9
('6081e8c1-706a-4092-8748-2c1a6fca41d9', '2025-12-01 13:40:00', 'Web'),
('6081e8c1-706a-4092-8748-2c1a6fca41d9', '2025-12-06 09:15:00', 'Web'),
('6081e8c1-706a-4092-8748-2c1a6fca41d9', '2025-12-11 14:50:00', 'Mobile'),
('6081e8c1-706a-4092-8748-2c1a6fca41d9', '2025-12-16 10:25:00', 'Web'),
('6081e8c1-706a-4092-8748-2c1a6fca41d9', '2025-12-21 17:30:00', 'Web'),

-- Пользователь 42b8a3d4-07bd-428f-a8f6-8ad3fd79e7f4
('42b8a3d4-07bd-428f-a8f6-8ad3fd79e7f4', '2025-12-02 07:30:00', 'Mobile'),
('42b8a3d4-07bd-428f-a8f6-8ad3fd79e7f4', '2025-12-07 12:00:00', 'Web'),
('42b8a3d4-07bd-428f-a8f6-8ad3fd79e7f4', '2025-12-12 16:40:00', 'Web'),
('42b8a3d4-07bd-428f-a8f6-8ad3fd79e7f4', '2025-12-17 11:15:00', 'Mobile'),

-- Пользователь 3faaeaeb-8cce-4c5e-833d-40ab07f99476
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-01 08:00:00', 'Web'),
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-05 13:20:00', 'Web'),
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-10 10:10:00', 'Mobile'),
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-15 15:45:00', 'Web'),
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-20 09:30:00', 'Web'),
('3faaeaeb-8cce-4c5e-833d-40ab07f99476', '2025-12-21 14:00:00', 'Mobile');

