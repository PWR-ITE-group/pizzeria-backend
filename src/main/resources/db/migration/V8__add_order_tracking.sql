-- Migration V8: Add order tracking token and make employee_id nullable for client orders

-- Add tracking_token column to orders table
ALTER TABLE pizzeria_schema.orders
ADD COLUMN IF NOT EXISTS tracking_token VARCHAR(64) UNIQUE;

-- Make employee_id nullable (for client-created orders)
ALTER TABLE pizzeria_schema.orders
ALTER COLUMN employee_id DROP NOT NULL;

-- Create index on tracking_token for fast lookups
CREATE INDEX IF NOT EXISTS idx_orders_tracking_token ON pizzeria_schema.orders (tracking_token);

