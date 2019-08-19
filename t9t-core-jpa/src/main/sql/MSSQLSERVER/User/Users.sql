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

-- users are create for the whole SQL server, therefore reference the master application
USE [master]
GO
CREATE LOGIN [appp42] WITH PASSWORD=N'DBA', DEFAULT_DATABASE=[Cambridge], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO
USE [Cambridge]
GO
CREATE USER [appp42] FOR LOGIN [appp42]
GO
USE [Cambridge]
GO
EXEC sp_addrolemember N'p42adm', N'appp42'
GO
USE [master]
GO
CREATE LOGIN [fortytwo] WITH PASSWORD=N'fortytwopw', DEFAULT_DATABASE=[Cambridge], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO
USE [Cambridge]
GO
CREATE USER [fortytwo] FOR LOGIN [fortytwo]
GO
USE [Cambridge]
GO
EXEC sp_addrolemember N'p42user', N'fortytwo'
GO
