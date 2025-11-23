-- -----------------------------------------------------
-- RAPORT: Przegląd i definicje indeksów
-- -----------------------------------------------------
SELECT
    tablename AS "Tabela",
    indexname AS "Nazwa indeksu",
    pg_size_pretty(pg_relation_size(quote_ident(schemaname) || '.' || quote_ident(indexname))) AS "Rozmiar",
    indexdef AS "Definicja (DDL)"
FROM
    pg_indexes
WHERE
    schemaname = 'pizzeria_schema' -- Tylko nasz schemat
ORDER BY
    tablename,
    indexname;