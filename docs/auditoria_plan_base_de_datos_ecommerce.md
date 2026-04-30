# Auditoría del Plan de Base de Datos (MySQL 8) para e‑commerce de supermercados

Autor: Revisión senior de arquitectura e‑commerce
Fecha: 2026‑04‑29

Resumen ejecutivo
- El plan es sólido y bien alineado con un e‑commerce multi‑negocio (marketplace de supermercados) y cubre catálogo, stock, carritos, pedidos, pagos y entregas. La estrategia de un solo esquema con business_id como tenant es adecuada.
- El DDL incluido en el documento ya contempla las tablas y relaciones esenciales (incluye user_addresses, business_addresses, business_hours, inventory_movements, índices clave y restricciones). Hay algunos detalles menores a afinar (p. ej., políticas ON DELETE explícitas coherentes, uso prudente de CHECK, y consistencia de campos de auditoría).
- Con pequeños ajustes de DDL e índices y documentando el control transaccional de stock, el plan está listo para implementación con Flyway.

Fortalezas
- Multi‑tenant por business_id en tablas dependientes (docs líneas 12, 31, 93, 103, 131, 169). Escalable y simple de operar.
- Trazabilidad: snapshots en order_items (nombre, sku, precio, vat) y orders (dirección en JSON). Bien para históricos (líneas 187–192, 178).
- Auditoría de stock prevista via inventory_movements (líneas 139–146) y status history de pedidos (líneas 195–201).
- Soft delete en entidades de catálogo (líneas 13, 98, 111, 127, 247). Adecuado para no romper histórico.
- Índices principales considerados (líneas 249–251). Correctos para consultas habituales.

Riesgos y brechas detectadas
1) Restricciones no soportadas exactamente como se redactan en MySQL
   - CHECK en weekday (línea 86) y rating (línea 232): MySQL 8 evalúa CHECK pero es común que no se usen o se ignoren en versiones/engines antiguos; conviene reforzar vía aplicación o triggers simples si es crítico.
   - “UNIQUE(user_id, business_id, status) con filtro status='ACTIVE'” (línea 154): MySQL no soporta índices parciales por condición. Se debe modelar distinto (ver Recomendación R1).

2) Reglas ON DELETE/ON UPDATE ausentes
   - No se especifica qué pasa al borrar usuarios/negocios/productos (líneas 253–258 mencionan reglas de negocio, pero las FKs carecen de ON DELETE). Define: RESTRICT, SET NULL o CASCADE según caso. Imprescindible para consistencia.

3) Direcciones de usuario y de entrega
   - Existe business_addresses (líneas 71–82) pero no user_addresses para checkout recurrente ni address_book; orders guarda JSON (línea 178), correcto como snapshot, pero falta entidad para reusabilidad (ver R3).

4) DDL incompleto para tablas mencionadas como “opcionales” pero necesarias desde el día 1
   - inventory_movements (líneas 139–146) citado pero sin DDL en bloque final (línea 481 lo marca opcional). Para stock confiable desde MVP, conviene incluirlo.
   - business_hours y business_addresses: especificadas arriba pero omitidas en DDL final. 
   - promotions mencionada como opcional; si hay descuentos en órdenes (línea 174), al menos un vínculo order_id↔promotion(s) o un campo promotion_snapshot puede ser útil si se aplican en MVP.

5) Unicidad en reviews
   - UNIQUE(user_id, business_id, order_id) (líneas 235, 474) permite múltiples NULL en order_id según semántica de MySQL, lo que podría romper “una reseña por usuario y negocio por pedido o en general”. Definir claramente: 
     - una reseña por pedido: UNIQUE(order_id) + FK NOT NULL; o
     - una reseña por negocio por usuario sin pedido: usar una columna computed de coalescencia o una restricción en app (ver R4).

6) Índices mejorables
   - Búsqueda por nombre/sku de productos frecuentemente requiere índices prefix/FTS: falta FULLTEXT(name, description) en products (si MySQL >= 8 con InnoDB). 
   - Filtro por categoría activa en negocio: índice (business_id, category_id) si se usa tabla N:M; si no, (business_id, parent_id) ya existe.

7) Control de stock atómico
   - Se menciona decrementar stock “en creación/pago del pedido” (línea 256). Requiere transacciones y chequeo de disponibilidad con bloqueo (SELECT ... FOR UPDATE) y registro en inventory_movements. No está detallado. 

8) Moneda y impuestos
   - currency a nivel de producto (línea 108) podría ser a nivel de negocio; a nivel de order es imprescindible y ya está en payments. Recomiendo currency en orders también (si el negocio pudiera cambiar moneda en el futuro) y guardar exchange_rate si aplica.

9) Campos de auditoría uniformes
   - Algunas tablas tienen created_at/updated_at; otras no (e.g., product_images carece de updated_at, deliveries carece de created_at). Uniformizar simplifica trazabilidad.

10) Normalización de slugs y uniqueness
   - categories: UNIQUE(business_id, slug) correcto (línea 99), pero falta UNIQUE(business_id, name) si se quiere evitar duplicados evidentes; evaluar según negocio.

Recomendaciones (con DDL sugerido)
R1. Carrito activo único por usuario y negocio (sin índices parciales)
- Opción A (recomendada): Añadir columna is_active TINYINT(1) con UNIQUE(user_id, business_id) filtrada por aplicación y constraint lógica con trigger.
- Opción B: Usar constraint lógica con índice parcial simulado por columna derivada.
DDL sugerido (Opción A):
ALTER TABLE carts ADD COLUMN is_active TINYINT(1) NOT NULL DEFAULT 1 AFTER status;
CREATE UNIQUE INDEX uk_cart_active ON carts(user_id, business_id, is_active);
-- La app debe asegurar que solo exista un cart con is_active=1 por par (user,business). Al convertir a pedido, set is_active=0 y status='CONVERTED'.

R2. Definir ON DELETE/ON UPDATE coherentes
- products → orders/order_items: RESTRICT para proteger históricos.
- products → cart_items/inventory: CASCADE o RESTRICT según soft delete (si se usa soft delete, mantener RESTRICT y app evita borrar físicos).
- businesses → dependientes: RESTRICT y usar is_active.
- users → orders/reviews/carts: RESTRICT o SET NULL en changed_by.
Ejemplos:
ALTER TABLE order_items DROP FOREIGN KEY fk_oi_p, ADD CONSTRAINT fk_oi_p FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
ALTER TABLE cart_items DROP FOREIGN KEY fk_ci_prod, ADD CONSTRAINT fk_ci_prod FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT;
ALTER TABLE order_status_history DROP FOREIGN KEY fk_osh_u, ADD CONSTRAINT fk_osh_u FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE SET NULL;

R3. Direcciones de usuario reutilizables + snapshot en order
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
-- Mantener delivery_address_json en orders como snapshot inmutable.

R4. Unicidad de reviews según estrategia
- Si es “una reseña por pedido”: hacer order_id NOT NULL y UNIQUE(order_id).
ALTER TABLE reviews MODIFY order_id BIGINT NOT NULL;
CREATE UNIQUE INDEX uk_review_order ON reviews(order_id);
- Si es “una reseña general por negocio y usuario”: usar UNIQUE(user_id, business_id) y permitir order_id NULL opcional.

R5. Incluir tablas omitidas en DDL final (mínimas para MVP)
-- business_addresses
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
-- business_hours
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
-- inventory_movements
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

R6. Índices para búsqueda y catálogo
ALTER TABLE products ADD FULLTEXT INDEX ftx_products_name_desc (name, description);
CREATE INDEX idx_prod_b_price ON products(business_id, is_active, price);
CREATE INDEX idx_ci_prod ON cart_items(product_id, cart_id);

R7. Control de stock transaccional
- Al confirmar pedido: por cada item, SELECT stock FROM inventory WHERE business_id=? AND product_id=? FOR UPDATE; validar (stock - qty >= -safety_stock), actualizar stock, insertar inventory_movements dentro de la misma transacción que la creación de order_items. Documentarlo en el servicio.

R8. Campos de auditoría consistentes
- Añadir created_at/updated_at en tablas que falten (product_images.updated_at, deliveries.created_at) para facilitar troubleshooting.

R9. Moneda a nivel de order
- Añadir currency y opcional exchange_rate en orders si se prevé más de una moneda.
ALTER TABLE orders ADD COLUMN currency CHAR(3) NOT NULL DEFAULT 'EUR' AFTER total;
-- exchange_rate DECIMAL(18,8) NULL si se usa conversión.

R10. Higiene de catálogos
- Evaluar UNIQUE(business_id, name) en categories y UNIQUE(business_id, name) opcional en products si el negocio prohíbe duplicados de nombres visibles.

Observaciones menores
- vat_rate NULL a nivel de producto está bien; en order_items se guarda snapshot. Considerar reglas de redondeo a nivel de línea y total consistente (bankers vs away from zero), y DECIMAL(12,4) para totales si se prevé importes altos o precisión adicional.
- deliveries.status: alinear con orders.status transitions; crear una vista materializada o job para SLA (tiempos por estado) en reporting futuro.

Veredicto
- El plan es mayormente correcto y apto para MVP. Con la aplicación de R1, R2, R3, R5 y R6 como prioridad alta, se mitigan riesgos funcionales y de integridad. El resto son mejoras de calidad/operación.

Prioridad y próximos pasos
1) Alta: R1 (carrito único), R2 (ON DELETE/UPDATE), R5 (DDL faltante), R7 (proceso stock), R6 (índices búsqueda). 
2) Media: R3 (user_addresses), R8 (auditoría uniforme), R4 (política reviews), R9 (currency en orders si aplica).
3) Baja: R10 (unicidad por nombre), ajustes de DECIMAL y redondeo, vistas para reporting.

Checklist de verificación para despliegue
- Migración Flyway V1 con el DDL completo y coherente.
- V2 con ajustes de índices FULLTEXT si el motor los soporta en el entorno.
- Tests de integridad: 
  - No más de un carrito activo por (user,business).
  - No se pueden borrar productos referenciados en órdenes.
  - Flujo de stock genera movimientos y no permite over‑sell más allá del safety_stock.
  - Búsqueda por texto retorna resultados esperados.
