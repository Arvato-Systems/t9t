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
package com.arvatosystems.t9t.base.jpa.st.impl;

import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import com.arvatosystems.t9t.base.jpa.st.IEMFBeanCustomizer;
import com.arvatosystems.t9t.cfg.be.DatabaseBrandType;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;

import de.jpaw.dp.Singleton;

@Singleton
public class EMFBeanCustomizer implements IEMFBeanCustomizer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EMFBeanCustomizer.class);
    private static final String DIALECT_KEY = "hibernate.dialect";

    @Override
    public LocalContainerEntityManagerFactoryBean createEMFBean(DataSource dataSource, String persistenceUnitName, RelationalDatabaseConfiguration settings) {
        final LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter()); // Use Hibernate (might be configurable?)
        emfBean.setPersistenceUnitName(persistenceUnitName);
        emfBean.setSharedCacheMode(SharedCacheMode.ENABLE_SELECTIVE);

        final Properties jpaProperties = new Properties();
        // see http://docs.jboss.org/hibernate/orm/4.3/javadocs/org/hibernate/dialect/package-summary.html
        DatabaseBrandType dbName = settings.getDatabaseBrand();
        if (dbName != null) {
            switch (dbName) {
            case HANA:
                jpaProperties.put(DIALECT_KEY, "org.hibernate.dialect.HANARowStoreDialect");
                break;
            case MS_SQL_SERVER:
                jpaProperties.put(DIALECT_KEY, "org.hibernate.dialect.SQLServer2012Dialect");
                break;
            case ORACLE:
                jpaProperties.put(DIALECT_KEY, "org.hibernate.dialect.Oracle12cDialect");
                break;
            case POSTGRES:
                jpaProperties.put(DIALECT_KEY, "org.hibernate.dialect.PostgreSQL94Dialect");
                break;
            case H2:
                jpaProperties.put(DIALECT_KEY, "org.hibernate.dialect.H2Dialect");
                break;
            default:
                break;
            }
        }

        jpaProperties.put("hibernate.connection.release_mode", "after_transaction"); // required for connection pooling?
        jpaProperties.put("hibernate.connection.autocommit", "false");

        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.generate_statistics", "true");
        jpaProperties.put("hibernate.use_sql_comments", "true");

        // The next one speeds up connection time with remote DBs a lot, but is not without risk. see
        // http://stackoverflow.com/questions/10075081/hibernate-slow-to-acquire-postgres-connection
        // http://stackoverflow.com/questions/14417692/initializing-c3p0-connection-pool-takes-2-min
        jpaProperties.put("hibernate.temp.use_jdbc_metadata_defaults", "false"); // requires the dialect to be set explicitly!

        // caching, see http://stackoverflow.com/questions/3663979/how-to-use-jpa2s-cacheable-instead-of-hibernates-cache for details
        jpaProperties.put("hibernate.cache.region.factory_class", "org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory");
        jpaProperties.put("hibernate.cache.provider_class", "org.hibernate.cache.SingletonEhCacheProvider");
        jpaProperties.put("hibernate.cache.use_second_level_cache", "true");
        jpaProperties.put("hibernate.cache.use_query_cache", "false");

        // Ordering of inserts / updates. Provides some tuning (less roundtrips to the DB).
        // See https://vladmihalcea.com/2015/03/18/how-to-batch-insert-and-update-statements-with-hibernate/
        // Note: This does not cause any rewriting of separate inserts into batch inserts, you need a DB specific option for that.
        // For example reWriteBatchedInserts for postgres. See http://www.postgresql-archive.org/Batches-of-single-insert-statements-vs-batches-of-multi-insert-statements-td5906499.html
        jpaProperties.put("hibernate.order_inserts", "true");
        jpaProperties.put("hibernate.order_updates", "true");
        //jpaProperties.put("hibernate.jdbc.batch_versioned_data", "true");
        //jpaProperties.put("hibernate.jdbc.batch_size", "25");

        // needed for calling Oracle stored procedures, see https://stackoverflow.com/questions/22045641/is-it-possible-to-pass-a-null-parameter-to-a-stored-procedure-in-java-jpa-2-1
        jpaProperties.put("hibernate.proc.param_null_passing", "true");

        addCustomProperties(settings, jpaProperties);
        emfBean.setJpaProperties(jpaProperties);

        emfBean.setPackagesToScan("de.jpaw.bonaparte.jpa.converters", "de.jpaw.bonaparte.jpa.postgres", "com.arvatosystems");

        return emfBean;
    }

    protected void addCustomProperties(RelationalDatabaseConfiguration dbCfg, Properties targetProperties) {
        if (dbCfg.getZ() == null) {
            return;
        }

        for (String customSetting : dbCfg.getZ()) {
            int equalsPos = customSetting.indexOf('=');
            if (equalsPos > 0) {
                // store key/value pair
                String key = customSetting.substring(0, equalsPos)
                                          .trim();
                String value = customSetting.substring(equalsPos + 1)
                                            .trim();
                LOGGER.info("Setting custom value for persistence.xml: key = {}, value = {}", key, value);
                targetProperties.put(key, value);
            } else {
                LOGGER.warn("Custom (z field) entry {} has not '=' delimiter, ignoring entry", customSetting);
            }
        }
    }


}
