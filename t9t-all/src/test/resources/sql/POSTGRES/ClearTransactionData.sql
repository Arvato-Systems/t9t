DO $$

declare

pref_1_table_names text[2] default '{"p28_", "p42_"}';
pref_2_table_names text[3] default '{"dat_", "int_", "log_"}';
exclude_table_names text := 'p28_dat_update_status, p28_dat_update_status_log, p28_dat_bucket_counter, p42_dat_passwords, p42_dat_user_states';
-- Set schemaname: 'main', 'public', 'cart', 'avstock'
cur_schema_name text := 'cart';
p28_dat_bucket_counter_name text := 'p28_dat_bucket_counter';

pref_1_table_name text;
pref_2_table_name text;
table_bucket_counter_updated int := 0;
table_names_rec     record;
table_names_cur     refcursor;

begin
raise notice 'Start -------------------------------';

    OPEN table_names_cur FOR EXECUTE 'SELECT tablename FROM pg_catalog.pg_tables WHERE schemaname = ''' || cur_schema_name || ''';';

    LOOP
        fetch table_names_cur into table_names_rec;
        exit when not found;
        raise notice '  table_name: %', table_names_rec.tablename;

        FOREACH pref_1_table_name IN ARRAY pref_1_table_names
        LOOP
            raise notice '    pref_1_table_name: %', pref_1_table_name;

            FOREACH pref_2_table_name IN ARRAY pref_2_table_names
            LOOP
                raise notice '      pref_2_table_name: %', pref_2_table_name;

                    IF (position(table_names_rec.tablename in exclude_table_names) = 0) and (position((pref_1_table_name || pref_2_table_name) in table_names_rec.tablename) > 0)
                    THEN
                        -- truncate table
                        raise notice '        *** TRUNCATE TABLE: %', table_names_rec.tablename;
                        --EXECUTE 'TRUNCATE ' || cur_schema_name || '.' || table_names_rec.tablename || ';';

                    ELSIF (table_names_rec.tablename = p28_dat_bucket_counter_name) and (table_bucket_counter_updated = 0)
                    THEN
                        raise notice '        +++ UPDATE p28_dat_bucket_counter';
                        --EXECUTE 'UPDATE ' || cur_schema_name || '.' || p28_dat_bucket_counter_name || ' SET current_val = 0;'
                        --COMMIT;
                        table_bucket_counter_updated := 1;
                    ELSE -- only for testing
                        raise notice '        ~~~ NOTHING TODO';
                    END IF;

            END LOOP; -- loop dat_, int_, log_

        END LOOP; -- loop p28_, p42_

    END LOOP;

    CLOSE table_names_cur;
raise notice 'End -------------------------------';
end;
$$;
