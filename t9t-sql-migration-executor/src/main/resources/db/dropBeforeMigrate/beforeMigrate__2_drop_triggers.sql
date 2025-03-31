--
-- Copyright (c) 2012 - 2025 Arvato Systems GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

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
