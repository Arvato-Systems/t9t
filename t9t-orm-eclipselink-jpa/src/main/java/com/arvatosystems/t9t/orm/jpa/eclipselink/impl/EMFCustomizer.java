/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;

import de.jpaw.dp.Singleton;

@Singleton
public class EMFCustomizer implements IEMFCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMFCustomizer.class);
    private static final String DIALECT_KEY = "eclipselink.target-database";

    private static void putOpt(final Map<String, Object> myProps, final String key, final String value) {
        if (value != null)
            myProps.put(key,  value);
    }

    @Override
    public EntityManagerFactory getCustomizedEmf(final String puName, final RelationalDatabaseConfiguration settings) throws Exception {
        final Map<String, Object> myProps = new HashMap<>();

        putOpt(myProps, "jakarta.persistence.jdbc.driver",   settings.getJdbcDriverClass());
        putOpt(myProps, "jakarta.persistence.jdbc.url",      settings.getJdbcConnectString());
        putOpt(myProps, "jakarta.persistence.jdbc.user",     settings.getUsername());
        putOpt(myProps, "jakarta.persistence.jdbc.password", settings.getPassword());

        final DatabaseBrandType dbName = settings.getDatabaseBrand();
        if (dbName != null) {
            switch (dbName) {
            case HANA:
                myProps.put(DIALECT_KEY, "HANA");
                break;
            case MS_SQL_SERVER:
                myProps.put(DIALECT_KEY, "SQLServer");
                break;
            case ORACLE:
                myProps.put(DIALECT_KEY, "Oracle11");
                break;
            case POSTGRES:
                myProps.put(DIALECT_KEY, "PostgreSQL");
                break;
            default:
                break;
            }
        }

        // also transfer custom parameters, if provided
        if (settings.getZ() != null) {
            for (final String customSetting: settings.getZ()) {
                final int equalsPos = customSetting.indexOf('=');
                if (equalsPos > 0) {
                    // store key/value pair
                    final String key = customSetting.substring(0, equalsPos - 1).trim();
                    final String value = customSetting.substring(equalsPos + 1).trim();
                    LOGGER.info("Setting custom value for persistence.xml: key = {}, value = {}", key, value);
                    myProps.put(key, value);
                } else {
                    LOGGER.warn("Custom (z field) entry {} has not '=' delimiter, ignoring entry", customSetting);
                }
            }
        }

        return Persistence.createEntityManagerFactory(puName, myProps);
    }
}
