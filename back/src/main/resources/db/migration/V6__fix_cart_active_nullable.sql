-- Allow multiple checked-out carts per user+business.
-- The unique index on (user_id, business_id, is_active) only works correctly
-- when is_active is nullable: MySQL treats each NULL as distinct, so inactive
-- carts (NULL) don't conflict, while the single active cart (1) remains unique.
ALTER TABLE carts MODIFY COLUMN is_active TINYINT(1) NULL DEFAULT 1;

-- Convert existing inactive rows (0) to NULL so they don't occupy the unique slot.
UPDATE carts SET is_active = NULL WHERE is_active = 0;
