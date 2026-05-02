-- Flyway migration: V3__inventory_audit_scaling.sql
-- Objetivo: aplicar correcciones de auditoría para escalar inventory_movements
-- - Idempotencia por referencia de negocio
-- - Índices para consultas frecuentes por negocio/fecha/razón

-- 1) Idempotencia por referencia de negocio (si se usa reference_id)
-- Nota: en MySQL, múltiples NULL en columnas únicas son permitidos y no colisionan entre sí.
ALTER TABLE inventory_movements
  ADD CONSTRAINT uk_im_bpr UNIQUE (business_id, product_id, reason, reference_id);

-- 2) Índices para patrones de consulta típicos
-- a) Listar movimientos por negocio y rango de fechas
CREATE INDEX idx_im_b_created ON inventory_movements (business_id, created_at);
-- b) Filtrar además por reason en auditorías
CREATE INDEX idx_im_b_reason_created ON inventory_movements (business_id, reason, created_at);

-- Notas operativas:
-- - Si existen duplicados con el mismo (business_id, product_id, reason, reference_id) y reference_id NO es NULL,
--   esta migración fallará. En ese caso, sanea los duplicados en entornos previos antes de desplegar a producción.
