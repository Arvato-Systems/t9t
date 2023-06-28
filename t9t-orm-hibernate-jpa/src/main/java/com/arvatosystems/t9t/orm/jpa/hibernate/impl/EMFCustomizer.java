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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HANARowStoreDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IEMFCustomizer;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.init.InitContainers;

import de.jpaw.dp.Singleton;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Persistence;

@Singleton
public class EMFCustomizer implements IEMFCustomizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(EMFCustomizer.class);

    private static void putOpt(final Map<String, Object> myProps, final String key, final String value) {
        if (value != null) {
            myProps.put(key,  value);
        }
    }

    /**
     * Add properties for creation of the EntityManagerFactory
     */
    protected void configureProperties(final Map<String, Object> properties) {
        // Extension point for customization
    }

    @Override
    public EntityManagerFactory getCustomizedEmf(final String puName, final RelationalDatabaseConfiguration settings) throws Exception {
        final Map<String, Object> myProps = new HashMap<>();

        configureProperties(myProps);

        putOpt(myProps, "jakarta.persistence.jdbc.driver",   settings.getJdbcDriverClass());
        putOpt(myProps, "jakarta.persistence.jdbc.url",      settings.getJdbcConnectString());
        putOpt(myProps, "jakarta.persistence.jdbc.user",     settings.getUsername());
        putOpt(myProps, "jakarta.persistence.jdbc.password", settings.getPassword());

        // see http://docs.jboss.org/hibernate/orm/4.3/javadocs/org/hibernate/dialect/package-summary.html
        final DatabaseBrandType dbName = settings.getDatabaseBrand();
        if (dbName != null) {
            switch (dbName) {
            case HANA:
                myProps.put(AvailableSettings.DIALECT, HANARowStoreDialect.class.getCanonicalName());
                break;
            case MS_SQL_SERVER:
                myProps.put(AvailableSettings.DIALECT, SQLServerDialect.class.getCanonicalName());
                myProps.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MAJOR_VERSION, 11);  // SQL server 2012
                break;
            case ORACLE:
                myProps.put(AvailableSettings.DIALECT, OracleDialect.class.getCanonicalName());
                myProps.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MAJOR_VERSION, 12);  // Oracle12c
                break;
            case POSTGRES:
                myProps.put(AvailableSettings.DIALECT, PostgreSQLDialect.class.getCanonicalName());
                // the code below is not deprecated but seems to require hibernate.temp.use_jdbc_metadata_defaults in persistence.xml, which is slow at startup
//                myProps.put(AvailableSettings.DIALECT, PostgreSQLDialect.class.getCanonicalName());
//                myProps.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MAJOR_VERSION, 9);
//                myProps.put(AvailableSettings.JAKARTA_HBM2DDL_DB_MINOR_VERSION, 5);
                break;
            case H2:
                myProps.put(AvailableSettings.DIALECT, H2Dialect.class.getCanonicalName());
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
                    final String key = customSetting.substring(0, equalsPos).trim();
                    final String value = customSetting.substring(equalsPos + 1).trim();
                    LOGGER.info("Setting custom value for persistence.xml: key = {}, value = {}", key, value);
                    myProps.put(key, value);
                } else {
                    LOGGER.warn("Custom (z field) entry {} has not '=' delimiter, ignoring entry", customSetting);
                }
            }
        }

        final Set<Class<?>> mcl = InitContainers.getClassesAnnotatedWith(MappedSuperclass.class);
        final Set<Class<?>> entities = InitContainers.getClassesAnnotatedWith(Entity.class);

        if (LOGGER.isTraceEnabled()) {
            // next lines are just for user info
            for (final Class<?> e : mcl) {
                LOGGER.trace("Found mapped Superclass class {}", e.getCanonicalName());
            }

            for (final Class<?> e : entities) {
                LOGGER.trace("Found entity class {}", e.getCanonicalName());
            }
        }
        LOGGER.info("Found {} mapped superclasses and {} entities", mcl.size(), entities.size());

        final Set<Class<?>> allClasses = new HashSet<Class<?>>(mcl.size() + entities.size() + 10);
        allClasses.addAll(mcl);
        allClasses.addAll(entities);
        // also must add the Attribute converter classes
        addAttributeConverters(allClasses);
        LOGGER.info("Total number of classes (including attribute converters) is {}", allClasses.size());

        myProps.put(AvailableSettings.LOADED_CLASSES, new ArrayList<Class<?>>(allClasses));

        return Persistence.createEntityManagerFactory(puName, myProps);
    }

    /** Hook to allow customization of added attribute converters (by adding more / others or removing some entities). */
    protected void addAttributeConverters(final Set<Class<?>> allClasses) {
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterByteArray.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterFpMicroUnits.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterCompactBonaPortable.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterCompactJsonArray.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterCompactJsonElement.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterCompactJsonObject.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterStringJsonArray.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterStringJsonElement.class);
        allClasses.add(de.jpaw.bonaparte.jpa.converters.ConverterStringJsonObject.class);
    }
}
