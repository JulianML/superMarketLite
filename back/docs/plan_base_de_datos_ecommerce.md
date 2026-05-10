# Plan de Base de Datos para e‑commerce de supermercados locales

Objetivo
- Diseñar una base de datos relacional (MySQL 8) para una app de e‑commerce con dos tipos de actores principales: Usuarios y Negocios (supermercados locales).
- Los negocios publican productos; los usuarios navegan, añaden al carrito, compran y reciben el reparto por el propio supermercado.
- Requisitos No Funcionales: integridad referencial, escalabilidad para múltiples negocios, trazabilidad de pedidos y stock, zonas horarias, auditoría básica.

Decisiones técnicas
- Motor: MySQL 8.0 (InnoDB) con utf8mb4 y collation utf8mb4_0900_ai_ci.
- Zona horaria en servidor/app: UTC; almacenar timestamps en UTC.
- ORM: JPA/Hibernate (ya en proyecto Spring). Migraciones sugeridas: Flyway (opcional inicialmente).
- Estrategia multi‑tenant: un único esquema con tenant_id (= business_id) en tablas dependientes del negocio.
- Soft delete donde aplique (productos, categorías, imágenes, promociones) con campo deleted_at NULLable.
- Moneda: por ahora monomoneda en EUR; mantener currency='EUR' por defecto en products, orders y payments. No se usará exchange_rate de momento.

Modelo de datos (alto nivel)
- Autenticación/Autorización: users, roles, user_roles.
- Negocios: businesses, business_hours, business_addresses.
- Catálogo: categories, products, product_images, product_variants (opcional futuro), product_category (N:N), tags (opcional futuro).
- Stock: inventory (por negocio y producto), inventory_movements (auditoría de cambios de stock).
- Carritos: carts, cart_items.
- Órdenes: orders, order_items, order_status_history.
- Direcciones de usuario: user_addresses (libreta de direcciones reutilizable para checkout).
- Pagos: payments (registro), refunds (opcional futuro).
- Envíos/Entrega: deliveries (entrega gestionada por el supermercado), delivery_zones (opcional), delivery_tracking (opcional futuro).
- Opiniones: reviews.
- Promociones/Cupones: promotions, coupons, coupon_redemptions (opcional futuro).
- Notificaciones: notifications (opcional futuro), email_queue (opcional futuro).
- Auditoría: audit_log (opcional futuro en app; movimientos clave en BD ya cubiertos con tablas específicas).

Diagrama ER (descripción textual)
- users 1:N carts, 1:N orders, 1:N reviews, N:M roles.
- businesses 1:N products, 1:N inventory, 1:N orders, 1:N deliveries, 1:N categories.
- categories 1:N products (o N:M vía product_category si se requiere multi‑categoría).
- products 1:N product_images, 1:N inventory (por negocio), 1:N order_items, 1:N cart_items.
- carts 1:N cart_items; cada cart pertenece a un user (y opcionalmente a un business si el carrito es por tienda).
- orders 1:N order_items, 1:N payments, 1:N order_status_history, 1:1 delivery.

Esquema de tablas (columnas principales)
1) users
- id BIGINT PK AI
- email VARCHAR(255) UNIQUE NOT NULL
- password_hash VARCHAR(255) NOT NULL
- full_name VARCHAR(255) NOT NULL
- phone VARCHAR(30) NULL
- is_business_owner TINYINT(1) DEFAULT 0
- created_at DATETIME NOT NULL
- updated_at DATETIME NOT NULL

2) roles
- id BIGINT PK AI
- name VARCHAR(50) UNIQUE NOT NULL  (ADMIN, BUSINESS_OWNER, CUSTOMER)

3) user_roles
- user_id BIGINT FK -> users.id
- role_id BIGINT FK -> roles.id
- PK (user_id, role_id)

4) businesses
- id BIGINT PK AI
- owner_user_id BIGINT FK -> users.id (dueño de la cuenta)
- name VARCHAR(255) NOT NULL
- slug VARCHAR(255) UNIQUE NOT NULL
- email VARCHAR(255) NOT NULL
- phone VARCHAR(30) NULL
- tax_id VARCHAR(50) NULL  (NIF/CIF/otro)
- description TEXT NULL
- logo_url VARCHAR(512) NULL
- is_active TINYINT(1) DEFAULT 1
- created_at DATETIME NOT NULL
- updated_at DATETIME NOT NULL

5) business_addresses
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- line1 VARCHAR(255) NOT NULL
- line2 VARCHAR(255) NULL
- city VARCHAR(120) NOT NULL
- state VARCHAR(120) NULL
- postal_code VARCHAR(20) NOT NULL
- country_code CHAR(2) NOT NULL
- location POINT NULL SRID 4326 (opcional si se habilita)
- created_at DATETIME NOT NULL

6) business_hours
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- weekday TINYINT NOT NULL CHECK 0..6 (0=domingo)
- open_time TIME NULL
- close_time TIME NULL
- is_closed TINYINT(1) DEFAULT 0

7) categories
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id (categorías por negocio)
- name VARCHAR(255) NOT NULL
- slug VARCHAR(255) NOT NULL
- parent_id BIGINT NULL FK -> categories.id (jerarquía)
- is_active TINYINT(1) DEFAULT 1
- deleted_at DATETIME NULL
- UNIQUE(business_id, slug)

8) products
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- sku VARCHAR(100) NOT NULL
- name VARCHAR(255) NOT NULL
- description TEXT NULL
- price DECIMAL(10,2) NOT NULL
- currency CHAR(3) NOT NULL DEFAULT 'EUR'
- vat_rate DECIMAL(5,2) NULL
- is_active TINYINT(1) DEFAULT 1
- deleted_at DATETIME NULL
- created_at DATETIME NOT NULL
- updated_at DATETIME NOT NULL
- UNIQUE(business_id, sku)

9) product_category (opcional si multi‑categoría)
- product_id BIGINT FK -> products.id
- category_id BIGINT FK -> categories.id
- PK (product_id, category_id)

10) product_images
- id BIGINT PK AI
- product_id BIGINT FK -> products.id
- url VARCHAR(512) NOT NULL
- position INT DEFAULT 0
- alt_text VARCHAR(255) NULL
- deleted_at DATETIME NULL
- updated_at DATETIME NOT NULL

11) inventory
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- product_id BIGINT FK -> products.id
- stock INT NOT NULL DEFAULT 0
- safety_stock INT NOT NULL DEFAULT 0
- updated_at DATETIME NOT NULL
- UNIQUE(business_id, product_id)

12) inventory_movements
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- product_id BIGINT FK -> products.id
- change INT NOT NULL  (positivo/negativo)
- reason VARCHAR(100) NOT NULL  (SALE, RESTOCK, ADJUSTMENT)
- reference_id BIGINT NULL  (order_id o ajuste)
- created_at DATETIME NOT NULL

13) carts
- id BIGINT PK AI
- user_id BIGINT FK -> users.id
- business_id BIGINT FK -> businesses.id  (carritos por tienda)
- status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'  (ACTIVE, CONVERTED, ABANDONED)
- is_active TINYINT(1) NOT NULL DEFAULT 1
- created_at DATETIME NOT NULL
- updated_at DATETIME NOT NULL
- UNIQUE(user_id, business_id, is_active) — La app garantiza que solo exista un carrito con is_active=1 por (user,business). Al convertir a pedido, set is_active=0 y status='CONVERTED'.

14) cart_items
- id BIGINT PK AI
- cart_id BIGINT FK -> carts.id
- product_id BIGINT FK -> products.id
- quantity INT NOT NULL CHECK quantity>0
- unit_price DECIMAL(10,2) NOT NULL  (precio al momento de añadir)
- created_at DATETIME NOT NULL
- UNIQUE(cart_id, product_id)

15) orders
- id BIGINT PK AI
- order_number VARCHAR(50) UNIQUE NOT NULL
- user_id BIGINT FK -> users.id
- business_id BIGINT FK -> businesses.id
- cart_id BIGINT NULL FK -> carts.id
- status VARCHAR(30) NOT NULL  (CREATED, PAID, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, REFUNDED)
- subtotal DECIMAL(10,2) NOT NULL
- shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00
- discount_total DECIMAL(10,2) NOT NULL DEFAULT 0.00
- tax_total DECIMAL(10,2) NOT NULL DEFAULT 0.00
- total DECIMAL(10,2) NOT NULL
- currency CHAR(3) NOT NULL DEFAULT 'EUR'
- notes TEXT NULL
- delivery_address_json JSON NULL  (instantánea de entrega)
- placed_at DATETIME NOT NULL
- updated_at DATETIME NOT NULL
- cancelled_at DATETIME NULL

16) order_items
- id BIGINT PK AI
- order_id BIGINT FK -> orders.id
- product_id BIGINT FK -> products.id
- product_name VARCHAR(255) NOT NULL  (instantánea)
- sku VARCHAR(100) NOT NULL  (instantánea)
- quantity INT NOT NULL
- unit_price DECIMAL(10,2) NOT NULL  (instantánea)
- vat_rate DECIMAL(5,2) NULL
- line_total DECIMAL(10,2) NOT NULL

17) order_status_history
- id BIGINT PK AI
- order_id BIGINT FK -> orders.id
- from_status VARCHAR(30) NULL
- to_status VARCHAR(30) NOT NULL
- changed_by BIGINT NULL FK -> users.id (empleado/owner)
- changed_at DATETIME NOT NULL
- note VARCHAR(255) NULL

18) payments
- id BIGINT PK AI
- order_id BIGINT FK -> orders.id
- provider VARCHAR(50) NOT NULL  (STRIPE, CASH_ON_DELIVERY, OTHER)
- provider_ref VARCHAR(100) NULL
- amount DECIMAL(10,2) NOT NULL
- currency CHAR(3) NOT NULL DEFAULT 'EUR'
- status VARCHAR(30) NOT NULL  (PENDING, AUTHORIZED, CAPTURED, FAILED, REFUNDED)
- paid_at DATETIME NULL
- created_at DATETIME NOT NULL

19) deliveries
- id BIGINT PK AI
- order_id BIGINT UNIQUE FK -> orders.id
- business_id BIGINT FK -> businesses.id
- method VARCHAR(30) NOT NULL DEFAULT 'LOCAL_DELIVERY'  (o PICKUP)
- eta_from DATETIME NULL
- eta_to DATETIME NULL
- delivered_at DATETIME NULL
- courier_name VARCHAR(120) NULL  (empleado del súper)
- courier_phone VARCHAR(30) NULL
- status VARCHAR(30) NOT NULL  (PENDING, ASSIGNED, OUT_FOR_DELIVERY, DELIVERED, FAILED)
- notes VARCHAR(255) NULL
- created_at DATETIME NOT NULL

20) reviews
- id BIGINT PK AI
- user_id BIGINT FK -> users.id
- business_id BIGINT FK -> businesses.id
- order_id BIGINT NOT NULL FK -> orders.id
- rating TINYINT NOT NULL CHECK 1..5
- comment TEXT NULL
- created_at DATETIME NOT NULL
- UNIQUE(order_id)

21) promotions (opcional inicial)
- id BIGINT PK AI
- business_id BIGINT FK -> businesses.id
- name VARCHAR(255) NOT NULL
- description TEXT NULL
- type VARCHAR(30) NOT NULL  (PERCENTAGE, FIXED)
- value DECIMAL(10,2) NOT NULL
- starts_at DATETIME NULL
- ends_at DATETIME NULL
- is_active TINYINT(1) DEFAULT 1
- deleted_at DATETIME NULL

Índices recomendados
- users(email), businesses(slug), products(business_id, sku), products(business_id, is_active), products FULLTEXT(name, description), products(business_id, is_active, price), categories(business_id, parent_id), inventory(business_id, product_id), carts(user_id, business_id, status), cart_items(cart_id), cart_items(product_id, cart_id), orders(business_id, user_id, status, placed_at), order_items(order_id), payments(order_id, status), deliveries(order_id, status), reviews(business_id, rating).

Reglas de integridad y negocio
- Eliminar negocio: restringido si tiene pedidos; desactivar en lugar de borrar.
- Eliminar producto: soft delete; no borrar si hay referencias en orders/order_items.
- Un carrito activo por usuario y negocio: implementar con columna is_active y UNIQUE(user_id, business_id, is_active). La app garantiza un único cart con is_active=1 por par (user,business); al convertir a pedido, set is_active=0 y status='CONVERTED'. Se puede reforzar con trigger si se desea.
- Stock: decrementar en creación/pago de pedido con transacción y SELECT ... FOR UPDATE; registrar en inventory_movements.
- Precios y nombres en órdenes: guardar como instantánea para trazabilidad, independiente de cambios futuros en catálogo.
- ON DELETE/UPDATE: definir explícitamente en FKs: referencias a products en tablas no históricas (cart_items, inventory, product_images) usan ON DELETE CASCADE; en tablas históricas (orders, order_items, inventory_movements) usar ON DELETE RESTRICT. Referencias opcionales a users (p. ej., order_status_history.changed_by) usan ON DELETE SET NULL. Entidades dependientes de negocio: RESTRICT (salvo direcciones/horarios de negocio, que usan CASCADE).

DDL inicial (MySQL 8)
SET NAMES utf8mb4; SET time_zone = '+00:00';

CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255) NOT NULL,
  phone VARCHAR(30),
  is_business_owner TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  label VARCHAR(100) NULL,
  line1 VARCHAR(255) NOT NULL,
  line2 VARCHAR(255) NULL,
  city VARCHAR(120) NOT NULL,
  state VARCHAR(120) NULL,
  postal_code VARCHAR(20) NOT NULL,
  country_code CHAR(2) NOT NULL,
  location POINT NULL SRID 4326,
  is_default TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_ua_u FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  INDEX idx_ua_u (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE businesses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  owner_user_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL,
  phone VARCHAR(30),
  tax_id VARCHAR(50),
  description TEXT,
  logo_url VARCHAR(512),
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  CONSTRAINT fk_b_owner FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE business_addresses (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  line1 VARCHAR(255) NOT NULL,
  line2 VARCHAR(255) NULL,
  city VARCHAR(120) NOT NULL,
  state VARCHAR(120) NULL,
  postal_code VARCHAR(20) NOT NULL,
  country_code CHAR(2) NOT NULL,
  location POINT NULL SRID 4326,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_ba_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE business_hours (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  weekday TINYINT NOT NULL,
  open_time TIME NULL,
  close_time TIME NULL,
  is_closed TINYINT(1) NOT NULL DEFAULT 0,
  CONSTRAINT fk_bh_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
  INDEX idx_bh_b (business_id, weekday)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE categories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  slug VARCHAR(255) NOT NULL,
  parent_id BIGINT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  deleted_at DATETIME NULL,
  UNIQUE (business_id, slug),
  INDEX idx_cat_parent (business_id, parent_id),
  CONSTRAINT fk_cat_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
  CONSTRAINT fk_cat_parent FOREIGN KEY (parent_id) REFERENCES categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE products (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  sku VARCHAR(100) NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  price DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  vat_rate DECIMAL(5,2) NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  deleted_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  UNIQUE (business_id, sku),
  INDEX idx_prod_active (business_id, is_active),
  CONSTRAINT fk_p_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE products ADD FULLTEXT INDEX ftx_products_name_desc (name, description);
CREATE INDEX idx_prod_b_price ON products(business_id, is_active, price);

CREATE TABLE product_images (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  url VARCHAR(512) NOT NULL,
  position INT NOT NULL DEFAULT 0,
  alt_text VARCHAR(255),
  deleted_at DATETIME NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_pi_p (product_id),
  CONSTRAINT fk_pi_p FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  safety_stock INT NOT NULL DEFAULT 0,
  updated_at DATETIME NOT NULL,
  UNIQUE (business_id, product_id),
  INDEX idx_inv_bp (business_id, product_id),
  CONSTRAINT fk_inv_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
  CONSTRAINT fk_inv_p FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inventory_movements (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  business_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  change INT NOT NULL,
  reason VARCHAR(100) NOT NULL,
  reference_id BIGINT NULL,
  created_at DATETIME NOT NULL,
  CONSTRAINT fk_im_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE CASCADE,
  CONSTRAINT fk_im_p FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT,
  INDEX idx_im_bp (business_id, product_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE carts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  business_id BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  INDEX idx_cart_user (user_id, status),
  INDEX idx_cart_b (business_id, status),
  CONSTRAINT fk_cart_u FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_cart_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uk_cart_active ON carts(user_id, business_id, is_active);

CREATE TABLE cart_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cart_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE (cart_id, product_id),
  INDEX idx_ci_cart (cart_id),
  CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
  CONSTRAINT fk_ci_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_ci_prod ON cart_items(product_id, cart_id);

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_number VARCHAR(50) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  business_id BIGINT NOT NULL,
  cart_id BIGINT NULL,
  status VARCHAR(30) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL,
  shipping_fee DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  discount_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  tax_total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  total DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  notes TEXT NULL,
  delivery_address_json JSON NULL,
  placed_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  cancelled_at DATETIME NULL,
  INDEX idx_o_bu (business_id, user_id, status, placed_at),
  CONSTRAINT fk_o_u FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_o_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
  CONSTRAINT fk_o_c FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_items (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  sku VARCHAR(100) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  vat_rate DECIMAL(5,2) NULL,
  line_total DECIMAL(10,2) NOT NULL,
  INDEX idx_oi_o (order_id),
  CONSTRAINT fk_oi_o FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
  CONSTRAINT fk_oi_p FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE order_status_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  from_status VARCHAR(30) NULL,
  to_status VARCHAR(30) NOT NULL,
  changed_by BIGINT NULL,
  changed_at DATETIME NOT NULL,
  note VARCHAR(255) NULL,
  INDEX idx_osh_o (order_id),
  CONSTRAINT fk_osh_o FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
  CONSTRAINT fk_osh_u FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  provider VARCHAR(50) NOT NULL,
  provider_ref VARCHAR(100) NULL,
  amount DECIMAL(10,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  status VARCHAR(30) NOT NULL,
  paid_at DATETIME NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_pay_o (order_id, status),
  CONSTRAINT fk_pay_o FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE deliveries (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL UNIQUE,
  business_id BIGINT NOT NULL,
  method VARCHAR(30) NOT NULL DEFAULT 'LOCAL_DELIVERY',
  eta_from DATETIME NULL,
  eta_to DATETIME NULL,
  delivered_at DATETIME NULL,
  courier_name VARCHAR(120) NULL,
  courier_phone VARCHAR(30) NULL,
  status VARCHAR(30) NOT NULL,
  notes VARCHAR(255) NULL,
  created_at DATETIME NOT NULL,
  INDEX idx_del_b (business_id, status),
  CONSTRAINT fk_del_o FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT,
  CONSTRAINT fk_del_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  business_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  rating TINYINT NOT NULL,
  comment TEXT NULL,
  created_at DATETIME NOT NULL,
  UNIQUE KEY uk_review_order (order_id),
  INDEX idx_rev_b (business_id, rating),
  CONSTRAINT fk_rev_u FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
  CONSTRAINT fk_rev_b FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
  CONSTRAINT fk_rev_o FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- (Opcionales: promotions)

Población inicial
- roles: ADMIN, BUSINESS_OWNER, CUSTOMER.
- Crear 1–2 negocios demo, categorías y productos de ejemplo.

Migraciones y despliegue
- Recomendada integración con Flyway: colocar archivos en src/main/resources/db/migration/V1__init.sql con el DDL anterior.
- docker-compose ya define MySQL. Asegurar variables en Spring: spring.jpa.hibernate.ddl-auto=validate (en prod) y usar Flyway para cambios.

Métricas y reporting (futuro)
- Vistas/materializadas o ETL para KPIs: ventas por negocio, ticket medio, top productos, tasa de abandono de carrito.

Seguridad y privacidad
- Nunca almacenar datos sensibles de tarjetas (usar proveedor). Hash robusto de contraseñas (BCrypt/Argon2).
- Logs de acceso en app; en BD solo lo necesario para pedidos/entregas.

Evolución futura
- Soporte de múltiples almacenes por negocio.
- Variante de productos (tallas/formatos) con product_variants.
- Cupones y reglas de descuento avanzadas.
- Pickup en tienda además de entrega local.
