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

-- For MS SQL server, in this example, Cambridge is the database. The connect right is assigned to the user, not the role.
-- The database must be selected first.
USE [Cambridge]
GO
CREATE ROLE [p42ro]
GO
USE [Cambridge]
GO
CREATE ROLE [p42rw]
GO
USE [Cambridge]
GO
CREATE ROLE [p42user]
GO
USE [Cambridge]
GO
CREATE ROLE [p42app]
GO
USE [Cambridge]
GO
CREATE ROLE [p42adm]
GO

--Vergabe der Rechte zu einem bestimmten Schema
use [Cambridge]
GO
GRANT UPDATE ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT ALTER ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT EXECUTE ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT SELECT ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT VIEW DEFINITION ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT INSERT ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT DELETE ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT CONTROL ON SCHEMA::[Cambridge] TO [p42adm]
GO
use [Cambridge]
GO
GRANT REFERENCES ON SCHEMA::[Cambridge] TO [p42adm]
GO
