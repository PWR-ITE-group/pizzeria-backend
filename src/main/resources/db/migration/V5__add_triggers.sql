
CREATE TABLE IF NOT EXISTS pizzeria_schema.price_history_log (
                                                                 id BIGSERIAL PRIMARY KEY,
                                                                 product_id BIGINT,
                                                                 old_price NUMERIC(10,2),
                                                                 new_price NUMERIC(10,2),
                                                                 changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                                 changed_by VARCHAR(100) DEFAULT current_user -- Кто менял (пользователь БД)
);

ALTER TABLE pizzeria_schema.orders
    ADD COLUMN IF NOT EXISTS total_price NUMERIC(10, 2) DEFAULT 0;

ALTER TABLE pizzeria_schema.employees
    ADD COLUMN IF NOT EXISTS completed_orders_count INTEGER DEFAULT 0;


CREATE OR REPLACE FUNCTION pizzeria_schema.log_price_changes()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.base_price <> NEW.base_price THEN
        INSERT INTO pizzeria_schema.price_history_log (product_id, old_price, new_price)
        VALUES (NEW.id, OLD.base_price, NEW.base_price);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_product_price
    AFTER UPDATE ON pizzeria_schema.products
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.log_price_changes();


CREATE OR REPLACE FUNCTION pizzeria_schema.recalculate_order_total()
    RETURNS TRIGGER AS $$
DECLARE
    v_order_id INTEGER;
    v_new_total NUMERIC(10,2);
BEGIN
    IF (TG_OP = 'DELETE') THEN
        v_order_id := OLD.order_id;
    ELSE
        v_order_id := NEW.order_id;
    END IF;

    SELECT COALESCE(SUM(quantity * unit_price), 0) INTO v_new_total
    FROM pizzeria_schema.order_items
    WHERE order_id = v_order_id;

    UPDATE pizzeria_schema.orders
    SET total_price = v_new_total
    WHERE id = v_order_id;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_order_total
    AFTER INSERT OR UPDATE OR DELETE ON pizzeria_schema.order_items
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.recalculate_order_total();


CREATE OR REPLACE FUNCTION pizzeria_schema.update_courier_stats()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'delivered' AND OLD.status <> 'delivered' THEN
        IF NEW.courier_id IS NOT NULL THEN
            UPDATE pizzeria_schema.employees
            SET completed_orders_count = completed_orders_count + 1
            WHERE id = NEW.courier_id;
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_courier_xp_boost
    AFTER UPDATE ON pizzeria_schema.deliveries
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.update_courier_stats();


CREATE OR REPLACE FUNCTION pizzeria_schema.clean_delivery_info()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.name := TRIM(NEW.name);
    NEW.last_name := TRIM(NEW.last_name);
    NEW.city := TRIM(NEW.city);
    NEW.street := TRIM(NEW.street);

    IF NEW.email IS NOT NULL THEN
        NEW.email := LOWER(TRIM(NEW.email));
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_clean_data_before_insert
    BEFORE INSERT OR UPDATE ON pizzeria_schema.delivery_info
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.clean_delivery_info();


CREATE OR REPLACE FUNCTION pizzeria_schema.disable_products_on_menu_close()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.is_active = FALSE AND OLD.is_active = TRUE THEN
        UPDATE pizzeria_schema.products
        SET is_available = FALSE
        WHERE menu_id = NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_cascade_menu_disable
    AFTER UPDATE OF is_active ON pizzeria_schema.menus
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.disable_products_on_menu_close();