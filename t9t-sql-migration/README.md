```
  Copyright (c) 2012 - 2018 Arvato Systems GmbH

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```

T9T Database Migration Scripts
==============================

This module contains the T9T database migration scripts
in `src/main/sql`.

As with all other database scripts, those scripts are located in
a directory structure `database type`/`database element`/`script`.sql
where `database element` is always `Migration` for the migration scripts of this package.

The naming of the database scripts follows the following naming schema:

```
V[major].[minor].[timestamp]__[description].sql
```

Where `major` and `minor` are the current T9T major and minor versions
and the `timestamp` is the timestamp the migration has been created in format
`yyyyMMddHHmm`.

A migration created on 03/01/2018 14:26 in release 3.3 would be named `V3.3.201801031426__MyMigration.sql`.

Note: Using the timestamp instead of a fixed patch level number allows more flexible handling when it comes to merging
of changes which include database migrations.
