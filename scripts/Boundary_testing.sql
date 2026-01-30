
DO $$
DECLARE
    v_menu_id INT;
    v_prod_id INT;
    v_order_id INT;
    v_emp_id INT;
BEGIN
    RAISE NOTICE '--- STARTING BOUNDARY TESTS ---';

    INSERT INTO pizzeria_schema.menus (name) VALUES ('Boundary Menu') RETURNING id INTO v_menu_id;
    INSERT INTO pizzeria_schema.products (menu_id, name, base_price) VALUES (v_menu_id, 'Valid Pizza', 50.00) RETURNING id INTO v_prod_id;
    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role) 
    VALUES ('B_Test', 'User', '000-000', 'unique_login_base', 'hash', 'waiter') RETURNING id INTO v_emp_id;
    INSERT INTO pizzeria_schema.orders (employee_id, status, order_type) VALUES (v_emp_id, 'new', 'dine_in') RETURNING id INTO v_order_id;


    RAISE NOTICE 'TEST 1: Duplicate Login...';
    BEGIN
        INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
        VALUES ('Clone', 'User', '111-111', 'unique_login_base', 'hash', 'chef');
        
        RAISE EXCEPTION 'FAILED: DB allowed duplicate login!';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: Duplicate login rejected.';
    END;


    RAISE NOTICE 'TEST 2: Duplicate Phone...';
    BEGIN
        INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
        VALUES ('Clone', 'Phone', '000-000', 'other_login', 'hash', 'chef');
        
        RAISE EXCEPTION 'FAILED: DB allowed duplicate phone!';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: Duplicate phone rejected.';
    END;


    RAISE NOTICE 'TEST 3: Negative Quantity in Order Items...';
    BEGIN
        INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
        VALUES (v_order_id, v_prod_id, -5, 10.00, 'pending');
        
        RAISE EXCEPTION 'FAILED: DB allowed negative quantity!';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: Negative quantity rejected.';
    END;


    RAISE NOTICE 'TEST 4: Promotion Discount > 100 percent';
    BEGIN
        INSERT INTO pizzeria_schema.promotions (code, discount_percent)
        VALUES ('SUPER_FAIL', 150.00);
        
        RAISE EXCEPTION 'FAILED: DB allowed discount > 100%%!';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: Invalid discount rejected.';
    END;


    RAISE NOTICE 'TEST 5: Varchar Limit Exceeded...';
    BEGIN
        INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
        VALUES ('Long', 'Phone', '123456789012345678901234567890123456789012345678901234567890', 'login_long', 'hash', 'courier');
        
        RAISE EXCEPTION 'FAILED: DB allowed string longer than VARCHAR limit!';
    EXCEPTION WHEN string_data_right_truncation THEN
        RAISE NOTICE 'PASSED: Long string rejected.';
    END;


    RAISE NOTICE 'TEST 6: Negative Payment Amount...';
    BEGIN
        INSERT INTO pizzeria_schema.payments (order_id, amount, method, status)
        VALUES (v_order_id, -50.00, 'cash', 'pending');
        
        RAISE EXCEPTION 'FAILED: DB allowed negative payment!';
    EXCEPTION WHEN check_violation THEN
        RAISE NOTICE 'PASSED: Negative payment rejected.';
    END;


    RAISE NOTICE 'TEST 7: Duplicate Product in Same Order...';
    BEGIN
        INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
        VALUES (v_order_id, v_prod_id, 1, 50.00, 'pending');

        INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
        VALUES (v_order_id, v_prod_id, 2, 50.00, 'pending');
        
        RAISE EXCEPTION 'FAILED: DB allowed duplicate product in one order!';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: Duplicate product entry rejected.';
    END;


    RAISE NOTICE 'TEST 8: Two Deliveries for One Order...';
    DECLARE
        v_courier_id INT;
    BEGIN
        INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role) 
        VALUES ('C', 'C', '999', 'courier_unique', 'h', 'courier') RETURNING id INTO v_courier_id;

        INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status)
        VALUES (v_order_id, v_courier_id, 'assigned');

        INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status)
        VALUES (v_order_id, v_courier_id, 'assigned');
        
        RAISE EXCEPTION 'FAILED: DB allowed multiple deliveries for one order!';
    EXCEPTION WHEN unique_violation THEN
        RAISE NOTICE 'PASSED: Second delivery rejected.';
    END;

    ROLLBACK;
    RAISE NOTICE '--- ALL BOUNDARY TESTS COMPLETED ---';
END $$;