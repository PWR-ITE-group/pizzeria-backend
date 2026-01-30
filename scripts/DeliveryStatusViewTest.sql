DO $$
DECLARE
    v_city VARCHAR;
    v_courier VARCHAR;
BEGIN
    WITH 
      emp AS (INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role) VALUES ('Speedy', 'Gonzales', '777', 'speedy', 'x', 'courier') RETURNING id),
      ord AS (INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('in_delivery', 'delivery') RETURNING id),
      del AS (INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status) SELECT ord.id, emp.id, 'in_transit' FROM ord, emp RETURNING id)
    
    INSERT INTO pizzeria_schema.delivery_info (delivery_id, city, street, name, last_name, phone)
    SELECT del.id, 'TestCity', 'TestStreet', 'Client', 'K', '123' FROM del;

    SELECT city, courier_name INTO v_city, v_courier
    FROM pizzeria_schema.delivery_status_view
    WHERE city = 'TestCity';

    IF v_city = 'TestCity' AND v_courier = 'Speedy' THEN
        RAISE NOTICE 'TEST 5 PASSED: Joins works correctly across all tables.';
    ELSE
        RAISE EXCEPTION 'TEST 5 FAILED: Data mismatch. City: %, Courier: %', v_city, v_courier;
    END IF;

    ROLLBACK;
END $$;