CREATE OR REPLACE FUNCTION truncate_all_tables()
  RETURNS VOID AS $$
DECLARE
  tbl_name TEXT;
BEGIN
  FOR tbl_name IN
  SELECT table_name
  FROM information_schema.tables
  WHERE table_schema = 'public'
      AND table_type = 'BASE TABLE'
      AND table_name != 'schema_version'
  LOOP
    EXECUTE 'TRUNCATE TABLE ' || tbl_name || ' CASCADE;';
  END LOOP;
END;
$$ LANGUAGE PLPGSQL;