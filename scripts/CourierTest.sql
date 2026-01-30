DO $$
DECLARE
    v_order_id INT;
    v_del_id INT;
BEGIN
    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('ready', 'delivery') RETURNING id INTO v_order_id;
    INSERT INTO pizzeria_schema.deliveries (order_id, status) VALUES (v_order_id, 'assigned') RETURNING id INTO v_del_id;
    
    SET ROLE pizzeria_courier_role;

    PERFORM * FROM pizzeria_schema.deliveries WHERE id = v_del_id;
    RAISE NOTICE '[COURIER] Read Delivery: SUCCESS';

    UPDATE pizzeria_schema.deliveries SET status = 'in_transit' WHERE id = v_del_id;
    RAISE NOTICE '[COURIER] Update Delivery: SUCCESS';

    BEGIN
        INSERT INTO pizzeria_schema.menus (name) VALUES ('Hacker Menu');
        RAISE EXCEPTION '[COURIER] FAIL: Courier created a menu!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[COURIER] Create Menu: BLOCKED (As Expected)';
    END;

    RESET ROLE;
END $$;