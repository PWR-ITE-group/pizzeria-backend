DO $$
DECLARE
    v_count INTEGER;
BEGIN
    INSERT INTO pizzeria_schema.promotions (code, discount_percent, is_active, valid_from, valid_to) 
    VALUES ('ACTIVE_TEST', 10, TRUE, NOW() - INTERVAL '1 day', NOW() + INTERVAL '1 day');
    
    INSERT INTO pizzeria_schema.promotions (code, discount_percent, is_active) 
    VALUES ('OFF_TEST', 10, FALSE);

    INSERT INTO pizzeria_schema.promotions (code, discount_percent, is_active, valid_from, valid_to) 
    VALUES ('OLD_TEST', 10, TRUE, NOW() - INTERVAL '10 days', NOW() - INTERVAL '1 day');

    INSERT INTO pizzeria_schema.promotions (code, discount_percent, is_active, valid_from, valid_to) 
    VALUES ('FUTURE_TEST', 10, TRUE, NOW() + INTERVAL '1 day', NOW() + INTERVAL '10 days');

    SELECT COUNT(*) INTO v_count 
    FROM pizzeria_schema.active_promotions 
    WHERE code LIKE '%_TEST';

    IF v_count = 1 THEN
        RAISE NOTICE 'TEST 2 PASSED: Only valid active promotions are visible.';
    ELSE
        RAISE EXCEPTION 'TEST 2 FAILED: Expected 1 active promo, found %', v_count;
    END IF;

    ROLLBACK;
END $$;