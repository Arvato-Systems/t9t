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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaJdbcConnectionProvider;
import com.arvatosystems.t9t.base.jpa.st.IDataSourceFactory;
import com.arvatosystems.t9t.base.jpa.st.IEMFBeanCustomizer;
import com.arvatosystems.t9t.base.jpa.st.util.DiagnoseDataSourceProxy;
import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;

@Startup(12000)
public class InitJpa {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitJpa.class);

    public static void onStartup() throws Exception {
        LOGGER.info("Using JPA support with global transaction management by Spring transaction");

        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        final RelationalDatabaseConfiguration dbCfg = cfg.getDatabaseConfiguration();

        // Create initial DataSource (or get from pooling like c3p0 or commons-pool)
        final DataSource dataSource = DiagnoseDataSourceProxy.createProxy(Jdp.getRequired(IDataSourceFactory.class)
                                                                             .createDataSource());

        // Wrap as transaction aware to be included in global transaction management
        // (This is the data source to be normally used!)
        final TransactionAwareDataSourceProxy transactionAwareDataSource = new TransactionAwareDataSourceProxy(dataSource);
        transactionAwareDataSource.afterPropertiesSet();
        Jdp.bindInstanceTo(transactionAwareDataSource, DataSource.class);

        // Create PersistenceManager
        // Use unwrapped DS, since transaction management is done on JPA-Level !!
        final LocalContainerEntityManagerFactoryBean emfBean = Jdp.getRequired(IEMFBeanCustomizer.class)
                                                                  .createEMFBean(dataSource, cfg.getPersistenceUnitName(), dbCfg);
        emfBean.afterPropertiesSet();
        final EntityManagerFactory emf = emfBean.getObject();
        Jdp.bindInstanceTo(emf, EntityManagerFactory.class);

        // Create global transaction manager
        final JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
        jpaTransactionManager.setEntityManagerFactory(emf);
        jpaTransactionManager.setDataSource(emfBean.getDataSource());
        jpaTransactionManager.afterPropertiesSet();
        Jdp.bindInstanceTo(jpaTransactionManager, PlatformTransactionManager.class);

        final EntityManager sharedEntityManager = SharedEntityManagerCreator.createSharedEntityManager(jpaTransactionManager.getEntityManagerFactory());

        Jdp.registerWithCustomProvider(PersistenceProviderJPA.class, new PersistenceProviderJPASTProvider(jpaTransactionManager, sharedEntityManager));

        // Add JDBC provider with tx managment
        Jdp.bindInstanceTo(new JDBCConnectionProvider(transactionAwareDataSource), IJdbcConnectionProvider.class);
        Jdp.bindInstanceTo(new JDBCConnectionProvider(transactionAwareDataSource), IJpaJdbcConnectionProvider.class);

        // Add JDBC provider for independend connections
        // This is just using the raw data source without tx management
        // NOTE: There seems to be a bug in JDP, which will just kick the binding blow if done before the unnamed
        // binding of the same type above.
        Jdp.bindInstanceTo(new JDBCConnectionProvider(dataSource), IJdbcConnectionProvider.class, "independent");
    }

}
