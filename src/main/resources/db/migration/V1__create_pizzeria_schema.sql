
CREATE SCHEMA IF NOT EXISTS pizzeria_schema;


CREATE TABLE IF NOT EXISTS pizzeria_schema.employees
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    phone           VARCHAR(50) UNIQUE NOT NULL,
    login           VARCHAR(100) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.menus
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    is_active       BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.products
(
    id              BIGSERIAL PRIMARY KEY,
    menu_id         BIGINT REFERENCES pizzeria_schema.menus (id),
    name            VARCHAR(255) NOT NULL,
    description     TEXT,
    base_price      NUMERIC(10, 2) NOT NULL,
    image_url       VARCHAR(255),
    is_available    BOOLEAN DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.ingredients
(
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    unit            VARCHAR(50),
    stock_quantity  NUMERIC(10, 2) DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.product_ingredients
(
    product_id      BIGINT REFERENCES pizzeria_schema.products (id) ON DELETE CASCADE,
    ingredient_id   BIGINT REFERENCES pizzeria_schema.ingredients (id) ON DELETE CASCADE,
    quantity        NUMERIC(10, 2) NOT NULL,
    PRIMARY KEY (product_id, ingredient_id)
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.orders
(
    id              BIGSERIAL PRIMARY KEY,
    employee_id     BIGINT REFERENCES pizzeria_schema.employees (id),
    status          VARCHAR(50) NOT NULL,
    order_type      VARCHAR(50) NOT NULL,
    placed_at       TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITHOUT TIME ZONE
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.delivery_info
(
    id              BIGSERIAL PRIMARY KEY,
    delivery_id     BIGINT UNIQUE NOT NULL,
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

CREATE TABLE IF NOT EXISTS pizzeria_schema.deliveries
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    courier_id      BIGINT REFERENCES pizzeria_schema.employees (id),
    status          VARCHAR(50) NOT NULL,
    assigned_at     TIMESTAMP WITHOUT TIME ZONE,
    delivered_at    TIMESTAMP WITHOUT TIME ZONE
    );

ALTER TABLE pizzeria_schema.delivery_info
    ADD CONSTRAINT fk_delivery_info_delivery_id
        FOREIGN KEY (delivery_id) REFERENCES pizzeria_schema.deliveries (id) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS pizzeria_schema.order_items
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    product_id      BIGINT REFERENCES pizzeria_schema.products (id) ON DELETE RESTRICT,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    unit_price      NUMERIC(10, 2) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    UNIQUE (order_id, product_id)
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.payments
(
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT UNIQUE REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    amount          NUMERIC(10, 2) NOT NULL CHECK (amount >= 0),
    method          VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    paid_at         TIMESTAMP WITHOUT TIME ZONE
    );

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

CREATE TABLE IF NOT EXISTS pizzeria_schema.order_promotions
(
    order_id        BIGINT REFERENCES pizzeria_schema.orders (id) ON DELETE CASCADE,
    promotion_id    BIGINT REFERENCES pizzeria_schema.promotions (id) ON DELETE RESTRICT,
    PRIMARY KEY (order_id, promotion_id)
    );

CREATE TABLE IF NOT EXISTS pizzeria_schema.inventory_movements
(
    id              BIGSERIAL PRIMARY KEY,
    ingredient_id   BIGINT REFERENCES pizzeria_schema.ingredients (id) ON DELETE RESTRICT,
    quantity_change NUMERIC(10, 2) NOT NULL,
    movement_type   VARCHAR(50) NOT NULL,
    "timestamp"     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    employee_id     BIGINT REFERENCES pizzeria_schema.employees (id) ON DELETE RESTRICT
    );


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


CREATE OR REPLACE FUNCTION pizzeria_schema.create_stock_use_on_preparing()
RETURNS TRIGGER AS $$
DECLARE
v_order_employee_id BIGINT;
    v_product_id BIGINT;
    v_quantity BIGINT;
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
