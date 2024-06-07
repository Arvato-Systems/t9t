-------------------------------------
-- Drop all functions
-------------------------------------

DO $$
DECLARE function_record RECORD;
BEGIN
    RAISE NOTICE 'DROPPING FUNCTIONS...';
    FOR function_record IN
        SELECT routine_name
        FROM information_schema.routines
        WHERE routine_type = 'FUNCTION' AND specific_schema NOT IN ('pg_catalog', 'information_schema')
    LOOP
        EXECUTE 'DROP FUNCTION IF EXISTS ' || function_record.routine_name || ' CASCADE';
        RAISE NOTICE 'Function % dropped', function_record.routine_name;
    END LOOP;

    RAISE NOTICE 'ALL FUNCTIONS DROPPED';
END $$;
