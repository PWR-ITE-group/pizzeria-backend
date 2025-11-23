-- -----------------------------------------------------
-- RAPORT: Macierz dostępu (Role Based Access Control)
-- -----------------------------------------------------
SELECT
    grantee AS "Rola (Aktor)",
    table_name AS "Tabela",
    -- Agregujemy uprawnienia w jeden ciąg tekstowy po przecinku
    STRING_AGG(privilege_type, ', ' ORDER BY privilege_type) AS "Lista uprawnień",
    -- Tłumaczymy poziom dostępu na ludzki język
    CASE
        WHEN COUNT(*) = 4 THEN '⚡ PEŁNY DOSTĘP (CRUD)'
        WHEN STRING_AGG(privilege_type, ', ') LIKE '%UPDATE%' OR STRING_AGG(privilege_type, ', ') LIKE '%INSERT%' THEN '✏️ Odczyt i Zapis'
        ELSE '👀 Tylko Odczyt'
        END AS "Poziom dostępu"
FROM
    information_schema.role_table_grants
WHERE
    table_schema = 'pizzeria_schema'
  AND grantee LIKE 'pizzeria_%' -- Filtrujemy tylko nasze role
GROUP BY
    grantee,
    table_name
ORDER BY
    grantee,
    table_name;