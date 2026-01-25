DO $$
DECLARE
    v_emp_min INT; v_emp_max INT;
    v_menu_id INT;
    v_prod_min INT; v_prod_max INT;
    v_ing_min INT; v_ing_max INT;
    
    C_ORDER_COUNT CONSTANT INT := 100; 
    C_MOVEMENT_COUNT CONSTANT INT := 200;
BEGIN
    RAISE NOTICE 'Started generating test data...';


    INSERT INTO pizzeria_schema.employees (name, last_name, phone, login, password_hash, role)
    SELECT 
        'Name_' || i, 
        'Surname_' || i, 
        'Phone_' || i || '_' || floor(random()*10000), 
        'user_' || i, 
        'hash_xyz', 
        (ARRAY['waiter', 'chef', 'manager', 'courier'])[floor(random()*4 + 1)]
    FROM generate_series(1, 50) AS i
    ON CONFLICT DO NOTHING; 

    SELECT min(id), max(id) INTO v_emp_min, v_emp_max FROM pizzeria_schema.employees;


    INSERT INTO pizzeria_schema.menus (name, description) VALUES ('Main Menu', 'Standard menu') RETURNING id INTO v_menu_id;

    INSERT INTO pizzeria_schema.ingredients (name, unit, stock_quantity)
    SELECT 'Ingredient_' || i, 'kg', 1000.00
    FROM generate_series(1, 100) AS i;
    
    SELECT min(id), max(id) INTO v_ing_min, v_ing_max FROM pizzeria_schema.ingredients;

    INSERT INTO pizzeria_schema.products (menu_id, name, base_price, is_available)
    SELECT 
        v_menu_id, 
        'Pizza_' || i, 
        (random() * 50 + 20)::numeric(10,2), 
        (random() > 0.1) 
    FROM generate_series(1, 100) AS i;
    
    SELECT min(id), max(id) INTO v_prod_min, v_prod_max FROM pizzeria_schema.products;


    RAISE NOTICE 'Generating % Orders...', C_ORDER_COUNT;
    
    INSERT INTO pizzeria_schema.orders (employee_id, status, order_type, placed_at)
    SELECT
        floor(random() * (v_emp_max - v_emp_min + 1) + v_emp_min)::INT,
        CASE 
            WHEN random() < 0.1 THEN 'new' 
            WHEN random() < 0.2 THEN 'preparing' 
            WHEN random() < 0.25 THEN 'ready' 
            WHEN random() < 0.3 THEN 'cancelled'
            ELSE 'delivered' 
        END,
        CASE WHEN random() < 0.6 THEN 'delivery' ELSE 'pickup' END,
        NOW() - (random() * interval '365 days') 
    FROM generate_series(1, C_ORDER_COUNT) AS i;


    RAISE NOTICE 'Generating Deliveries...';
    
    INSERT INTO pizzeria_schema.deliveries (order_id, courier_id, status, assigned_at, delivered_at)
    SELECT 
        id,
        floor(random() * (v_emp_max - v_emp_min + 1) + v_emp_min)::INT,
        CASE 
            WHEN status = 'delivered' THEN 'delivered'
            WHEN status = 'cancelled' THEN 'failed'
            ELSE 'assigned'
        END,
        placed_at + interval '10 minutes',
        CASE WHEN status = 'delivered' THEN placed_at + interval '40 minutes' ELSE NULL END
    FROM pizzeria_schema.orders
    WHERE order_type = 'delivery';


    RAISE NOTICE 'Generating Delivery Info...';
    
    INSERT INTO pizzeria_schema.delivery_info (delivery_id, name, last_name, phone, city, street, house_nr)
    SELECT
        id,
        'ClientName',
        CASE 
            WHEN id % 3 = 0 THEN 'Smith'
            WHEN id % 3 = 1 THEN 'smith'
            ELSE 'SMITH'
        END || id,
        '555-' || id, 
        'Wroclaw',
        'Main St',
        id::text
    FROM pizzeria_schema.deliveries;


    RAISE NOTICE 'Generating Inventory History (% rows)...', C_MOVEMENT_COUNT;
    
    INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, "timestamp", employee_id)
    SELECT
        floor(random() * (v_ing_max - v_ing_min + 1) + v_ing_min)::INT,
        (random() * 10 - 5)::numeric(10,2), 
        (ARRAY['use', 'restock', 'adjustment'])[floor(random()*3 + 1)],
        NOW() - (random() * interval '365 days'),
        floor(random() * (v_emp_max - v_emp_min + 1) + v_emp_min)::INT
    FROM generate_series(1, C_MOVEMENT_COUNT) AS i;

    RAISE NOTICE 'Data generation completed successfully!';
END $$;