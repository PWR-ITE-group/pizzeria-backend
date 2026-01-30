DO $$
DECLARE
    v_manager_id INTEGER;
    v_courier_id INTEGER;
    v_order_id INTEGER;
    v_delivery_id INTEGER;

    v_view_row pizzeria_schema.order_full_info%ROWTYPE; 
BEGIN
    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
    VALUES ('Manager', 'One', '111', 'mgr_1', 'x', 'manager') 
    RETURNING id INTO v_manager_id;

    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
    VALUES ('Fast', 'Driver', '222', 'driver_1', 'x', 'courier') 
    RETURNING id INTO v_courier_id;

    INSERT INTO pizzeria_schema.orders (employee_id, status, order_type)
    VALUES (v_manager_id, 'in_delivery', 'delivery')
    RETURNING id INTO v_order_id;

    INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status, assigned_at)
    VALUES (v_order_id, v_courier_id, 'in_transit', NOW())
    RETURNING id INTO v_delivery_id;

    INSERT INTO pizzeria_schema.delivery_info (delivery_id, name, last_name, phone, city, street, house_nr)
    VALUES (v_delivery_id, 'Client', 'Kowalski', '500-600', 'Wroclaw', 'Dluga', '15');

    SELECT * INTO v_view_row
    FROM pizzeria_schema.order_full_info
    WHERE order_id = v_order_id;

    IF v_view_row.employee_name = 'Manager' AND      
       v_view_row.courier_last_name = 'Driver' AND   
       v_view_row.city = 'Wroclaw' AND               
       v_view_row.order_status = 'in_delivery'       
    THEN
        RAISE NOTICE 'TEST ORDER_FULL_INFO PASSED: All 5 tables joined successfully.';
    ELSE
        RAISE EXCEPTION 'TEST FAILED: Data mismatch. Courier: %, City: %, Manager: %', 
            v_view_row.courier_last_name, v_view_row.city, v_view_row.employee_name;
    END IF;

    ROLLBACK;
END $$;