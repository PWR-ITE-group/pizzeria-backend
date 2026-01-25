
ALTER TABLE pizzeria_schema.orders
ADD COLUMN IF NOT EXISTS tracking_token VARCHAR(64) UNIQUE;

ALTER TABLE pizzeria_schema.orders
ALTER COLUMN employee_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_orders_tracking_token ON pizzeria_schema.orders (tracking_token);

