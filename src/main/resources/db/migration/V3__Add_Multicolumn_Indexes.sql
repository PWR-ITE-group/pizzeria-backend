
DROP INDEX IF EXISTS pizzeria_schema.idx_orders_status;
DROP INDEX IF EXISTS pizzeria_schema.idx_products_menu;
DROP INDEX IF EXISTS pizzeria_schema.idx_inventory_ingredient;


CREATE INDEX idx_orders_kitchen_queue
    ON pizzeria_schema.orders (status, placed_at ASC);

CREATE INDEX idx_orders_history_latest
    ON pizzeria_schema.orders (placed_at DESC);

CREATE INDEX idx_orders_employee_active
    ON pizzeria_schema.orders (employee_id, status)
    WHERE status NOT IN ('delivered', 'cancelled', 'failed');

CREATE INDEX idx_products_menu_covering
    ON pizzeria_schema.products (menu_id, is_available)
    INCLUDE (name, base_price, image_url);


CREATE INDEX idx_deliveries_courier_tasks
    ON pizzeria_schema.deliveries (courier_id, status, assigned_at ASC);


CREATE INDEX idx_inventory_ingredient_history
    ON pizzeria_schema.inventory_movements (ingredient_id, "timestamp" DESC)
    INCLUDE (quantity_change, movement_type);


CREATE INDEX idx_delivery_info_phone
    ON pizzeria_schema.delivery_info (phone);

CREATE INDEX idx_delivery_info_lastname_lower
    ON pizzeria_schema.delivery_info (lower(last_name));