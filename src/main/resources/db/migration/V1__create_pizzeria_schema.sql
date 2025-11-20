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
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20) UNIQUE NOT NULL, -- +48 123 456 789 (хватит 20)
    login           VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,       -- Хэш всегда длинный
    role            VARCHAR(32) NOT NULL,        -- waiter, chef, manager (хватит 32)
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Table menus
CREATE TABLE IF NOT EXISTS pizzeria_schema.menus
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,                        -- TEXT лучше для описаний
    is_active       BOOLEAN DEFAULT TRUE
);

-- Table products
CREATE TABLE IF NOT EXISTS pizzeria_schema.products
(
    id              SERIAL PRIMARY KEY,
    menu_id         INTEGER REFERENCES pizzeria_schema.menus (id),
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    base_price      NUMERIC(10, 2) NOT NULL CHECK (base_price >= 0), -- Цена не может быть отрицательной
    image_url       VARCHAR(512),
    is_available    BOOLEAN DEFAULT TRUE
);

-- Table ingredients
CREATE TABLE IF NOT EXISTS pizzeria_schema.ingredients
(
    id              SERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    unit            VARCHAR(20), -- grams, ml, pcs
    stock_quantity  NUMERIC(10, 2) DEFAULT 0 CHECK (stock_quantity >= 0)
);

-- Table product_ingredients (N:M relationship)
CREATE TABLE IF NOT EXISTS pizzeria_schema.product_ingredients
(
    product_id      INTEGER REFERENCES pizzeria_schema.products (id) ON DELETE CASCADE,
    ingredient_id   INTEGER REFERENCES pizzeria_schema.ingredients (id) ON DELETE CASCADE,
    quantity        NUMERIC(10, 2) NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (product_id, ingredient_id)
);

-- Table orders
CREATE TABLE IF NOT EXISTS pizzeria_schema.orders
(
    id              SERIAL PRIMARY KEY,
    employee_id     INTEGER REFERENCES pizzeria_schema.employees (id),
    status          VARCHAR(32) NOT NULL, -- new, preparing, ready, delivered
    order_type      VARCHAR(32) NOT NULL, -- delivery, pickup, dine_in
    placed_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE
);

-- Table deliveries
CREATE TABLE IF NOT EXISTS pizzeria_schema.deliveries
(
    id              SERIAL PRIMARY KEY,
    order_id        INTEGER UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    courier_id      INTEGER REFERENCES pizzeria_schema.employees (id),
    status          VARCHAR(32) NOT NULL, -- assigned, in_transit, delivered
    assigned_at     TIMESTAMP WITHOUT TIME ZONE,
    delivered_at    TIMESTAMP WITHOUT TIME ZONE
);

-- Table delivery_info
-- Исправлено: email необязателен, телефоны и адреса оптимизированы
CREATE TABLE IF NOT EXISTS pizzeria_schema.delivery_info
(
    id              SERIAL PRIMARY KEY,
    delivery_id     INTEGER UNIQUE NOT NULL REFERENCES pizzeria_schema.deliveries (id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(20) NOT NULL,
    email           VARCHAR(255),          -- NULL allowed
    street          VARCHAR(150) NOT NULL,
    house_nr        VARCHAR(20) NOT NULL,
    city            VARCHAR(100) NOT NULL,
    postal_code     VARCHAR(10),
    additional_info TEXT
);

-- Table order_items
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_items
(
    id              SERIAL PRIMARY KEY,
    order_id        INTEGER REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    product_id      INTEGER REFERENCES pizzeria_schema.products (id) ON DELETE RESTRICT,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10, 2) NOT NULL CHECK (unit_price >= 0),
    status          VARCHAR(32) NOT NULL, -- pending, preparing, ready
    UNIQUE (order_id, product_id)
);

-- Table payments
CREATE TABLE IF NOT EXISTS pizzeria_schema.payments
(
    id              SERIAL PRIMARY KEY,
    order_id        INTEGER UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    method          VARCHAR(32) NOT NULL, -- cash, card, blik
    status          VARCHAR(32) NOT NULL, -- pending, paid, failed
    paid_at         TIMESTAMP WITHOUT TIME ZONE
);

-- Table payment_company_details
CREATE TABLE IF NOT EXISTS pizzeria_schema.payment_company_details
(
    id              SERIAL PRIMARY KEY,
    payment_id      INTEGER UNIQUE REFERENCES pizzeria_schema.payments (id) ON DELETE CASCADE,
    company_name    VARCHAR(255) NOT NULL,
    nip             VARCHAR(20) NOT NULL,
    street          VARCHAR(150),
    house_nr        VARCHAR(20),
    city            VARCHAR(100),
    postal_code     VARCHAR(10),
    account_number  VARCHAR(50)
);

-- Table promotions
CREATE TABLE IF NOT EXISTS pizzeria_schema.promotions
(
    id              SERIAL PRIMARY KEY,
    code            VARCHAR(50) UNIQUE NOT NULL,
    description     TEXT,
    discount_percent NUMERIC(5, 2) CHECK (discount_percent >= 0 AND discount_percent <= 100),
    valid_from      DATE,
    valid_to        DATE,
    is_active       BOOLEAN DEFAULT TRUE
);

-- Table order_promotions
CREATE TABLE IF NOT EXISTS pizzeria_schema.order_promotions
(
    order_id        INTEGER REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    promotion_id    INTEGER REFERENCES pizzeria_schema.promotions (id) ON DELETE RESTRICT,
    PRIMARY KEY (order_id, promotion_id)
);

-- Table inventory_movements
CREATE TABLE IF NOT EXISTS pizzeria_schema.inventory_movements
(
    id              SERIAL PRIMARY KEY,
    ingredient_id   INTEGER REFERENCES pizzeria_schema.ingredients (id) ON DELETE RESTRICT,
    quantity_change NUMERIC(10, 2) NOT NULL,
    movement_type   VARCHAR(32) NOT NULL, -- use, restock, adjustment
    "timestamp"     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    employee_id     INTEGER REFERENCES pizzeria_schema.employees (id) ON DELETE RESTRICT
);

-- -----------------------------------------------------
-- 3. BASIC INDEXES (Performance Essentials)
-- -----------------------------------------------------

CREATE INDEX idx_employees_login ON pizzeria_schema.employees (login);
CREATE INDEX idx_products_menu ON pizzeria_schema.products (menu_id);
CREATE INDEX idx_orders_status ON pizzeria_schema.orders (status);
CREATE INDEX idx_order_items_order ON pizzeria_schema.order_items (order_id);
CREATE INDEX idx_deliveries_order ON pizzeria_schema.deliveries (order_id);
CREATE INDEX idx_payments_order ON pizzeria_schema.payments (order_id);

-- -----------------------------------------------------
-- 4. TRIGGERS AND FUNCTIONS (Business Logic)
-- -----------------------------------------------------

-- Function: Авто-обновление склада
CREATE OR REPLACE FUNCTION pizzeria_schema.update_ingredient_stock()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE pizzeria_schema.ingredients
    SET stock_quantity = stock_quantity + NEW.quantity_change
    WHERE id = NEW.ingredient_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_stock_on_movement
    AFTER INSERT ON pizzeria_schema.inventory_movements
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.update_ingredient_stock();

-- Function: Авто-обновление updated_at
CREATE OR REPLACE FUNCTION pizzeria_schema.set_updated_at()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_updated_at
    BEFORE UPDATE ON pizzeria_schema.orders
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.set_updated_at();

-- Function: Списание продуктов при начале готовки (preparing)
CREATE OR REPLACE FUNCTION pizzeria_schema.create_stock_use_on_preparing()
    RETURNS TRIGGER AS $$
DECLARE
    v_order_employee_id INTEGER;
    v_product_id INTEGER;
    v_quantity INTEGER;
BEGIN
    IF NEW.status = 'preparing' AND OLD.status IS DISTINCT FROM NEW.status THEN
        v_product_id := NEW.product_id;
        v_quantity := NEW.quantity;

        SELECT employee_id INTO v_order_employee_id
        FROM pizzeria_schema.orders
        WHERE id = NEW.order_id;

        INSERT INTO pizzeria_schema.inventory_movements (ingredient_id, quantity_change, movement_type, employee_id)
        SELECT
            pi.ingredient_id,
            -(pi.quantity * v_quantity),
            'use',
            v_order_employee_id
        FROM pizzeria_schema.product_ingredients pi
        WHERE pi.product_id = v_product_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_stock_use_on_preparing
    AFTER UPDATE OF status ON pizzeria_schema.order_items
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.create_stock_use_on_preparing();