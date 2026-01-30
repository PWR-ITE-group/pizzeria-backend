
EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.orders
WHERE status = 'new'
ORDER BY placed_at ASC
LIMIT 20;

EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.orders
ORDER BY placed_at DESC
LIMIT 20;


EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.orders
WHERE employee_id = 10 
  AND status NOT IN ('delivered', 'cancelled', 'failed');


EXPLAIN ANALYZE
SELECT name, base_price, image_url 
FROM pizzeria_schema.products
WHERE menu_id = 1 AND is_available = true;


EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.deliveries
WHERE courier_id = 20 AND status = 'assigned'
ORDER BY assigned_at ASC;


EXPLAIN ANALYZE
SELECT movement_type, quantity_change, timestamp
FROM pizzeria_schema.inventory_movements
WHERE ingredient_id = 5
ORDER BY timestamp DESC
LIMIT 50;


EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.delivery_info
WHERE phone = '555-12345';

EXPLAIN ANALYZE
SELECT * FROM pizzeria_schema.delivery_info
WHERE lower(last_name) = 'smith100';