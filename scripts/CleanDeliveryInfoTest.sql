DO $$
DECLARE
    v_order_id INT;
    v_del_id INT;
    v_city TEXT;
    v_email TEXT;
BEGIN
    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('new', 'delivery') RETURNING id INTO v_order_id;
    INSERT INTO pizzeria_schema.deliveries (order_id, status) VALUES (v_order_id, 'assigned') RETURNING id INTO v_del_id;

    INSERT INTO pizzeria_schema.delivery_info (delivery_id, name, last_name, phone, city, street, email)
    VALUES (v_del_id, 'Jan', 'Kowalski', '123', '  Wroclaw  ', 'Main', '  MY@EMAIL.COM  ');

    SELECT city, email INTO v_city, v_email FROM pizzeria_schema.delivery_info WHERE delivery_id = v_del_id;

    IF v_city = 'Wroclaw' AND v_email = 'my@email.com' THEN
        RAISE NOTICE 'TEST 4 PASSED: Data cleaned (trimmed and lowercased).';
    ELSE
        RAISE EXCEPTION 'TEST 4 FAILED: Got City="%", Email="%"', v_city, v_email;
    END IF;

    ROLLBACK;
END $$;