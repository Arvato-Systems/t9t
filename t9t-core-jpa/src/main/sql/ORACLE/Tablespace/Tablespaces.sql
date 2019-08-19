--
-- Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

CREATE SMALLFILE TABLESPACE rts42cfg0I LOGGING DATAFILE '/oracle/db/fortytwo/rts42cfg0I.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42cfg0D LOGGING DATAFILE '/oracle/db/fortytwo/rts42cfg0D.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42dat0D LOGGING DATAFILE '/oracle/db/fortytwo/rts42dat0D.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42dat0I LOGGING DATAFILE '/oracle/db/fortytwo/rts42dat0I.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42log0D LOGGING DATAFILE '/oracle/db/fortytwo/rts42log0D.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42log0I LOGGING DATAFILE '/oracle/db/fortytwo/rts42log0I.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42his0D LOGGING DATAFILE '/oracle/db/fortytwo/rts42his0D.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;

CREATE SMALLFILE TABLESPACE rts42his0I LOGGING DATAFILE '/oracle/db/fortytwo/rts42his0I.dbf'
SIZE 10M
SEGMENT SPACE MANAGEMENT AUTO
EXTENT MANAGEMENT LOCAL AUTOALLOCATE
online;
