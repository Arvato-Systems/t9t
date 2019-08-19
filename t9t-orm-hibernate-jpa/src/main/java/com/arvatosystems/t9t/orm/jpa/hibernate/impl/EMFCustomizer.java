/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;

import de.jpaw.dp.Singleton;

@Singleton
public class EMFCustomizer implements IEMFCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMFCustomizer.class);
    private static final String DIALECT_KEY = "hibernate.dialect";

    private static void putOpt(Map<String, Object> myProps, String key, String value) {
        if (value != null) {
            myProps.put(key,  value);
        }
    }

    /**
     * Add properties for creation of the EntityManagerFactory
     */
    protected void configureProperties(Map<String, Object> properties) {
        // Extension point for customization
    }

    @Override
    public EntityManagerFactory getCustomizedEmf(String puName, RelationalDatabaseConfiguration settings) throws Exception {
        final Map<String, Object> myProps = new HashMap<>();

        configureProperties(myProps);

        putOpt(myProps, "javax.persistence.jdbc.driver",   settings.getJdbcDriverClass());
        putOpt(myProps, "javax.persistence.jdbc.url",      settings.getJdbcConnectString());
        putOpt(myProps, "javax.persistence.jdbc.user",     settings.getUsername());
        putOpt(myProps, "javax.persistence.jdbc.password", settings.getPassword());

        // see http://docs.jboss.org/hibernate/orm/4.3/javadocs/org/hibernate/dialect/package-summary.html
        DatabaseBrandType dbName = settings.getDatabaseBrand();
        if (dbName != null) {
            switch (dbName) {
            case HANA:
                myProps.put(DIALECT_KEY, "org.hibernate.dialect.HANARowStoreDialect");
                break;
            case MS_SQL_SERVER:
                myProps.put(DIALECT_KEY, "org.hibernate.dialect.SQLServer2012Dialect");
                break;
            case ORACLE:
                myProps.put(DIALECT_KEY, "org.hibernate.dialect.Oracle12cDialect");
                break;
            case POSTGRES:
                myProps.put(DIALECT_KEY, "org.hibernate.dialect.PostgreSQL94Dialect");
                break;
            case H2:
                myProps.put(DIALECT_KEY, "org.hibernate.dialect.H2Dialect");
                break;
            default:
                break;
            }
        }

        // also transfer custom parameters, if provided
        if (settings.getZ() != null) {
            for (String customSetting: settings.getZ()) {
                int equalsPos = customSetting.indexOf('=');
                if (equalsPos > 0) {
                    // store key/value pair
                    String key = customSetting.substring(0, equalsPos).trim();
                    String value = customSetting.substring(equalsPos+1).trim();
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
