DO $$
DECLARE
    v_menu_id INT;
    v_prod_id INT;
    v_log_count INT;
    v_old_price NUMERIC;
    v_new_price NUMERIC;
BEGIN
    INSERT INTO pizzeria_schema.menus (name) VALUES ('Price Test Menu') RETURNING id INTO v_menu_id;
    INSERT INTO pizzeria_schema.products (menu_id, name, base_price) 
    VALUES (v_menu_id, 'Audit Pizza', 100.00) RETURNING id INTO v_prod_id;

    UPDATE pizzeria_schema.products SET base_price = 150.00 WHERE id = v_prod_id;

    SELECT old_price, new_price INTO v_old_price, v_new_price
    FROM pizzeria_schema.price_history_log
    WHERE product_id = v_prod_id
    ORDER BY changed_at DESC LIMIT 1;

    IF v_old_price = 100.00 AND v_new_price = 150.00 THEN
        RAISE NOTICE 'TEST 1 PASSED: Price change logged successfully (100 -> 150).';
    ELSE
        RAISE EXCEPTION 'TEST 1 FAILED: Expected 100->150, got %->%', v_old_price, v_new_price;
    END IF;

    ROLLBACK;
END $$;