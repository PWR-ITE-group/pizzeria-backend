TRUNCATE 
    pizzeria_schema.inventory_movements,
    pizzeria_schema.delivery_info,
    pizzeria_schema.deliveries,
    pizzeria_schema.order_promotions,
    pizzeria_schema.order_items,
    pizzeria_schema.orders,
    pizzeria_schema.product_ingredients,
    pizzeria_schema.products,
    pizzeria_schema.ingredients,
    pizzeria_schema.menus,
    pizzeria_schema.employees,
    pizzeria_schema.promotions
CASCADE;

ALTER SEQUENCE pizzeria_schema.employees_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.menus_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.products_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.ingredients_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.orders_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.deliveries_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.delivery_info_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.order_items_id_seq RESTART WITH 1;
ALTER SEQUENCE pizzeria_schema.inventory_movements_id_seq RESTART WITH 1;