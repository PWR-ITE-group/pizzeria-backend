DO $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
    v_emp_id INT;
    v_menu_id INT;
    v_prod_id INT;
    v_rows_to_insert CONSTANT INT := 100;
BEGIN
    SELECT id INTO v_emp_id FROM pizzeria_schema.employees LIMIT 1;
    SELECT id INTO v_prod_id FROM pizzeria_schema.products LIMIT 1;

    RAISE NOTICE '--- TEST 1: INSERT PERFORMANCE (% rows) ---', v_rows_to_insert;

    v_start_time := clock_timestamp();

    WITH new_orders AS (
        INSERT INTO pizzeria_schema.orders (employee_id, status, order_type, placed_at)
        SELECT 
            v_emp_id, 
            'new', 
            'dine_in', 
            NOW()
        FROM generate_series(1, v_rows_to_insert)
        RETURNING id
    )
    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
    SELECT 
        id, 
        v_prod_id, 
        2, 
        50.00, 
        'pending'
    FROM new_orders;

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'INSERT Finished.';
    RAISE NOTICE 'Total Time: %', v_duration;
    RAISE NOTICE 'Avg Time per Order: % ms', (EXTRACT(EPOCH FROM v_duration) * 1000) / v_rows_to_insert;
    RAISE NOTICE '---------------------------------------------';
END $$;