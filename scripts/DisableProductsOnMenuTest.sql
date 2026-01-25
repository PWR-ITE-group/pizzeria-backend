DO $$
DECLARE
    v_menu_id INT;
    v_prod_active_count INT;
BEGIN
    INSERT INTO pizzeria_schema.menus (name, is_active) VALUES ('Summer Menu', TRUE) RETURNING id INTO v_menu_id;
    
    INSERT INTO pizzeria_schema.products (menu_id, name, base_price, is_available) VALUES 
    (v_menu_id, 'P1', 10, TRUE),
    (v_menu_id, 'P2', 10, TRUE),
    (v_menu_id, 'P3', 10, TRUE);

    UPDATE pizzeria_schema.menus SET is_active = FALSE WHERE id = v_menu_id;

    SELECT count(*) INTO v_prod_active_count 
    FROM pizzeria_schema.products 
    WHERE menu_id = v_menu_id AND is_available = TRUE;

    IF v_prod_active_count = 0 THEN
        RAISE NOTICE 'TEST 5 PASSED: All products disabled after menu close.';
    ELSE
        RAISE EXCEPTION 'TEST 5 FAILED: % products are still active!', v_prod_active_count;
    END IF;

    ROLLBACK;
END $$;