DO $$
DECLARE
    v_courier_id INT;
    v_order_id INT;
    v_del_id INT;
    v_count INT;
BEGIN
    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role, completed_orders_count)
    VALUES ('Flash', 'Gordon', '999', 'flash', 'hash', 'courier', 0) RETURNING id INTO v_courier_id;

    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('in_delivery', 'delivery') RETURNING id INTO v_order_id;
    
    INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status) 
    VALUES (v_order_id, v_courier_id, 'in_transit') RETURNING id INTO v_del_id;

    UPDATE pizzeria_schema.deliveries SET status = 'delivered' WHERE id = v_del_id;

    SELECT completed_orders_count INTO v_count FROM pizzeria_schema.employees WHERE id = v_courier_id;

    IF v_count = 1 THEN
        RAISE NOTICE 'TEST 3 PASSED: Courier XP boosted successfully.';
    ELSE
        RAISE EXCEPTION 'TEST 3 FAILED: Expected count 1, got %', v_count;
    END IF;

    ROLLBACK;
END $$;