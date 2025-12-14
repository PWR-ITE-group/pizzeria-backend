-- -----------------------------------------------------
-- 1. SCHEMA CREATION
-- -----------------------------------------------------

CREATE SCHEMA IF NOT EXISTS pizzeria_schema;

-- -----------------------------------------------------
-- 2. TABLE CREATION
-- -----------------------------------------------------

-- Table employees
CREATE TABLE IF NOT EXISTS pizzeria_schema.employees
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50) UNIQUE NOT NULL,
    login           VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL, -- np. waiter, chef, manager, courier
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

-- Table menus
CREATE TABLE IF NOT EXISTS pizzeria_schema.menus
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN DEFAULT TRUE
    );

-- Table products
CREATE TABLE IF NOT EXISTS pizzeria_schema.products
(
    id              BIGSERIAL PRIMARY KEY,
    menu_id         BIGINT REFERENCES pizzeria_schema.menus (id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    base_price      NUMERIC(10, 2) NOT NULL,
    image_url       VARCHAR(255), -- zdjęcie produktu (np. pizza)
    is_available    BOOLEAN DEFAULT TRUE
    );

-- Table ingredients
CREATE TABLE IF NOT EXISTS pizzeria_schema.ingredients
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    unit            VARCHAR(50), -- np. grams, ml
    stock_quantity  NUMERIC(10, 2) DEFAULT 0
    );

-- Table product_ingredients (N:M relationship)
CREATE TABLE IF NOT EXISTS pizzeria_schema.product_ingredients
(
    product_id      BIGINT REFERENCES pizzeria_schema.products (id) ON DELETE CASCADE,
    ingredient_id   BIGINT REFERENCES pizzeria_schema.ingredients (id) ON DELETE CASCADE,
    quantity        NUMERIC(10, 2) NOT NULL, -- ile jednostek składnika na jeden produkt
    PRIMARY KEY (product_id, ingredient_id)
    );

-- Table orders
CREATE TABLE IF NOT EXISTS pizzeria_schema.orders
(
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT REFERENCES pizzeria_schema.employees (id), -- Kto przyjął zamówienie (jeśli lokalne)
    status          VARCHAR(50) NOT NULL, -- np. new, preparing, ready, delivered
    order_type      VARCHAR(50) NOT NULL, -- delivery, pickup, dine_in
    placed_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE
    );

-- Table delivery_info
CREATE TABLE IF NOT EXISTS pizzeria_schema.delivery_info
(
    id              BIGSERIAL PRIMARY KEY,
    delivery_id     BIGINT UNIQUE NOT NULL, -- Reference to deliveries.id, added below
    name            VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50) NOT NULL,
    email           VARCHAR(255),
    street          VARCHAR(255),
    house_nr        VARCHAR(10),
    apartment_nr    VARCHAR(10),
    city            VARCHAR(100),
    postal_code     VARCHAR(20),
    additional_info VARCHAR(255)
    );

-- Table deliveries
CREATE TABLE IF NOT EXISTS pizzeria_schema.deliveries
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    courier_id      BIGINT REFERENCES pizzeria_schema.employees (id), -- Pracownik typu kurier
    status          VARCHAR(50) NOT NULL, -- assigned, in_transit, delivered
    assigned_at     TIMESTAMP WITHOUT TIME ZONE,
    delivered_at    TIMESTAMP WITHOUT TIME ZONE
    );

-- Adding the foreign key to delivery_info after deliveries is created
ALTER TABLE pizzeria_schema.delivery_info
    ADD CONSTRAINT fk_delivery_info_delivery_id
        FOREIGN KEY (delivery_id) REFERENCES pizzeria_schema.deliveries (id) ON DELETE CASCADE;

-- Table order_items
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_items
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    product_id      BIGINT REFERENCES pizzeria_schema.products (id) ON DELETE RESTRICT,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10, 2) NOT NULL,
    status          VARCHAR(50) NOT NULL, -- pending, preparing, ready
    UNIQUE (order_id, product_id)
    );

-- Table payments
CREATE TABLE IF NOT EXISTS pizzeria_schema.payments
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    method          VARCHAR(50) NOT NULL, -- online, cash, card
    status          VARCHAR(50) NOT NULL, -- pending, paid, failed
    paid_at         TIMESTAMP WITHOUT TIME ZONE
    );

-- Table payment_company_details
CREATE TABLE IF NOT EXISTS pizzeria_schema.payment_company_details
(
    id              BIGSERIAL PRIMARY KEY,
    payment_id      BIGINT UNIQUE REFERENCES pizzeria_schema.payments (id) ON DELETE CASCADE,
    company_name    VARCHAR(255) NOT NULL,
    nip             VARCHAR(20) NOT NULL,
    street          VARCHAR(255),
    house_nr        VARCHAR(10),
    city            VARCHAR(100),
    postal_code     VARCHAR(20),
    account_number  VARCHAR(50)
    );

-- Table promotions
CREATE TABLE IF NOT EXISTS pizzeria_schema.promotions
(
    id              BIGSERIAL PRIMARY KEY,
    code            VARCHAR(100) UNIQUE NOT NULL,
    description     TEXT,
    discount_percent NUMERIC(5, 2) CHECK (discount_percent >= 0 AND discount_percent <= 100),
    valid_from      DATE,
    valid_to        DATE,
    is_active       BOOLEAN DEFAULT TRUE
    );

-- Table order_promotions (N:M relationship)
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_promotions
(
    order_id        BIGINT REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    promotion_id    BIGINT REFERENCES pizzeria_schema.promotions (id) ON DELETE RESTRICT,
    PRIMARY KEY (order_id, promotion_id)
    );

-- Table inventory_movements
CREATE TABLE IF NOT EXISTS pizzeria_schema.inventory_movements
(
    id              BIGSERIAL PRIMARY KEY,
    ingredient_id   BIGINT REFERENCES pizzeria_schema.ingredients (id) ON DELETE RESTRICT,
    quantity_change NUMERIC(10, 2) NOT NULL, -- dodatnie (restock) lub ujemne (use, adjustment)
    movement_type   VARCHAR(50) NOT NULL, -- use, restock, adjustment
    "timestamp"     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    employee_id     BIGINT REFERENCES pizzeria_schema.employees (id) ON DELETE RESTRICT
    );

-- -----------------------------------------------------
-- 3. INDEXES
-- -----------------------------------------------------

CREATE INDEX idx_employees_login ON pizzeria_schema.employees (login);
CREATE INDEX idx_products_menu ON pizzeria_schema.products (menu_id);
CREATE INDEX idx_orders_status ON pizzeria_schema.orders (status);
CREATE INDEX idx_orders_type ON pizzeria_schema.orders (order_type);
CREATE INDEX idx_order_items_order ON pizzeria_schema.order_items (order_id);
CREATE INDEX idx_order_items_product ON pizzeria_schema.order_items (product_id);
CREATE INDEX idx_deliveries_order ON pizzeria_schema.deliveries (order_id);
CREATE INDEX idx_deliveries_courier ON pizzeria_schema.deliveries (courier_id);
CREATE INDEX idx_payments_order ON pizzeria_schema.payments (order_id);
CREATE INDEX idx_inventory_ingredient ON pizzeria_schema.inventory_movements (ingredient_id);

-- -----------------------------------------------------
-- 4. TRIGGERS AND FUNCTIONS
-- -----------------------------------------------------

-- Function to update ingredient stock after a movement
CREATE OR REPLACE FUNCTION pizzeria_schema.update_ingredient_stock()
RETURNS TRIGGER AS $$
BEGIN
UPDATE pizzeria_schema.ingredients
SET stock_quantity = stock_quantity + NEW.quantity_change
WHERE id = NEW.ingredient_id;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for inventory_movements to update stock
CREATE TRIGGER trg_update_stock_on_movement
    AFTER INSERT ON pizzeria_schema.inventory_movements
    FOR EACH ROW
    EXECUTE FUNCTION pizzeria_schema.update_ingredient_stock();

-- Function to update orders.updated_at on any change
CREATE OR REPLACE FUNCTION pizzeria_schema.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to set updated_at on orders table
CREATE TRIGGER trg_set_updated_at
    BEFORE UPDATE ON pizzeria_schema.orders
    FOR EACH ROW
    EXECUTE FUNCTION pizzeria_schema.set_updated_at();

-- Function to create inventory movements (stock use) when an order item status changes to 'preparing'
-- NOTE: This logic assumes ingredients are consumed when order items start preparing.
CREATE OR REPLACE FUNCTION pizzeria_schema.create_stock_use_on_preparing()
RETURNS TRIGGER AS $$
DECLARE
v_order_employee_id BIGINT;
    v_product_id BIGINT;
    v_quantity BIGINT;
BEGIN
    -- Check if status is changing to 'preparing'
    IF NEW.status = 'preparing' AND OLD.status IS DISTINCT FROM NEW.status THEN
        v_product_id := NEW.product_id;
        v_quantity := NEW.quantity;

        -- Get the employee who took the order (assuming this employee is responsible for stock movement)
        -- Fallback to a system/manager ID if employee_id is NULL or not appropriate
SELECT employee_id INTO v_order_employee_id
FROM pizzeria_schema.orders
WHERE id = NEW.order_id;

-- Loop through all ingredients for the product
INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
SELECT
    pi.ingredient_id,
    -(pi.quantity * v_quantity), -- Negative quantity for consumption
    'use',
    v_order_employee_id -- The employee tied to the order
FROM pizzeria_schema.product_ingredients pi
WHERE pi.product_id = v_product_id;
END IF;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to create stock use on order_items status change
CREATE TRIGGER trg_stock_use_on_preparing
    AFTER UPDATE OF status ON pizzeria_schema.order_items
    FOR EACH ROW
    EXECUTE FUNCTION pizzeria_schema.create_stock_use_on_preparing();
