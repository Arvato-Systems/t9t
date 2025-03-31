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
