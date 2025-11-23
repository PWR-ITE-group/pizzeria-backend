-- -----------------------------------------------------
-- ADVANCED INDEXING & OPTIMIZATION
-- -----------------------------------------------------

-- Удаляем старые простые индексы, если они были (чтобы заменить их на сложные)
DROP INDEX IF EXISTS pizzeria_schema.idx_orders_status;
DROP INDEX IF EXISTS pizzeria_schema.idx_products_menu;
DROP INDEX IF EXISTS pizzeria_schema.idx_inventory_ingredient;

-- =====================================================
-- 1. ЗАКАЗЫ (ORDERS) - Самая нагруженная таблица
-- =====================================================

-- Сценарий: "Кухонный экран". Повар видит заказы со статусом 'new' или 'preparing'.
-- Ему важно видеть их в порядке поступления (старые сверху - FIFO), чтобы не задерживать отдачу.
-- Используем ASC для времени.
CREATE INDEX idx_orders_kitchen_queue
    ON pizzeria_schema.orders (status, placed_at ASC);

-- Сценарий: "История заказов пользователя/админка".
-- Менеджер смотрит последние заказы. Ему нужны САМЫЕ НОВЫЕ сверху.
-- Используем DESC для времени.
CREATE INDEX idx_orders_history_latest
    ON pizzeria_schema.orders (placed_at DESC);

-- Сценарий: "Поиск активных заказов конкретного сотрудника (официанта)"
CREATE INDEX idx_orders_employee_active
    ON pizzeria_schema.orders (employee_id, status)
    WHERE status NOT IN ('delivered', 'cancelled', 'failed');
-- ^ Это "Частичный индекс" (Partial Index). Он весит мало, так как не индексирует архивные заказы.


-- =====================================================
-- 2. ПРОДУКТЫ (PRODUCTS) - Покрывающие индексы
-- =====================================================

-- Сценарий: "Отображение меню".
-- Клиент открывает меню. Нам нужно показать Название, Цену и Картинку.
-- Мы делаем поиск по menu_id + is_available, а остальные поля просто "приклеиваем" (INCLUDE),
-- чтобы база не лезла в основную таблицу (Index Only Scan).
CREATE INDEX idx_products_menu_covering
    ON pizzeria_schema.products (menu_id, is_available)
    INCLUDE (name, base_price, image_url);


-- =====================================================
-- 3. ДОСТАВКИ (DELIVERIES)
-- =====================================================

-- Сценарий: "Курьер смотрит свои текущие задачи".
-- Ему нужны только те, что 'assigned' или 'in_transit'.
-- Сортируем по времени назначения (самые старые - срочные).
CREATE INDEX idx_deliveries_courier_tasks
    ON pizzeria_schema.deliveries (courier_id, status, assigned_at ASC);


-- =====================================================
-- 4. СКЛАД (INVENTORY)
-- =====================================================

-- Сценарий: "Анализ движения товара".
-- "Что происходило с Помидорами (id=5) за последнюю неделю?"
-- Сортируем DESC, чтобы сразу видеть последние изменения.
-- INCLUDE (quantity_change) позволяет быстро посчитать сумму без обращения к таблице.
CREATE INDEX idx_inventory_ingredient_history
    ON pizzeria_schema.inventory_movements (ingredient_id, "timestamp" DESC)
    INCLUDE (quantity_change, movement_type);


-- =====================================================
-- 5. КЛИЕНТЫ/АДРЕСА (DELIVERY_INFO)
-- =====================================================

-- Сценарий: "Поиск клиента по телефону при звонке".
-- Оператор вбивает номер.
CREATE INDEX idx_delivery_info_phone
    ON pizzeria_schema.delivery_info (phone);

-- Сценарий: "Поиск по фамилии для статистики".
-- Используем lower(), чтобы поиск 'Ivanov' нашел 'ivanov'.
-- Это "Функциональный индекс".
CREATE INDEX idx_delivery_info_lastname_lower
    ON pizzeria_schema.delivery_info (lower(last_name));