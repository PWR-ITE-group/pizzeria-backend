-- -----------------------------------------------------
-- SECURITY: Granular Role Based Access Control (RBAC)
-- -----------------------------------------------------

-- 1. Безопасное создание ролей (групп)
DO $$
    BEGIN
        -- Admin: Полный доступ ко всему в схеме
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_admin_role') THEN
            CREATE ROLE pizzeria_admin_role;
        END IF;

        -- Manager: Управление меню, складом, персоналом (но не удаление таблиц)
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_manager_role') THEN
            CREATE ROLE pizzeria_manager_role;
        END IF;

        -- Waiter: Прием заказов, оплата, просмотр меню
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_waiter_role') THEN
            CREATE ROLE pizzeria_waiter_role;
        END IF;

        -- Courier: Просмотр адресов доставки, обновление статуса доставки
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_courier_role') THEN
            CREATE ROLE pizzeria_courier_role;
        END IF;
    END
$$;

-- 2. Базовый доступ к схеме (нужен всем)
GRANT USAGE ON SCHEMA pizzeria_schema TO pizzeria_admin_role, pizzeria_manager_role, pizzeria_waiter_role, pizzeria_courier_role;

-- ВАЖНО: Доступ к последовательностям (Sequences), чтобы работали INSERT (id autoincrement)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA pizzeria_schema TO pizzeria_admin_role, pizzeria_manager_role, pizzeria_waiter_role, pizzeria_courier_role;

-- ==================================================================
-- 3. НАСТРОЙКА ПРАВ ДЛЯ КАЖДОЙ РОЛИ
-- ==================================================================

-- ---------------------------------------------------
-- A. ADMIN (Бог системы)
-- ---------------------------------------------------
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pizzeria_schema TO pizzeria_admin_role;
-- Админ сможет делать всё: SELECT, INSERT, UPDATE, DELETE, TRUNCATE


-- ---------------------------------------------------
-- B. MANAGER (Управляющий)
-- ---------------------------------------------------
-- Менеджер управляет меню, продуктами, акциями и сотрудниками
GRANT SELECT, INSERT, UPDATE, DELETE ON
    pizzeria_schema.menus,
    pizzeria_schema.products,
    pizzeria_schema.ingredients,
    pizzeria_schema.product_ingredients,
    pizzeria_schema.promotions,
    pizzeria_schema.employees,
    pizzeria_schema.inventory_movements
    TO pizzeria_manager_role;

-- Менеджер также видит и правит заказы
GRANT SELECT, INSERT, UPDATE ON
    pizzeria_schema.orders,
    pizzeria_schema.order_items,
    pizzeria_schema.payments,
    pizzeria_schema.deliveries,
    pizzeria_schema.delivery_info
    TO pizzeria_manager_role;


-- ---------------------------------------------------
-- C. WAITER (Официант)
-- ---------------------------------------------------
-- Официант только ЧИТАЕТ меню (не может менять цены)
GRANT SELECT ON
    pizzeria_schema.menus,
    pizzeria_schema.products,
    pizzeria_schema.product_ingredients,
    pizzeria_schema.promotions
    TO pizzeria_waiter_role;

-- Официант СОЗДАЕТ и ОБНОВЛЯЕТ заказы
GRANT SELECT, INSERT, UPDATE ON
    pizzeria_schema.orders,
    pizzeria_schema.order_items,
    pizzeria_schema.order_promotions,
    pizzeria_schema.payments,
    pizzeria_schema.payment_company_details
    TO pizzeria_waiter_role;

-- Официанту нужен доступ к складу, так как при продаже (INSERT order)
-- срабатывает ТРИГГЕР, который обновляет ingredients.
GRANT SELECT, UPDATE ON pizzeria_schema.ingredients TO pizzeria_waiter_role;
GRANT INSERT ON pizzeria_schema.inventory_movements TO pizzeria_waiter_role;


-- ---------------------------------------------------
-- D. COURIER (Курьер)
-- ---------------------------------------------------
-- Курьер видит, куда везти (адрес) и сам заказ
GRANT SELECT ON
    pizzeria_schema.orders,
    pizzeria_schema.delivery_info
    TO pizzeria_courier_role;

-- Курьер может менять ТОЛЬКО статус своей доставки
GRANT SELECT, UPDATE ON pizzeria_schema.deliveries TO pizzeria_courier_role;

-- Курьеру не нужно видеть ингредиенты, меню или зарплаты других.
-- (Никаких GRANT больше не даем)
