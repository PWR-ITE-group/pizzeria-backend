DO $$
DECLARE
    v_prod_sold_id INTEGER;
    v_prod_empty_id INTEGER;
    v_order_id_1 INTEGER; 
    v_order_id_2 INTEGER;
    v_sold_count BIGINT;
    v_revenue NUMERIC;
    v_empty_count BIGINT;
BEGIN
    INSERT INTO pizzeria_schema.menus (name) VALUES ('Test Menu');
    INSERT INTO pizzeria_schema.products (name, base_price) VALUES ('Sold Pizza', 10.00) RETURNING id INTO v_prod_sold_id;
    INSERT INTO pizzeria_schema.products (name, base_price) VALUES ('Sad Pizza', 100.00) RETURNING id INTO v_prod_empty_id;

    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('done', 'pickup') RETURNING id INTO v_order_id_1;
    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('done', 'delivery') RETURNING id INTO v_order_id_2; -- Второй заказ
    
    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status) 
    VALUES (v_order_id_1, v_prod_sold_id, 2, 10.00, 'done');
    
    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status) 
    VALUES (v_order_id_2, v_prod_sold_id, 3, 10.00, 'done');

    SELECT total_sold, total_revenue INTO v_sold_count, v_revenue 
    FROM pizzeria_schema.product_sales_view WHERE product_id = v_prod_sold_id;

    SELECT total_sold INTO v_empty_count 
    FROM pizzeria_schema.product_sales_view WHERE product_id = v_prod_empty_id;

    IF v_sold_count = 5 AND v_revenue = 50.00 AND v_empty_count = 0 THEN
        RAISE NOTICE 'TEST 3 PASSED: Aggregation and ZERO handling correct.';
    ELSE
        RAISE EXCEPTION 'TEST 3 FAILED: Sold: % (exp 5), Rev: % (exp 50.00), Empty: % (exp 0)', v_sold_count, v_revenue, v_empty_count;
    END IF;

    ROLLBACK;
END $$;