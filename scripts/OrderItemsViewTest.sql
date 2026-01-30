DO $$
DECLARE
    v_total NUMERIC;
BEGIN
    
    WITH new_menu AS (INSERT INTO pizzeria_schema.menus (name) VALUES ('M') RETURNING id),
         new_prod AS (INSERT INTO pizzeria_schema.products (name, base_price, menu_id) SELECT 'Math Pizza', 50.00, id FROM new_menu RETURNING id),
         new_order AS (INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('new', 'pickup') RETURNING id)
    
    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
    SELECT new_order.id, new_prod.id, 3, 15.50, 'new' -- 3 * 15.50 = 46.50
    FROM new_order, new_prod;

    SELECT total_price INTO v_total 
    FROM pizzeria_schema.order_items_view 
    WHERE product_name = 'Math Pizza' LIMIT 1;

    IF v_total = 46.50 THEN
        RAISE NOTICE 'TEST 4 PASSED: Math calculation (3 * 15.50 = 46.50) is correct.';
    ELSE
        RAISE EXCEPTION 'TEST 4 FAILED: Expected 46.50, got %', v_total;
    END IF;

    ROLLBACK;
END $$;