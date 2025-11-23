-- -----------------------------------------------------
-- SECURITY: RBAC based on Use Case Diagram
-- -----------------------------------------------------

-- 1. Создание ролей (Actors)
DO $$
    BEGIN
        -- Актер: Klient
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_client_role') THEN
            CREATE ROLE pizzeria_client_role;
        END IF;

        -- Актер: Obsługa lokalu
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_waiter_role') THEN
            CREATE ROLE pizzeria_waiter_role;
        END IF;

        -- Актер: Kucharz
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_chef_role') THEN
            CREATE ROLE pizzeria_chef_role;
        END IF;

        -- Актер: Dostawca
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_courier_role') THEN
            CREATE ROLE pizzeria_courier_role;
        END IF;

        -- Актер: Meneger
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_manager_role') THEN
            CREATE ROLE pizzeria_manager_role;
        END IF;
    END
$$;

-- 2. Базовый доступ (нужен всем для подключения и простых операций)
GRANT USAGE ON SCHEMA pizzeria_schema TO
    pizzeria_client_role, pizzeria_waiter_role, pizzeria_chef_role, pizzeria_courier_role, pizzeria_manager_role;

-- Всем нужен доступ к генераторам ID (SERIAL), чтобы делать INSERT
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA pizzeria_schema TO
    pizzeria_client_role, pizzeria_waiter_role, pizzeria_chef_role, pizzeria_courier_role, pizzeria_manager_role;


-- ==================================================================
-- 3. НАСТРОЙКА ПРАВ ПО USE CASES
-- ==================================================================

-- ---------------------------------------------------
-- A. ROLE: KLIENT (Клиент)
-- ---------------------------------------------------
-- UC1: Przeglądanie menu
-- UC2: Konfiguracja pizzy (нужен доступ к чтению ингредиентов)
GRANT SELECT ON pizzeria_schema.menus, pizzeria_schema.products, pizzeria_schema.ingredients, pizzeria_schema.promotions
    TO pizzeria_client_role;

-- UC3: Składanie zamówień (Создание заказа и позиций)
-- UC6: Dokonanie płatności (Создание платежа)
GRANT INSERT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.delivery_info, pizzeria_schema.payments, pizzeria_schema.order_promotions
    TO pizzeria_client_role;

-- UC4, UC5: Przeglądanie statusu/historii (Своего заказа)
GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.payments, pizzeria_schema.delivery_info
    TO pizzeria_client_role;


-- ---------------------------------------------------
-- B. ROLE: WAITER (Obsługa lokalu)
-- ---------------------------------------------------
-- UC1: Przeglądanie menu (Чтение)
GRANT SELECT ON pizzeria_schema.menus, pizzeria_schema.products, pizzeria_schema.ingredients
    TO pizzeria_waiter_role;

-- UC7: Rejestracja zamówień (Создание заказа в зале)
-- UC8: Przypisywanie zamówień do stolików (UPDATE orders)
GRANT INSERT, UPDATE ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.order_promotions
    TO pizzeria_waiter_role;

-- UC9: Rozliczenie zamówienia (Прием оплаты)
GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.payments, pizzeria_schema.payment_company_details
    TO pizzeria_waiter_role;

-- Waiter должен видеть заказы, чтобы их обслуживать
GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items
    TO pizzeria_waiter_role;


-- ---------------------------------------------------
-- C. ROLE: CHEF (Kucharz)
-- ---------------------------------------------------
-- UC9 (Chef): Podgląd listy zamówień (Что готовить)
GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.products
    TO pizzeria_chef_role;

-- UC10: Zmiana statusu zamówienia (Например: new -> preparing -> ready)
GRANT UPDATE (status, updated_at) ON pizzeria_schema.orders TO pizzeria_chef_role;
GRANT UPDATE (status) ON pizzeria_schema.order_items TO pizzeria_chef_role;

-- UC11: Zmiana ilości produktów w magazynie (Списание, инвентаризация)
GRANT SELECT, UPDATE ON pizzeria_schema.ingredients TO pizzeria_chef_role;
GRANT INSERT ON pizzeria_schema.inventory_movements TO pizzeria_chef_role;


-- ---------------------------------------------------
-- D. ROLE: COURIER (Dostawca)
-- ---------------------------------------------------
-- UC12: Podgląd danych klientów (Куда везти)
GRANT SELECT ON pizzeria_schema.delivery_info, pizzeria_schema.orders
    TO pizzeria_courier_role;

-- UC10 (Courier): Zmiana statusu (Например: in_transit -> delivered)
-- Курьер работает с таблицей deliveries и orders
GRANT SELECT, UPDATE ON pizzeria_schema.deliveries TO pizzeria_courier_role;
GRANT UPDATE (status, updated_at) ON pizzeria_schema.orders TO pizzeria_courier_role;


-- ---------------------------------------------------
-- E. ROLE: MANAGER (Meneger)
-- ---------------------------------------------------
-- UC13, UC14, UC17, UC18, UC19: Полное управление Меню и Продуктами
GRANT ALL PRIVILEGES ON
    pizzeria_schema.menus,
    pizzeria_schema.products,
    pizzeria_schema.ingredients,
    pizzeria_schema.product_ingredients,
    pizzeria_schema.promotions
    TO pizzeria_manager_role;

-- UC15, UC16: Управление сотрудниками (Добавление/Удаление)
GRANT ALL PRIVILEGES ON pizzeria_schema.employees TO pizzeria_manager_role;

-- UC11 (Manager): Управление складом
GRANT ALL PRIVILEGES ON pizzeria_schema.inventory_movements TO pizzeria_manager_role;

-- Доступ к просмотру финансов и заказов (Read Only для контроля)
GRANT SELECT ON
    pizzeria_schema.orders,
    pizzeria_schema.order_items,
    pizzeria_schema.payments,
    pizzeria_schema.deliveries,
    pizzeria_schema.delivery_info,
    pizzeria_schema.payment_company_details
    TO pizzeria_manager_role;
