-- -----------------------------------------------------
-- 1. ПОДГОТОВКА: Создаем новые колонки и таблицы для триггеров
-- -----------------------------------------------------

-- Таблица для истории изменения цен (Аудит)
CREATE TABLE IF NOT EXISTS pizzeria_schema.price_history_log (
                                                                 id BIGSERIAL PRIMARY KEY,
                                                                 product_id BIGINT,
                                                                 old_price NUMERIC(10,2),
                                                                 new_price NUMERIC(10,2),
                                                                 changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                                 changed_by VARCHAR(100) DEFAULT current_user -- Кто менял (пользователь БД)
);

-- Добавляем колонку "Общая сумма" в заказ (для кэширования)
ALTER TABLE pizzeria_schema.orders
    ADD COLUMN IF NOT EXISTS total_price NUMERIC(10, 2) DEFAULT 0;

-- Добавляем колонку "Рейтинг/Кол-во заказов" сотруднику
ALTER TABLE pizzeria_schema.employees
    ADD COLUMN IF NOT EXISTS completed_orders_count INTEGER DEFAULT 0;


-- -----------------------------------------------------
-- 2. ТРИГГЕР: АУДИТ ЦЕН
-- Логика: Если цена изменилась, сохранить старую в архив.
-- -----------------------------------------------------
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


-- -----------------------------------------------------
-- 3. ТРИГГЕР: АВТО-РАСЧЕТ СУММЫ ЗАКАЗА
-- Логика: Когда добавляем/удаляем позицию, пересчитываем order.total_price
-- -----------------------------------------------------
CREATE OR REPLACE FUNCTION pizzeria_schema.recalculate_order_total()
    RETURNS TRIGGER AS $$
DECLARE
    v_order_id INTEGER;
    v_new_total NUMERIC(10,2);
BEGIN
    -- Определяем ID заказа (зависит от операции DELETE или INSERT/UPDATE)
    IF (TG_OP = 'DELETE') THEN
        v_order_id := OLD.order_id;
    ELSE
        v_order_id := NEW.order_id;
    END IF;

    -- Считаем сумму
    SELECT COALESCE(SUM(quantity * unit_price), 0) INTO v_new_total
    FROM pizzeria_schema.order_items
    WHERE order_id = v_order_id;

    -- Обновляем кэш в таблице orders
    UPDATE pizzeria_schema.orders
    SET total_price = v_new_total
    WHERE id = v_order_id;

    RETURN NULL; -- Для AFTER триггера результат не важен
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_calculate_order_total
    AFTER INSERT OR UPDATE OR DELETE ON pizzeria_schema.order_items
    FOR EACH ROW
EXECUTE FUNCTION pizzeria_schema.recalculate_order_total();


-- -----------------------------------------------------
-- 4. ТРИГГЕР: РЕЙТИНГ КУРЬЕРА
-- Логика: Если доставка стала 'delivered', +1 к статистике курьера
-- -----------------------------------------------------
CREATE OR REPLACE FUNCTION pizzeria_schema.update_courier_stats()
    RETURNS TRIGGER AS $$
BEGIN
    -- Если статус сменился на delivered
    IF NEW.status = 'delivered' AND OLD.status <> 'delivered' THEN
        -- Если у доставки есть курьер
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


-- -----------------------------------------------------
-- 5. ТРИГГЕР: ЧИСТОТА ДАННЫХ (Data Hygiene)
-- Логика: Убрать пробелы в начале/конце, Email в нижний регистр перед вставкой
-- -----------------------------------------------------
CREATE OR REPLACE FUNCTION pizzeria_schema.clean_delivery_info()
    RETURNS TRIGGER AS $$
BEGIN
    -- Убираем пробелы
    NEW.name := TRIM(NEW.name);
    NEW.last_name := TRIM(NEW.last_name);
    NEW.city := TRIM(NEW.city);
    NEW.street := TRIM(NEW.street);

    -- Email всегда с маленькой буквы
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


-- -----------------------------------------------------
-- 6. ТРИГГЕР: КАСКАДНОЕ ОТКЛЮЧЕНИЕ МЕНЮ
-- Логика: Если меню выключили, выключить все пиццы в нем
-- -----------------------------------------------------
CREATE OR REPLACE FUNCTION pizzeria_schema.disable_products_on_menu_close()
    RETURNS TRIGGER AS $$
BEGIN
    -- Если is_active стало FALSE
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