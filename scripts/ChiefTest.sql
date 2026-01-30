DO $$
DECLARE
    v_order_id INT;
    v_ing_id   INT;
    v_emp_id   INT;
BEGIN
    
    SELECT id INTO v_order_id FROM pizzeria_schema.orders LIMIT 1;
    
    SELECT id INTO v_ing_id FROM pizzeria_schema.ingredients LIMIT 1;
    

    SELECT id INTO v_emp_id FROM pizzeria_schema.employees WHERE role = 'chef' LIMIT 1;
    
    IF v_emp_id IS NULL THEN
        SELECT id INTO v_emp_id FROM pizzeria_schema.employees LIMIT 1;
    END IF;


    SET ROLE pizzeria_chef_role;

    UPDATE pizzeria_schema.orders 
    SET status = 'ready' 
    WHERE id = v_order_id;
    
    RAISE NOTICE '[CHEF] Update Order Status: SUCCESS';

    INSERT INTO pizzeria_schema.inventory_movements 
        (ingredient_id, quantity_change, movement_type, employee_id, "timestamp")
    VALUES 
        (v_ing_id, -2.50, 'use', v_emp_id, NOW());
        
    RAISE NOTICE '[CHEF] Insert Inventory Movement: SUCCESS';

    BEGIN
        UPDATE pizzeria_schema.orders 
        SET order_type = 'delivery' 
        WHERE id = v_order_id;
        
        RAISE EXCEPTION '[CHEF] FAIL: Chef updated order_type (Column Security failed)!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[CHEF] Update Order Type: BLOCKED (As Expected)';
    END;

    BEGIN
        PERFORM * FROM pizzeria_schema.payments;
        RAISE EXCEPTION '[CHEF] FAIL: Chef saw payments!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[CHEF] Read Payments: BLOCKED (As Expected)';
    END;

    RESET ROLE;
END $$;