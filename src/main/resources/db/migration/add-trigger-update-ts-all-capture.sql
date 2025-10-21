CREATE OR REPLACE FUNCTION change_modified_at()
  RETURNS TRIGGER
  LANGUAGE PLPGSQL
  AS
$$
BEGIN
	NEW.data_change_ts := NOW();
RETURN NEW;
END;
$$
;

SELECT
        'CREATE TRIGGER '
        || tab_name
        || ' BEFORE UPDATE OR INSERT ON ALL DATABASE FOR EACH ROW EXECUTE PROCEDURE change_modified_at();' AS trigger_creation_query
FROM (
         SELECT
                 quote_ident(table_schema) || '.' || quote_ident(table_name) as tab_name
         FROM
             information_schema.tables
         WHERE
                 table_schema NOT IN ('pg_catalog', 'information_schema')
           AND table_schema NOT LIKE 'pg_toast%'
     ) tablist;