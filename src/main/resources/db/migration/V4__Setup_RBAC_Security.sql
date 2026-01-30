
DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_client_role') THEN
            CREATE ROLE pizzeria_client_role;
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_waiter_role') THEN
            CREATE ROLE pizzeria_waiter_role;
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_chef_role') THEN
            CREATE ROLE pizzeria_chef_role;
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_courier_role') THEN
            CREATE ROLE pizzeria_courier_role;
        END IF;

        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_manager_role') THEN
            CREATE ROLE pizzeria_manager_role;
        END IF;
    END
$$;

GRANT USAGE ON SCHEMA pizzeria_schema TO
    pizzeria_client_role, pizzeria_waiter_role, pizzeria_chef_role, pizzeria_courier_role, pizzeria_manager_role;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA pizzeria_schema TO
    pizzeria_client_role, pizzeria_waiter_role, pizzeria_chef_role, pizzeria_courier_role, pizzeria_manager_role;


GRANT SELECT ON pizzeria_schema.menus, pizzeria_schema.products, pizzeria_schema.ingredients, pizzeria_schema.promotions
    TO pizzeria_client_role;


GRANT INSERT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.delivery_info, pizzeria_schema.payments, pizzeria_schema.order_promotions
    TO pizzeria_client_role;

GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.payments, pizzeria_schema.delivery_info
    TO pizzeria_client_role;


GRANT SELECT ON pizzeria_schema.menus, pizzeria_schema.products, pizzeria_schema.ingredients
    TO pizzeria_waiter_role;


GRANT INSERT, UPDATE ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.order_promotions
    TO pizzeria_waiter_role;

GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.payments, pizzeria_schema.payment_company_details
    TO pizzeria_waiter_role;

GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items
    TO pizzeria_waiter_role;


GRANT SELECT ON pizzeria_schema.orders, pizzeria_schema.order_items, pizzeria_schema.products
    TO pizzeria_chef_role;

GRANT UPDATE (status, updated_at) ON pizzeria_schema.orders TO pizzeria_chef_role;
GRANT UPDATE (status) ON pizzeria_schema.order_items TO pizzeria_chef_role;

GRANT SELECT, UPDATE ON pizzeria_schema.ingredients TO pizzeria_chef_role;
GRANT INSERT ON pizzeria_schema.inventory_movements TO pizzeria_chef_role;



GRANT SELECT ON pizzeria_schema.delivery_info, pizzeria_schema.orders
    TO pizzeria_courier_role;

GRANT SELECT, UPDATE ON pizzeria_schema.deliveries TO pizzeria_courier_role;
GRANT UPDATE (status, updated_at) ON pizzeria_schema.orders TO pizzeria_courier_role;


GRANT ALL PRIVILEGES ON
    pizzeria_schema.menus,
    pizzeria_schema.products,
    pizzeria_schema.ingredients,
    pizzeria_schema.product_ingredients,
    pizzeria_schema.promotions
    TO pizzeria_manager_role;

GRANT ALL PRIVILEGES ON pizzeria_schema.employees TO pizzeria_manager_role;

GRANT ALL PRIVILEGES ON pizzeria_schema.inventory_movements TO pizzeria_manager_role;

GRANT SELECT ON
    pizzeria_schema.orders,
    pizzeria_schema.order_items,
    pizzeria_schema.payments,
    pizzeria_schema.deliveries,
    pizzeria_schema.delivery_info,
    pizzeria_schema.payment_company_details
    TO pizzeria_manager_role;
