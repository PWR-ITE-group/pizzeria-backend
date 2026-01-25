

DO $$
DECLARE
    v_emp_id INTEGER;
    v_menu_id INTEGER;
    v_prod_id INTEGER;
    start_time TIMESTAMP;
    end_time TIMESTAMP;
    duration INTERVAL;
    orders_count CONSTANT INTEGER := 10000;
BEGIN
    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
    VALUES ('Perf', 'Tester', '999999999', 'perf_bot', 'hash', 'manager') 
    ON CONFLICT (login) DO UPDATE SET name = EXCLUDED.name 
    RETURNING id INTO v_emp_id;

    INSERT INTO pizzeria_schema.menus (name) VALUES ('Perf Menu') RETURNING id INTO v_menu_id;
    
    INSERT INTO pizzeria_schema.products (menu_id, name, base_price) 
    VALUES (v_menu_id, 'Perf Pizza', 15.00) RETURNING id INTO v_prod_id;

    start_time := clock_timestamp();

    WITH new_orders AS (
        INSERT INTO pizzeria_schema.orders (employee_id, status, order_type, placed_at)
        SELECT 
            v_emp_id, 
            'new', 
            CASE WHEN i % 2 = 0 THEN 'delivery' ELSE 'pickup' END, 
            NOW() - (i || ' minutes')::INTERVAL 
        FROM generate_series(1, orders_count) AS i
        RETURNING id
    )

    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
    SELECT 
        id, 
        v_prod_id, 
        (random() * 5 + 1)::INTEGER, 
        15.00, 
        'pending'
    FROM new_orders;

    end_time := clock_timestamp();
    duration := end_time - start_time;

    RAISE NOTICE '--------------------------------------------------';
    RAISE NOTICE 'BULK INSERT REPORT:';
    RAISE NOTICE 'Orders Created: %', orders_count;
    RAISE NOTICE 'Items Created:  %', orders_count; 
    RAISE NOTICE 'Total Time:     %', duration;
    RAISE NOTICE 'Rows per sec:   %', orders_count / EXTRACT(EPOCH FROM duration);
    RAISE NOTICE '--------------------------------------------------';

END $$;