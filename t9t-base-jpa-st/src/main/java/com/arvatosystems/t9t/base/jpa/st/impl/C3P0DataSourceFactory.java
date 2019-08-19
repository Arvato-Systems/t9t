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

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.st.IDataSourceFactory;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class C3P0DataSourceFactory implements IDataSourceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(C3P0DataSourceFactory.class);

    @Override
    public DataSource createDataSource() throws Exception {
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final RelationalDatabaseConfiguration dbCfg = cfg.getDatabaseConfiguration();

        final ComboPooledDataSource dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(dbCfg.getJdbcDriverClass());
        dataSource.setJdbcUrl(dbCfg.getJdbcConnectString());
        dataSource.setUser(dbCfg.getUsername());
        dataSource.setPassword(dbCfg.getPassword());

        final Properties properties = dataSource.getProperties();
        properties.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.slf4j.Slf4jMLog");
        addCustomProperties(dbCfg, properties);

        return dataSource;
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

                if (key.startsWith("hibernate.c3p0.")) {
                    key = key.substring("hibernate.".length());
                }

                if (key.startsWith("c3p0.")) {
                    LOGGER.info("Setting custom value for persistence.xml: key = {}, value = {}", key, value);
                    targetProperties.put(key, value);
                }
            } else {
                LOGGER.warn("Custom (z field) entry {} has not '=' delimiter, ignoring entry", customSetting);
            }
        }
    }

}
