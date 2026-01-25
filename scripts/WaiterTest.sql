DO $$
BEGIN
    SET ROLE pizzeria_waiter_role;

    UPDATE pizzeria_schema.orders SET status = 'preparing' WHERE id = (SELECT id FROM pizzeria_schema.orders LIMIT 1);
    RAISE NOTICE '[WAITER] Update Order: SUCCESS';

    PERFORM * FROM pizzeria_schema.payments;
    RAISE NOTICE '[WAITER] Read Payments: SUCCESS';

    BEGIN
        DELETE FROM pizzeria_schema.products WHERE id = 1;
        RAISE EXCEPTION '[WAITER] FAIL: Waiter was able to delete product!';
    EXCEPTION WHEN insufficient_privilege THEN
        RAISE NOTICE '[WAITER] Delete Product: BLOCKED (As Expected)';
    END;

    RESET ROLE;
END $$;