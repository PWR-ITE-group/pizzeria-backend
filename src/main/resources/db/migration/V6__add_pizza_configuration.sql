
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_item_ingredients
(
    id                  BIGSERIAL PRIMARY KEY,
    order_item_id       BIGINT NOT NULL REFERENCES pizzeria_schema.order_items (id) ON DELETE CASCADE,
    ingredient_id       BIGINT NOT NULL REFERENCES pizzeria_schema.ingredients (id) ON DELETE RESTRICT,
    quantity            NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    modification_type   VARCHAR(20) NOT NULL CHECK (modification_type IN ('base', 'added', 'removed'))
);

CREATE INDEX idx_order_item_ingredients_order_item ON pizzeria_schema.order_item_ingredients (order_item_id);
CREATE INDEX idx_order_item_ingredients_ingredient ON pizzeria_schema.order_item_ingredients (ingredient_id);

ALTER TABLE pizzeria_schema.order_items
    ADD COLUMN IF NOT EXISTS custom_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS custom_description TEXT;

ALTER TABLE pizzeria_schema.order_items
    ALTER COLUMN product_id DROP NOT NULL;

ALTER TABLE pizzeria_schema.order_items
    DROP CONSTRAINT IF EXISTS order_items_order_id_product_id_key;

CREATE OR REPLACE FUNCTION pizzeria_schema.create_stock_use_on_preparing()
RETURNS TRIGGER AS $$
DECLARE
    v_order_employee_id BIGINT;
    v_product_id BIGINT;
    v_quantity BIGINT;
    v_order_item_id BIGINT;
BEGIN
    IF NEW.status = 'preparing' AND OLD.status IS DISTINCT FROM NEW.status THEN
        v_order_item_id := NEW.id;
        v_quantity := NEW.quantity;

        SELECT employee_id INTO v_order_employee_id
        FROM pizzeria_schema.orders
        WHERE id = NEW.order_id;


        IF EXISTS (SELECT 1 FROM pizzeria_schema.order_item_ingredients WHERE order_item_id = v_order_item_id) THEN
            INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
            SELECT
                oii.ingredient_id,
                -(oii.quantity * v_quantity),
                'use',
                v_order_employee_id
            FROM pizzeria_schema.order_item_ingredients oii
            WHERE oii.order_item_id = v_order_item_id
              AND oii.modification_type IN ('base', 'added');
        ELSIF NEW.product_id IS NOT NULL THEN
            v_product_id := NEW.product_id;

            INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
            SELECT
                pi.ingredient_id,
                -(pi.quantity * v_quantity),
                'use',
                v_order_employee_id
            FROM pizzeria_schema.product_ingredients pi
            WHERE pi.product_id = v_product_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

