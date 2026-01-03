-- -----------------------------------------------------
-- PIZZA CONFIGURATION: Custom and Modified Pizzas
-- -----------------------------------------------------

-- 1. Create order_item_ingredients table for storing ingredient configurations
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_item_ingredients
(
    id                  BIGSERIAL PRIMARY KEY,
    order_item_id       BIGINT NOT NULL REFERENCES pizzeria_schema.order_items (id) ON DELETE CASCADE,
    ingredient_id       BIGINT NOT NULL REFERENCES pizzeria_schema.ingredients (id) ON DELETE RESTRICT,
    quantity            NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    modification_type   VARCHAR(20) NOT NULL CHECK (modification_type IN ('base', 'added', 'removed'))
);

-- Indexes for performance
CREATE INDEX idx_order_item_ingredients_order_item ON pizzeria_schema.order_item_ingredients (order_item_id);
CREATE INDEX idx_order_item_ingredients_ingredient ON pizzeria_schema.order_item_ingredients (ingredient_id);

-- 2. Modify order_items table: add custom fields and make product_id nullable
ALTER TABLE pizzeria_schema.order_items
    ADD COLUMN IF NOT EXISTS custom_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS custom_description TEXT;

-- Make product_id nullable (for custom pizzas without base product)
ALTER TABLE pizzeria_schema.order_items
    ALTER COLUMN product_id DROP NOT NULL;

-- Remove UNIQUE constraint on (order_id, product_id) to allow duplicate custom pizzas
ALTER TABLE pizzeria_schema.order_items
    DROP CONSTRAINT IF EXISTS order_items_order_id_product_id_key;

-- 3. Update trigger create_stock_use_on_preparing to account for custom ingredients
CREATE OR REPLACE FUNCTION pizzeria_schema.create_stock_use_on_preparing()
RETURNS TRIGGER AS $$
DECLARE
    v_order_employee_id BIGINT;
    v_product_id BIGINT;
    v_quantity BIGINT;
    v_order_item_id BIGINT;
BEGIN
    -- Check if status is changing to 'preparing'
    IF NEW.status = 'preparing' AND OLD.status IS DISTINCT FROM NEW.status THEN
        v_order_item_id := NEW.id;
        v_quantity := NEW.quantity;

        -- Get the employee who took the order
        SELECT employee_id INTO v_order_employee_id
        FROM pizzeria_schema.orders
        WHERE id = NEW.order_id;

        -- Check if there are custom ingredients in order_item_ingredients
        -- If yes, use them (for both custom and modified pizzas)
        -- If no, fall back to product_ingredients (for standard pizzas)
        IF EXISTS (SELECT 1 FROM pizzeria_schema.order_item_ingredients WHERE order_item_id = v_order_item_id) THEN
            -- Process custom ingredients from order_item_ingredients
            -- Include 'base' and 'added' ingredients (exclude 'removed')
            INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
            SELECT
                oii.ingredient_id,
                -(oii.quantity * v_quantity), -- Negative quantity for consumption
                'use',
                v_order_employee_id
            FROM pizzeria_schema.order_item_ingredients oii
            WHERE oii.order_item_id = v_order_item_id
              AND oii.modification_type IN ('base', 'added');
        ELSIF NEW.product_id IS NOT NULL THEN
            -- Fallback: use product ingredients if no custom ingredients exist
            v_product_id := NEW.product_id;

            -- Loop through all ingredients for the product
            INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
            SELECT
                pi.ingredient_id,
                -(pi.quantity * v_quantity), -- Negative quantity for consumption
                'use',
                v_order_employee_id
            FROM pizzeria_schema.product_ingredients pi
            WHERE pi.product_id = v_product_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

