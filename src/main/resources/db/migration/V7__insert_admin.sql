INSERT INTO pizzeria_schema.employees
(name, last_name, phone, login, password_hash, role, created_at, completed_orders_count)
VALUES
    ('Admin', 'Boss', '000-000-000', 'admin',
     '$2a$12$4k6sDEmkHYFC3lmwT6lameoK0qpqvyjtRtbtKmwWQYdEkxaZ7VV6G',
     'manager',
     CURRENT_TIMESTAMP,
     0)
ON CONFLICT (login) DO UPDATE
    SET password_hash = EXCLUDED.password_hash;
