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
