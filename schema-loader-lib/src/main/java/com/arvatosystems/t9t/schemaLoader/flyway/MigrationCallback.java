/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.schemaLoader.flyway;

import java.sql.Connection;

import org.flywaydb.core.api.callback.BaseCallback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;

import com.arvatosystems.t9t.schemaLoader.SchemaElements;
import com.arvatosystems.t9t.schemaLoader.config.SchemaLoaderConfiguration;


public class MigrationCallback extends BaseCallback {

    private final SchemaElements schemaElements;
    private final SchemaLoaderConfiguration configuration;

    public MigrationCallback(SchemaLoaderConfiguration configuration, SchemaElements schemaElements) {
        this.configuration = configuration;
        this.schemaElements = schemaElements;
    }

    @Override
    public boolean supports(Event event, Context context) {
        return Event.BEFORE_MIGRATE.equals(event) || Event.AFTER_MIGRATE.equals(event);
    }

    @Override
    public void handle(Event event, Context context) {
        if (Event.BEFORE_MIGRATE.equals(event)) {
            beforeMigrate(context.getConnection());
        } else if (Event.AFTER_MIGRATE.equals(event)) {
            afterMigrate(context.getConnection());
        }
    }

    private void beforeMigrate(Connection connection) {
        for (String dbObjectType : configuration.getMigration()
                .getPreDrop()) {
            schemaElements.executeDrop(connection, dbObjectType);
        }
    }

    private void afterMigrate(Connection connection) {
        for (String dbObjectType : configuration.getMigration()
                .getPostCreate()) {
            schemaElements.executeCreate(connection, dbObjectType);
        }
    }
}
