DO $$
DECLARE
    v_menu_id INT;
    v_prod_id INT;
    v_order_id INT;
    v_total NUMERIC;
BEGIN
    INSERT INTO pizzeria_schema.menus (name) VALUES ('Total Test Menu') RETURNING id INTO v_menu_id;
    INSERT INTO pizzeria_schema.products (menu_id, name, base_price) VALUES (v_menu_id, 'Math Pizza', 50.00) RETURNING id INTO v_prod_id;
    
    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('new', 'pickup') RETURNING id INTO v_order_id;

    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
    VALUES (v_order_id, v_prod_id, 2, 50.00, 'pending');

    SELECT total_price INTO v_total FROM pizzeria_schema.orders WHERE id = v_order_id;
    
    IF v_total <> 100.00 THEN 
        RAISE EXCEPTION 'STEP A FAILED: Expected 100.00, got %', v_total; 
    END IF;

    UPDATE pizzeria_schema.order_items SET quantity = 3 WHERE order_id = v_order_id AND product_id = v_prod_id;
    
    SELECT total_price INTO v_total FROM pizzeria_schema.orders WHERE id = v_order_id;
    
    IF v_total <> 150.00 THEN 
        RAISE EXCEPTION 'STEP B FAILED: Expected 150.00, got %', v_total; 
    END IF;

    DELETE FROM pizzeria_schema.order_items WHERE order_id = v_order_id;

    SELECT total_price INTO v_total FROM pizzeria_schema.orders WHERE id = v_order_id;
    
    IF v_total <> 0.00 THEN 
        RAISE EXCEPTION 'STEP C FAILED: Expected 0.00, got %', v_total; 
    END IF;

    RAISE NOTICE 'TEST 2 PASSED: Order total recalculates on INSERT, UPDATE, and DELETE.';
    ROLLBACK;
END $$;