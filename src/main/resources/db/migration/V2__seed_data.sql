-- Seed data migration: populate all tables with at least 5 rows each
-- Note: values are crafted to satisfy FKs and UNIQUE constraints
SET NAMES utf8mb4; SET time_zone = '+00:00';

-- 1) users
INSERT INTO users (id, email, password_hash, full_name, phone, is_business_owner, created_at, updated_at) VALUES
  (1,'alice@example.com','$2a$10$seedhashAlice','Alice Doe','+34111111111',1,'2025-01-01 10:00:00','2025-01-01 10:00:00'),
  (2,'bob@example.com','$2a$10$seedhashBob','Bob Smith','+34222222222',1,'2025-01-01 10:05:00','2025-01-01 10:05:00'),
  (3,'carol@example.com','$2a$10$seedhashCarol','Carol Johnson','+34333333333',0,'2025-01-01 10:10:00','2025-01-01 10:10:00'),
  (4,'dave@example.com','$2a$10$seedhashDave','Dave Brown','+34444444444',0,'2025-01-01 10:15:00','2025-01-01 10:15:00'),
  (5,'eve@example.com','$2a$10$seedhashEve','Eve Davis','+34555555555',0,'2025-01-01 10:20:00','2025-01-01 10:20:00');

-- 2) roles
INSERT INTO roles (id, name) VALUES
  (1,'ADMIN'),
  (2,'BUSINESS_OWNER'),
  (3,'CUSTOMER'),
  (4,'DISPATCHER'),
  (5,'ACCOUNTANT');

-- 3) user_roles
INSERT INTO user_roles (user_id, role_id) VALUES
  (1,1),
  (1,2),
  (2,2),
  (3,3),
  (4,3);

-- 4) user_addresses (location left NULL to avoid SRID-specific insert quirks)
INSERT INTO user_addresses (id, user_id, label, line1, line2, city, state, postal_code, country_code, location, is_default, created_at, updated_at) VALUES
  (1,1,'Casa','Calle A 1',NULL,'Madrid',NULL,'28001','ES',NULL,1,'2025-01-02 09:00:00','2025-01-02 09:00:00'),
  (2,2,'Oficina','Avenida B 2',NULL,'Barcelona',NULL,'08001','ES',NULL,1,'2025-01-02 09:05:00','2025-01-02 09:05:00'),
  (3,3,'Casa','Calle C 3',NULL,'Valencia',NULL,'46001','ES',NULL,1,'2025-01-02 09:10:00','2025-01-02 09:10:00'),
  (4,4,'Casa','Calle D 4',NULL,'Sevilla',NULL,'41001','ES',NULL,1,'2025-01-02 09:15:00','2025-01-02 09:15:00'),
  (5,5,'Casa','Calle E 5',NULL,'Bilbao',NULL,'48001','ES',NULL,1,'2025-01-02 09:20:00','2025-01-02 09:20:00');

-- 5) businesses
INSERT INTO businesses (id, owner_user_id, name, slug, email, phone, tax_id, description, logo_url, is_active, created_at, updated_at) VALUES
  (1,1,'Tienda Alfa','tienda-alfa','alfa@shop.com','+34910000001','A00000001','Tienda de electrónica','https://pics.example/alfa.png',1,'2025-01-03 08:00:00','2025-01-03 08:00:00'),
  (2,2,'Mercado Beta','mercado-beta','beta@shop.com','+34910000002','B00000002','Supermercado local','https://pics.example/beta.png',1,'2025-01-03 08:05:00','2025-01-03 08:05:00'),
  (3,1,'Moda Gamma','moda-gamma','gamma@shop.com','+34910000003','C00000003','Ropa y accesorios','https://pics.example/gamma.png',1,'2025-01-03 08:10:00','2025-01-03 08:10:00'),
  (4,2,'Hogar Delta','hogar-delta','delta@shop.com','+34910000004','D00000004','Artículos del hogar','https://pics.example/delta.png',1,'2025-01-03 08:15:00','2025-01-03 08:15:00'),
  (5,1,'Juguetes Epsilon','juguetes-epsilon','epsilon@shop.com','+34910000005','E00000005','Juguetes y juegos','https://pics.example/epsilon.png',1,'2025-01-03 08:20:00','2025-01-03 08:20:00');

-- 6) business_addresses
INSERT INTO business_addresses (id, business_id, line1, line2, city, state, postal_code, country_code, location, created_at) VALUES
  (1,1,'Calle Tech 1',NULL,'Madrid',NULL,'28010','ES',NULL,'2025-01-03 09:00:00'),
  (2,2,'Calle Mercado 2',NULL,'Barcelona',NULL,'08010','ES',NULL,'2025-01-03 09:05:00'),
  (3,3,'Calle Moda 3',NULL,'Valencia',NULL,'46010','ES',NULL,'2025-01-03 09:10:00'),
  (4,4,'Calle Hogar 4',NULL,'Sevilla',NULL,'41010','ES',NULL,'2025-01-03 09:15:00'),
  (5,5,'Calle Juego 5',NULL,'Bilbao',NULL,'48010','ES',NULL,'2025-01-03 09:20:00');

-- 7) business_hours (Mon-Fri for business 1)
INSERT INTO business_hours (id, business_id, weekday, open_time, close_time, is_closed) VALUES
  (1,1,1,'09:00:00','18:00:00',0),
  (2,1,2,'09:00:00','18:00:00',0),
  (3,1,3,'09:00:00','18:00:00',0),
  (4,1,4,'09:00:00','18:00:00',0),
  (5,1,5,'09:00:00','18:00:00',0);

-- 8) categories (root categories per different businesses)
INSERT INTO categories (id, business_id, name, slug, parent_id, is_active, deleted_at) VALUES
  (1,1,'Electrónica','electronica',NULL,1,NULL),
  (2,2,'Alimentación','alimentacion',NULL,1,NULL),
  (3,3,'Ropa','ropa',NULL,1,NULL),
  (4,4,'Hogar','hogar',NULL,1,NULL),
  (5,5,'Juguetes','juguetes',NULL,1,NULL);

-- 9) products
INSERT INTO products (id, business_id, sku, name, description, price, currency, vat_rate, is_active, deleted_at, created_at, updated_at) VALUES
  (1,1,'ALF-001','Auriculares','Auriculares inalambricos',49.99,'EUR',21.00,1,NULL,'2025-01-04 10:00:00','2025-01-04 10:00:00'),
  (2,1,'ALF-002','Teclado','Teclado mecánico',89.90,'EUR',21.00,1,NULL,'2025-01-04 10:05:00','2025-01-04 10:05:00'),
  (3,2,'BET-001','Leche 1L','Leche entera 1L',1.10,'EUR',4.00,1,NULL,'2025-01-04 10:10:00','2025-01-04 10:10:00'),
  (4,3,'GAM-001','Camiseta','Camiseta algodón',12.50,'EUR',21.00,1,NULL,'2025-01-04 10:15:00','2025-01-04 10:15:00'),
  (5,5,'EPS-001','Puzzle 1000p','Rompecabezas 1000 piezas',15.99,'EUR',21.00,1,NULL,'2025-01-04 10:20:00','2025-01-04 10:20:00');

-- 10) product_images
INSERT INTO product_images (id, product_id, url, position, alt_text, deleted_at, updated_at) VALUES
  (1,1,'https://pics.example/auriculares1.png',0,'Auriculares vista 1',NULL,'2025-01-04 11:00:00'),
  (2,2,'https://pics.example/teclado1.png',0,'Teclado vista 1',NULL,'2025-01-04 11:05:00'),
  (3,3,'https://pics.example/leche1.png',0,'Leche 1L',NULL,'2025-01-04 11:10:00'),
  (4,4,'https://pics.example/camiseta1.png',0,'Camiseta',NULL,'2025-01-04 11:15:00'),
  (5,5,'https://pics.example/puzzle1.png',0,'Puzzle',NULL,'2025-01-04 11:20:00');

-- 11) inventory
INSERT INTO inventory (id, business_id, product_id, stock, safety_stock, updated_at) VALUES
  (1,1,1,100,10,'2025-01-05 09:00:00'),
  (2,1,2,80,8,'2025-01-05 09:05:00'),
  (3,2,3,200,20,'2025-01-05 09:10:00'),
  (4,3,4,150,15,'2025-01-05 09:15:00'),
  (5,5,5,60,6,'2025-01-05 09:20:00');

-- 12) inventory_movements
INSERT INTO inventory_movements (id, business_id, product_id, quantity_change, reason, reference_id, created_at) VALUES
  (1,1,1,100,'INITIAL_STOCK',NULL,'2025-01-05 09:00:00'),
  (2,1,2,80,'INITIAL_STOCK',NULL,'2025-01-05 09:05:00'),
  (3,2,3,200,'INITIAL_STOCK',NULL,'2025-01-05 09:10:00'),
  (4,3,4,150,'INITIAL_STOCK',NULL,'2025-01-05 09:15:00'),
  (5,5,5,60,'INITIAL_STOCK',NULL,'2025-01-05 09:20:00');

-- 13) carts
INSERT INTO carts (id, user_id, business_id, status, is_active, created_at, updated_at) VALUES
  (1,3,1,'ACTIVE',1,'2025-01-06 08:00:00','2025-01-06 08:00:00'),
  (2,4,1,'ACTIVE',0,'2025-01-06 08:05:00','2025-01-06 08:10:00'),
  (3,3,2,'ACTIVE',1,'2025-01-06 08:15:00','2025-01-06 08:15:00'),
  (4,5,3,'ACTIVE',1,'2025-01-06 08:20:00','2025-01-06 08:20:00'),
  (5,5,5,'ACTIVE',1,'2025-01-06 08:25:00','2025-01-06 08:25:00');

-- 14) cart_items
INSERT INTO cart_items (id, cart_id, product_id, quantity, unit_price, created_at) VALUES
  (1,1,1,1,49.99,'2025-01-06 09:00:00'),
  (2,1,2,1,89.90,'2025-01-06 09:05:00'),
  (3,3,3,6,1.10,'2025-01-06 09:10:00'),
  (4,4,4,2,12.50,'2025-01-06 09:15:00'),
  (5,5,5,1,15.99,'2025-01-06 09:20:00');

-- 15) orders
INSERT INTO orders (id, order_number, user_id, business_id, cart_id, status, subtotal, shipping_fee, discount_total, tax_total, total, currency, notes, delivery_address_json, placed_at, updated_at, cancelled_at) VALUES
  (1,'ORD-20250107-001',3,1,1,'PAID',139.89,4.99,0.00,29.38,174.26,'EUR','Entrega estándar', '{"line1":"Calle A 1","city":"Madrid","postal_code":"28001","country":"ES"}','2025-01-07 10:00:00','2025-01-07 10:10:00',NULL),
  (2,'ORD-20250107-002',4,1,2,'PAID',89.90,3.99,0.00,18.88,112.77,'EUR','Entrega 24h', '{"line1":"Calle D 4","city":"Sevilla","postal_code":"41001","country":"ES"}','2025-01-07 10:20:00','2025-01-07 10:30:00',NULL),
  (3,'ORD-20250107-003',3,2,3,'PAID',6.60,0.00,0.00,0.26,6.86,'EUR',NULL, '{"line1":"Calle C 3","city":"Valencia","postal_code":"46001","country":"ES"}','2025-01-07 10:40:00','2025-01-07 10:45:00',NULL),
  (4,'ORD-20250107-004',5,3,4,'PAID',25.00,2.99,0.00,5.25,33.24,'EUR',NULL, '{"line1":"Calle E 5","city":"Bilbao","postal_code":"48001","country":"ES"}','2025-01-07 11:00:00','2025-01-07 11:05:00',NULL),
  (5,'ORD-20250107-005',5,5,5,'PAID',15.99,2.99,0.00,3.36,22.34,'EUR','Regalo', '{"line1":"Calle E 5","city":"Bilbao","postal_code":"48001","country":"ES"}','2025-01-07 11:20:00','2025-01-07 11:25:00',NULL);

-- 16) order_items
INSERT INTO order_items (id, order_id, product_id, product_name, sku, quantity, unit_price, vat_rate, line_total) VALUES
  (1,1,1,'Auriculares','ALF-001',1,49.99,21.00,49.99),
  (2,1,2,'Teclado','ALF-002',1,89.90,21.00,89.90),
  (3,3,3,'Leche 1L','BET-001',6,1.10,4.00,6.60),
  (4,4,4,'Camiseta','GAM-001',2,12.50,21.00,25.00),
  (5,5,5,'Puzzle 1000p','EPS-001',1,15.99,21.00,15.99);

-- 17) order_status_history
INSERT INTO order_status_history (id, order_id, from_status, to_status, changed_by, changed_at, note) VALUES
  (1,1,NULL,'CREATED',1,'2025-01-07 10:00:00','Pedido creado'),
  (2,1,'CREATED','PAID',1,'2025-01-07 10:05:00','Pago confirmado'),
  (3,2,NULL,'CREATED',2,'2025-01-07 10:20:00','Pedido creado'),
  (4,3,NULL,'CREATED',1,'2025-01-07 10:40:00','Pedido creado'),
  (5,4,NULL,'CREATED',2,'2025-01-07 11:00:00','Pedido creado');

-- 18) payments
INSERT INTO payments (id, order_id, provider, provider_ref, amount, currency, status, paid_at, created_at) VALUES
  (1,1,'STRIPE','pi_1',174.26,'EUR','PAID','2025-01-07 10:05:00','2025-01-07 10:00:00'),
  (2,2,'STRIPE','pi_2',112.77,'EUR','PAID','2025-01-07 10:25:00','2025-01-07 10:20:00'),
  (3,3,'CASH',NULL,6.86,'EUR','PAID','2025-01-07 10:42:00','2025-01-07 10:40:00'),
  (4,4,'STRIPE','pi_4',33.24,'EUR','PAID','2025-01-07 11:02:00','2025-01-07 11:00:00'),
  (5,5,'STRIPE','pi_5',22.34,'EUR','PAID','2025-01-07 11:22:00','2025-01-07 11:20:00');

-- 19) deliveries (one per order; order_id is UNIQUE)
INSERT INTO deliveries (id, order_id, business_id, method, eta_from, eta_to, delivered_at, courier_name, courier_phone, status, notes, created_at) VALUES
  (1,1,1,'LOCAL_DELIVERY','2025-01-07 15:00:00','2025-01-07 17:00:00','2025-01-07 16:10:00','Juan Rider','+34600000001','DELIVERED','Gracias','2025-01-07 10:10:00'),
  (2,2,1,'LOCAL_DELIVERY','2025-01-07 16:00:00','2025-01-07 18:00:00','2025-01-07 17:20:00','Ana Rider','+34600000002','DELIVERED',NULL,'2025-01-07 10:30:00'),
  (3,3,2,'PICKUP',NULL,NULL,'2025-01-07 12:00:00',NULL,NULL,'PICKED_UP','Mostrador','2025-01-07 10:45:00'),
  (4,4,3,'LOCAL_DELIVERY','2025-01-07 18:00:00','2025-01-07 20:00:00','2025-01-07 19:10:00','Luis Rider','+34600000003','DELIVERED',NULL,'2025-01-07 11:05:00'),
  (5,5,5,'LOCAL_DELIVERY','2025-01-08 10:00:00','2025-01-08 12:00:00','2025-01-08 11:15:00','Marta Rider','+34600000004','DELIVERED','Feliz regalo','2025-01-07 11:25:00');

-- 20) reviews (one per order)
INSERT INTO reviews (id, user_id, business_id, order_id, rating, comment, created_at) VALUES
  (1,3,1,1,5,'Excelente servicio','2025-01-08 12:00:00'),
  (2,4,1,2,4,'Bien, pero podría mejorar el embalaje','2025-01-08 12:10:00'),
  (3,3,2,3,5,'Rápido y fresco','2025-01-08 12:20:00'),
  (4,5,3,4,4,'Todo correcto','2025-01-08 12:30:00'),
  (5,5,5,5,5,'Genial para un regalo','2025-01-08 12:40:00');
