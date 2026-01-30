
CREATE OR REPLACE VIEW pizzeria_schema.order_full_info AS
SELECT
    o.id AS order_id,
    o.status AS order_status,
    o.order_type,
    o.placed_at,
    o.updated_at,
    e.name AS employee_name,
    e.last_name AS employee_last_name,
    d.id AS delivery_id,
    c.name AS courier_name,
    c.last_name AS courier_last_name,
    di.phone AS customer_phone,
    di.street,
    di.house_nr,
    di.city,
    di.postal_code,
    di.additional_info
FROM pizzeria_schema.orders o
         LEFT JOIN pizzeria_schema.employees e ON o.employee_id = e.id
         LEFT JOIN pizzeria_schema.deliveries d ON d.order_id = o.id
         LEFT JOIN pizzeria_schema.employees c ON d.courier_id = c.id
         LEFT JOIN pizzeria_schema.delivery_info di ON di.delivery_id = d.id;



CREATE OR REPLACE VIEW pizzeria_schema.order_items_view AS
SELECT
    oi.order_id,
    oi.product_id,
    p.name AS product_name,
    oi.quantity,
    oi.unit_price,
    (oi.quantity * oi.unit_price) AS total_price,
    oi.status
FROM pizzeria_schema.order_items oi
         JOIN pizzeria_schema.products p ON p.id = oi.product_id;

CREATE OR REPLACE VIEW pizzeria_schema.active_promotions AS
SELECT *
FROM pizzeria_schema.promotions
WHERE is_active = TRUE
  AND (valid_from IS NULL OR valid_from <= CURRENT_DATE)
  AND (valid_to   IS NULL OR valid_to   >= CURRENT_DATE);

CREATE OR REPLACE VIEW pizzeria_schema.ingredient_stock_view AS
SELECT
    i.id,
    i.name,
    i.unit,
    i.stock_quantity,
    CASE
        WHEN i.stock_quantity < 10 THEN 'LOW'
        ELSE 'OK'
        END AS stock_status
FROM pizzeria_schema.ingredients i;

CREATE OR REPLACE VIEW pizzeria_schema.inventory_history_view AS
SELECT
    m.id,
    m.ingredient_id,
    i.name AS ingredient_name,
    m.quantity_change,
    m.movement_type,
    m.timestamp,
    e.name AS employee_name,
    e.last_name AS employee_last_name
FROM pizzeria_schema.inventory_movements m
         JOIN pizzeria_schema.ingredients i ON i.id = m.ingredient_id
         LEFT JOIN pizzeria_schema.employees e ON e.id = m.employee_id;


CREATE OR REPLACE VIEW pizzeria_schema.product_sales_view AS
SELECT
    p.id AS product_id,
    p.name,
    COALESCE(SUM(oi.quantity), 0) AS total_sold,
    COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_revenue
FROM pizzeria_schema.products p
         LEFT JOIN pizzeria_schema.order_items oi ON p.id = oi.product_id
GROUP BY p.id, p.name
ORDER BY total_sold DESC;


CREATE OR REPLACE VIEW pizzeria_schema.delivery_status_view AS
SELECT
    d.id AS delivery_id,
    d.status,
    d.assigned_at,
    d.delivered_at,
    o.id AS order_id,
    o.status AS order_status,
    o.placed_at,
    c.name AS courier_name,
    c.last_name AS courier_last_name,
    di.city,
    di.street,
    di.house_nr
FROM pizzeria_schema.deliveries d
         JOIN pizzeria_schema.orders o ON o.id = d.order_id
         LEFT JOIN pizzeria_schema.employees c ON c.id = d.courier_id
         LEFT JOIN pizzeria_schema.delivery_info di ON di.delivery_id = d.id;
