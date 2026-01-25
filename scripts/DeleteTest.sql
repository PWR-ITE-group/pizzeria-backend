DO $$
DECLARE
    v_start_time TIMESTAMP;
    v_end_time TIMESTAMP;
    v_duration INTERVAL;
    v_rows_to_delete CONSTANT INT := 100; 
    v_rows_actual INT; 
BEGIN
    RAISE NOTICE '--- TEST 3: DELETE PERFORMANCE (Cascade Delete) ---';

    v_start_time := clock_timestamp();

    DELETE FROM pizzeria_schema.orders
    WHERE id IN (
        SELECT id FROM pizzeria_schema.orders 
        ORDER BY placed_at ASC 
        LIMIT v_rows_to_delete
    );
    
    GET DIAGNOSTICS v_rows_actual = ROW_COUNT;

    v_end_time := clock_timestamp();
    v_duration := v_end_time - v_start_time;

    RAISE NOTICE 'DELETE Finished.';
    RAISE NOTICE 'Requested to delete: %', v_rows_to_delete;
    RAISE NOTICE 'ACTUALLY deleted: %', v_rows_actual; 
    RAISE NOTICE 'Total Time: %', v_duration;
    
    IF v_rows_actual > 0 THEN
        RAISE NOTICE 'Avg Time per Delete: % ms', (EXTRACT(EPOCH FROM v_duration) * 1000) / v_rows_actual;
    ELSE
        RAISE NOTICE 'Avg Time: N/A (Nothing was deleted)';
    END IF;
    RAISE NOTICE '---------------------------------------------';
END $$;