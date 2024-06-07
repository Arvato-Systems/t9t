-------------------------------------
-- Drop all triggers
-------------------------------------

DO $$
DECLARE trigger_record RECORD;
BEGIN
    RAISE NOTICE 'DROPPING TRIGGERS...';
    FOR trigger_record IN
        SELECT trigger_name, event_object_table
        FROM information_schema.triggers
        WHERE trigger_schema NOT IN ('pg_catalog', 'information_schema')
    LOOP
        EXECUTE 'DROP TRIGGER IF EXISTS ' || quote_ident(trigger_record.trigger_name) || ' ON ' || quote_ident(trigger_record.event_object_table) || ' CASCADE';
        RAISE NOTICE 'Trigger % for table % dropped', trigger_record.trigger_name, trigger_record.event_object_table;
    END LOOP;

    RAISE NOTICE 'ALL TRIGGERS DROPPED';
END $$;
