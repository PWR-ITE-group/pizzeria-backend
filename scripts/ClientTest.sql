DO $$
BEGIN
    SET ROLE pizzeria_client_role;

    PERFORM * FROM pizzeria_schema.products;
    RAISE NOTICE '[CLIENT] Read Products: SUCCESS';

    INSERT INTO pizzeria_schema.orders (status, order_type) VALUES ('new', 'pickup');
    RAISE NOTICE '[CLIENT] Create Order: SUCCESS';

    BEGIN
        UPDATE pizzeria_schema.products SET base_price = 0 WHERE id = 1;
        RAISE EXCEPTION '[CLIENT] FAIL: Client was able to update price!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[CLIENT] Update Price: BLOCKED (As Expected)';
    END;

    BEGIN
        PERFORM * FROM pizzeria_schema.employees;
        RAISE EXCEPTION '[CLIENT] FAIL: Client was able to read employees!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[CLIENT] Read Employees: BLOCKED (As Expected)';
    END;

    RESET ROLE;
END $$;