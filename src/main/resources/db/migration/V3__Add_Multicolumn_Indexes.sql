-- -----------------------------------------------------
-- OPTIMIZATION: Multicolumn Indexes for common query patterns
-- -----------------------------------------------------

-- 1. Orders: Frequent filtering by status AND date (e.g., "Kitchen Display System" showing today's new orders)
-- Helps query: WHERE status = 'new' ORDER BY placed_at DESC
CREATE INDEX idx_orders_status_placed_at
    ON pizzeria_schema.orders (status, placed_at);

-- 2. Products: Menu display usually filters by Menu ID AND Availability
-- Helps query: WHERE menu_id = 1 AND is_available = TRUE
CREATE INDEX idx_products_menu_availability
    ON pizzeria_schema.products (menu_id, is_available);

-- 3. Deliveries: Finding active deliveries for a specific courier
-- Helps query: WHERE courier_id = 5 AND status = 'in_transit'
CREATE INDEX idx_deliveries_courier_status
    ON pizzeria_schema.deliveries (courier_id, status);

-- 4. Inventory: Finding movements for an ingredient within a date range (Reporting)
-- Helps query: WHERE ingredient_id = 10 AND timestamp BETWEEN '...' AND '...'
CREATE INDEX idx_inventory_ingredient_time
    ON pizzeria_schema.inventory_movements (ingredient_id, "timestamp");

-- 5. Order Items: Finding specific items in an order by status (e.g., Chef checking what is left to cook)
-- Helps query: WHERE order_id = 100 AND status = 'preparing'
CREATE INDEX idx_order_items_order_status
    ON pizzeria_schema.order_items (order_id, status);