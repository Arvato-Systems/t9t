-------------------------------------
-- Drop all views
-------------------------------------

DO $$
DECLARE view_record RECORD;
BEGIN
    RAISE NOTICE 'DROPPING VIEWS...';
    FOR view_record IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_type = 'VIEW' AND table_schema NOT IN ('pg_catalog', 'information_schema')
    LOOP
        EXECUTE 'DROP VIEW IF EXISTS ' || quote_ident(view_record.table_name) || ' CASCADE';
        RAISE NOTICE 'View % dropped', view_record.table_name;
    END LOOP;

    RAISE NOTICE 'ALL VIEWS DROPPED';
END $$;
