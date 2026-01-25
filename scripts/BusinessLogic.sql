-- =============================================
-- TEST SUITE 2: BUSINESS LOGIC & TRIGGERS
-- =============================================

DO $$
DECLARE
    v_ing_id INTEGER;
    v_prod_id INTEGER;
    v_order_id INTEGER;
    v_start_stock NUMERIC;
    v_end_stock NUMERIC;
BEGIN	
    INSERT INTO pizzeria_schema.ingredients (name, unit, stock_quantity) 
    VALUES ('AutoTest Cheese', 'kg', 100.00) RETURNING id INTO v_ing_id;

    INSERT INTO pizzeria_schema.products (name, base_price, is_available) 
    VALUES ('AutoTest Pizza', 20.00, true) RETURNING id INTO v_prod_id;

    INSERT INTO pizzeria_schema.product_ingredients (product_id, ingredient_id, quantity)
    VALUES (v_prod_id, v_ing_id, 0.5);

    INSERT INTO pizzeria_schema.orders (status, order_type) 
    VALUES ('new', 'dine_in') RETURNING id INTO v_order_id;

    INSERT INTO pizzeria_schema.order_items (order_id, product_id, quantity, unit_price, status)
    VALUES (v_order_id, v_prod_id, 2, 20.00, 'pending');

    SELECT stock_quantity INTO v_start_stock FROM pizzeria_schema.ingredients WHERE id = v_ing_id;
    
    UPDATE pizzeria_schema.order_items SET status = 'preparing' WHERE order_id = v_order_id;

    SELECT stock_quantity INTO v_end_stock FROM pizzeria_schema.ingredients WHERE id = v_ing_id;

    IF v_end_stock = 99.00 THEN
        RAISE NOTICE 'SUCCESS: Stock updated correctly from % to %', v_start_stock, v_end_stock;
    ELSE
        RAISE EXCEPTION 'FAILURE: Stock did not update correctly. Expected 99.00, got %', v_end_stock;
    END IF;
    

END $$;