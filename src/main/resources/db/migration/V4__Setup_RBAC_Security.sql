-- -----------------------------------------------------
-- SECURITY: Role Based Access Control (RBAC)
-- -----------------------------------------------------

-- 1. Create Roles (Groups)
-- We use a DO block to avoid errors if roles already exist
DO $$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_manager_role') THEN
            CREATE ROLE pizzeria_manager_role;
        END IF;
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_staff_role') THEN
            CREATE ROLE pizzeria_staff_role;
        END IF;
        -- This is a read-only role for analytics/reporting tools
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'pizzeria_reporter_role') THEN
            CREATE ROLE pizzeria_reporter_role;
        END IF;
    END
$$;

-- -----------------------------------------------------
-- 2. Permissions for MANAGER (Full Control)
-- -----------------------------------------------------
-- Managers can do everything in the schema
GRANT ALL PRIVILEGES ON SCHEMA pizzeria_schema TO pizzeria_manager_role;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA pizzeria_schema TO pizzeria_manager_role;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA pizzeria_schema TO pizzeria_manager_role;

-- -----------------------------------------------------
-- 3. Permissions for STAFF (Waiters/Couriers)
-- -----------------------------------------------------
-- Allow usage of the schema
GRANT USAGE ON SCHEMA pizzeria_schema TO pizzeria_staff_role;

-- Orders & Deliveries: Staff can Read, Create, and Update (change status), but NOT Delete orders
GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.orders TO pizzeria_staff_role;
GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.order_items TO pizzeria_staff_role;
GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.deliveries TO pizzeria_staff_role;
GRANT SELECT, INSERT, UPDATE ON pizzeria_schema.delivery_info TO pizzeria_staff_role;

-- Menu & Products: Staff can ONLY Read (they shouldn't change prices or delete pizzas)
GRANT SELECT ON pizzeria_schema.menus TO pizzeria_staff_role;
GRANT SELECT ON pizzeria_schema.products TO pizzeria_staff_role;
GRANT SELECT ON pizzeria_schema.ingredients TO pizzeria_staff_role;
GRANT SELECT ON pizzeria_schema.product_ingredients TO pizzeria_staff_role;

-- Sequences: Needed for INSERTs to work (to generate IDs)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA pizzeria_schema TO pizzeria_staff_role;

-- -----------------------------------------------------
-- 4. Permissions for REPORTER (Read Only)
-- -----------------------------------------------------
GRANT USAGE ON SCHEMA pizzeria_schema TO pizzeria_reporter_role;
GRANT SELECT ON ALL TABLES IN SCHEMA pizzeria_schema TO pizzeria_reporter_role;

-- -----------------------------------------------------
-- 5. Create Specific Users and Assign Roles
-- -----------------------------------------------------
-- Ideally, you do not store passwords in Flyway scripts.
-- You should create the users manually or via a secure script.
-- However, here is how you assign the logic:

/*
 -- Example of creating a limited app user for daily operations:
 CREATE USER app_service_user WITH PASSWORD 'secure_password';
 GRANT pizzeria_staff_role TO app_service_user;

 -- Example of creating an admin user:
 CREATE USER admin_user WITH PASSWORD 'secure_admin_password';
 GRANT pizzeria_manager_role TO admin_user;
*/