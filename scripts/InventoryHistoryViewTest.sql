DO $$
DECLARE
    v_ing_id INTEGER;
    v_emp_id INTEGER;
    
    v_res_ing_name TEXT;
    v_res_emp_name TEXT;
    v_res_qty NUMERIC;
BEGIN
    INSERT INTO pizzeria_schema.ingredients (name, unit, stock_quantity) 
    VALUES ('Test Salami', 'kg', 10.00) 
    RETURNING id INTO v_ing_id;

    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role) 
    VALUES ('Storage', 'Master', '999111222', 'store_manager', 'hash', 'manager')
    ON CONFLICT (login) DO UPDATE SET id = pizzeria_schema.employees.id -- защита от дублей если запускаешь много раз
    RETURNING id INTO v_emp_id;

    INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, employee_id, quantity_change, movement_type)
    VALUES (v_ing_id, v_emp_id, 5.50, 'restock');

    SELECT ingredient_name, employee_name, quantity_change 
    INTO v_res_ing_name, v_res_emp_name, v_res_qty
    FROM pizzeria_schema.inventory_history_view
    WHERE ingredient_id = v_ing_id
    ORDER BY timestamp DESC LIMIT 1;

    IF v_res_ing_name = 'Test Salami' AND v_res_emp_name = 'Storage' AND v_res_qty = 5.50 THEN
        RAISE NOTICE 'TEST INVENTORY_HISTORY PASSED: Joins works correctly.';
    ELSE
        RAISE EXCEPTION 'TEST FAILED: Expected Salami/Storage/5.50, got %/%/%', v_res_ing_name, v_res_emp_name, v_res_qty;
    END IF;

    ROLLBACK;
END $$;