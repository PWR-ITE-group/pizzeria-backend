
-- 1. Employees
INSERT INTO employees (name, last_name, phone, login, password_hash, role)
VALUES ('John', 'Doe', '123456789', 'admin', 'hash_pass', 'manager'),
       ('Mike', 'Smith', '987654321', 'courier1', 'hash_pass', 'courier');

-- 2. Menus
INSERT INTO menus (name, description, is_active)
VALUES ('Main Menu', 'Standard pizzas and drinks', true),
       ('Seasonal Menu', 'Autumn specials', true);

-- 3. Products
INSERT INTO products (menu_id, name, description, base_price, image_url, is_available)
VALUES (1, 'Margherita', 'Tomato sauce, mozzarella, basil', 25.00, 'http://img.com/margarita', true),
       (1, 'Pepperoni', 'Spicy salami, cheese, tomato sauce', 30.50, 'http://img.com/pepp', true);

-- 4. Ingredients
INSERT INTO ingredients (name, unit, stock_quantity)
VALUES ('Pizza Dough', 'grams', 5000),
       ('Mozzarella Cheese', 'grams', 2000),
       ('Pepperoni Salami', 'grams', 1000);

-- 5. Product Ingredients (Linking Products to Ingredients)
INSERT INTO product_ingredients (product_id, ingredient_id, quantity)
VALUES (1, 1, 250), -- Dough for Margherita
       (1, 2, 150), -- Cheese for Margherita
       (2, 3, 100); -- Salami for Pepperoni

-- 6. Orders
INSERT INTO orders (employee_id, status, order_type)
VALUES (1, 'new', 'delivery'),
       (1, 'preparing', 'dine_in');

-- 7. Deliveries
INSERT INTO deliveries (order_id, courier_id, status)
VALUES (1, 2, 'assigned');

-- 8. Delivery Info
INSERT INTO delivery_info (delivery_id, name, last_name, phone, street, house_nr, city, postal_code)
VALUES (1, 'David', 'Johnson', '555-0101', 'High Street', '10', 'Wroclaw', '50-123');

-- 9. Order Items
INSERT INTO order_items (order_id, product_id, quantity, unit_price, status)
VALUES (1, 1, 2, 25.00, 'pending'), -- 2 Margheritas
       (2, 2, 1, 30.50, 'preparing'); -- 1 Pepperoni

-- 10. Payments
INSERT INTO payments (order_id, amount, method, status)
VALUES (1, 50.00, 'card', 'paid');

-- 11. Promotions
INSERT INTO promotions (code, description, discount_percent, is_active)
VALUES ('SUMMER20', 'Summer Sale 20% Off', 20.00, true);

-- 12. Inventory Movements
INSERT INTO inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
VALUES (1, 500, 'restock', 1);