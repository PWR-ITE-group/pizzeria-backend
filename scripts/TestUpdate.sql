DO $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
    v_rows_to_update CONSTANT INT := 100;
BEGIN
    RAISE NOTICE '--- TEST 2: UPDATE PERFORMANCE (Trigger: Price Audit) ---';

    v_start_time := clock_timestamp();

    UPDATE pizzeria_schema.products
    SET base_price = base_price + 1.50
    WHERE id IN (
        SELECT id FROM pizzeria_schema.products 
        LIMIT v_rows_to_update
    );

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'UPDATE Finished (% rows affected).', v_rows_to_update;
    RAISE NOTICE 'Total Time: %', v_duration;
    RAISE NOTICE 'Avg Time per Update: % ms', (EXTRACT(EPOCH FROM v_duration) * 1000) / v_rows_to_update;
    RAISE NOTICE '---------------------------------------------';
END $$;