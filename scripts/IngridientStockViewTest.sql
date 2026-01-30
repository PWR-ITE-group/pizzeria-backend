DO $$
DECLARE
    v_low_id INTEGER;
    v_ok_id INTEGER;
    v_status_low TEXT;
    v_status_ok TEXT;
BEGIN
    INSERT INTO pizzeria_schema.ingredients (name, unit, stock_quantity) VALUES ('Test Low Stock', 'kg', 5.00) RETURNING id INTO v_low_id;
    INSERT INTO pizzeria_schema.ingredients (name, unit, stock_quantity) VALUES ('Test High Stock', 'kg', 50.00) RETURNING id INTO v_ok_id;

    SELECT stock_status INTO v_status_low FROM pizzeria_schema.ingredient_stock_view WHERE id = v_low_id;
    SELECT stock_status INTO v_status_ok  FROM pizzeria_schema.ingredient_stock_view WHERE id = v_ok_id;

    IF v_status_low = 'LOW' AND v_status_ok = 'OK' THEN
        RAISE NOTICE 'TEST 1 PASSED: Stock statuses calculated correctly.';
    ELSE
        RAISE EXCEPTION 'TEST 1 FAILED: Expected LOW/OK, got %/%', v_status_low, v_status_ok;
    END IF;

    ROLLBACK;
END $$;