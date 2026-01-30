DO $$
BEGIN
    SET ROLE pizzeria_manager_role;

    UPDATE pizzeria_schema.products SET base_price = 20.00 WHERE id = 1;
    RAISE NOTICE '[MANAGER] Update Product Price: SUCCESS';

    PERFORM * FROM pizzeria_schema.employees;
    RAISE NOTICE '[MANAGER] Read Employees: SUCCESS';

    BEGIN
        DELETE FROM pizzeria_schema.orders WHERE id = (SELECT id FROM pizzeria_schema.orders LIMIT 1);
        RAISE EXCEPTION '[MANAGER] FAIL: Manager deleted an order (Constraint check failed)!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[MANAGER] Delete Order: BLOCKED (As Expected - Read Only Access)';
    END;

    RESET ROLE;
END $$;