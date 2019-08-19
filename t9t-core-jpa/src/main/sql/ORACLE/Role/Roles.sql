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

CREATE ROLE p42ro NOT IDENTIFIED;
/
GRANT CREATE SESSION TO p42ro;
/

CREATE ROLE p42rw NOT IDENTIFIED;
/
GRANT CREATE SESSION TO p42rw;
/

CREATE ROLE p42user NOT IDENTIFIED;
/
GRANT CREATE SESSION TO p42user;
/

CREATE ROLE p42app NOT IDENTIFIED;
/
GRANT CREATE SESSION TO p42app;
/

CREATE ROLE p42adm NOT IDENTIFIED;
/
GRANT CREATE SESSION TO p42adm;
/
GRANT CREATE TABLE     TO p42adm;
/
GRANT QUERY REWRITE    TO p42adm;
/
GRANT CREATE VIEW      TO p42adm;
/
GRANT CREATE SEQUENCE  TO p42adm;
/
GRANT CREATE TRIGGER   TO p42adm;
/
GRANT CREATE PUBLIC SYNONYM TO p42adm;
/
GRANT CREATE TYPE TO p42adm;
/
