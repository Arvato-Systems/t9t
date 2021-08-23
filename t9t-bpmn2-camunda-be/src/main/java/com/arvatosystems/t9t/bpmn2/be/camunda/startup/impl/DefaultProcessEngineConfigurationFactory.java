/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.camunda.startup.impl;

import static com.google.common.base.MoreObjects.firstNonNull;
import static org.camunda.bpm.engine.ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.persistence.StrongUuidGenerator;
import org.camunda.bpm.engine.spring.SpringTransactionsProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;

import com.arvatosystems.t9t.bpmn2.IBPMNBeanProvider;
import com.arvatosystems.t9t.bpmn2.be.camunda.jobExecutor.T9tJobExecutor;
import com.arvatosystems.t9t.bpmn2.be.camunda.startup.IProcessEngineConfigurationFactory;
import com.arvatosystems.t9t.cfg.be.Bpm2Configuration;
import com.arvatosystems.t9t.cfg.be.RelationalDatabaseConfiguration;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DefaultProcessEngineConfigurationFactory implements IProcessEngineConfigurationFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultProcessEngineConfigurationFactory.class);

    @Override
    public ProcessEngineConfiguration createConfiguration() {
        final T9tServerConfiguration serverConfiguration = Jdp.getRequired(T9tServerConfiguration.class);
        final DataSource dataSource = Jdp.getOptional(DataSource.class);
        final DataSource dataSource2 = Jdp.getOptional(DataSource.class, "JDBC2");
        final PlatformTransactionManager transactionManager = Jdp.getOptional(PlatformTransactionManager.class);
        final EntityManagerFactory emf = Jdp.getOptional(EntityManagerFactory.class);

        final SpringTransactionsProcessEngineConfiguration engineConfiguration = new SpringTransactionsProcessEngineConfiguration();
        engineConfiguration.setProcessEngineName("T9T");

        if (dataSource != null) {
            // This is the recommended setup
            LOGGER.debug("Using data source {}", dataSource);
            engineConfiguration.setDataSource(dataSource);
        } else if (dataSource2 != null) {
            LOGGER.debug("Using data source from secondary DB connection {}", dataSource2);
            engineConfiguration.setDataSource(dataSource2);
        } else {
            LOGGER.error("No javax.sql.DataSource found by JDP. Configure direct JDBC access for BPMN engine. Neither transactions will be synchronized nor shared JDBC pooling will be used! THIS SETUP IS NOT RECOMMENDED!");

            final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
            final RelationalDatabaseConfiguration dbCfg = cfg.getDatabaseConfiguration();

            engineConfiguration.setJdbcDriver(dbCfg.getJdbcDriverClass());
            engineConfiguration.setJdbcUrl(dbCfg.getJdbcConnectString());
            engineConfiguration.setJdbcUsername(dbCfg.getUsername());
            engineConfiguration.setJdbcPassword(dbCfg.getPassword());
        }

        if (emf != null) {
            // This is the recommended setup
            LOGGER.debug("Using JPA entity manager factory {}", emf);
            engineConfiguration.setJpaEntityManagerFactory(emf);
            engineConfiguration.setJpaHandleTransaction(true);
            engineConfiguration.setJpaCloseEntityManager(true);
        } else {
            LOGGER.error("No javax.persistence.EntityManagerFactory found by JDP.");

            final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);

            if (cfg.getPersistenceUnitName() != null) {
                LOGGER.error("Configure BPMN engine to use persistence unit name {}. Workflow engine will create its own JPA entity manager, thus JPA transactions will be independend! THIS SETUP IS NOT RECOMMENDED!",
                             cfg.getPersistenceUnitName());
                engineConfiguration.setJpaPersistenceUnitName(cfg.getPersistenceUnitName());
                engineConfiguration.setJpaHandleTransaction(true);
                engineConfiguration.setJpaCloseEntityManager(true);
            } else {
                LOGGER.error("No JPA will be available within BPMN engine! THIS SETUP IS NOT RECOMMENDED!");
            }
        }

        if (transactionManager != null) {
            // This is the recommended setup
            LOGGER.debug("Using transaction manager {}", transactionManager);
            engineConfiguration.setTransactionManager(transactionManager);
        } else {
            LOGGER.error("No {} found by JDP. Transactions will be synchronized! THIS SETUP IS NOT RECOMMENDED!", PlatformTransactionManager.class.getCanonicalName());
        }

        engineConfiguration.setDatabaseSchemaUpdate(DB_SCHEMA_UPDATE_FALSE);

        final Bpm2Configuration bpm2Configuration = serverConfiguration.getBpm2Configuration();
        final T9tJobExecutor jobExecutor = new T9tJobExecutor();
        engineConfiguration.setJobExecutor(jobExecutor);
        engineConfiguration.setJobExecutorActivate(bpm2Configuration == null || !Boolean.FALSE.equals(bpm2Configuration.getJobExecutorEnabled()));
        engineConfiguration.setCreateIncidentOnFailedJobEnabled(true);

        if (bpm2Configuration != null) {
            if (bpm2Configuration.getJobExecutorLockId() != null) {
                jobExecutor.setLockOwner(bpm2Configuration.getJobExecutorLockId());
            }

            jobExecutor.setCorePoolSize(firstNonNull(bpm2Configuration.getJobExecutorMinWorker(), 3));
            jobExecutor.setMaxPoolSize(firstNonNull(bpm2Configuration.getJobExecutorMaxWorker(), 10));
            jobExecutor.setQueueSize(firstNonNull(bpm2Configuration.getJobExecutorQueueSize(), 3));
        }

        engineConfiguration.setCustomPostBPMNParseListeners(new ArrayList<>());
        engineConfiguration.getCustomPostBPMNParseListeners().add(new T9tBPMNParseListener());

        engineConfiguration.setArtifactFactory(new JdpArtifactFactory());
        engineConfiguration.setIdGenerator(new StrongUuidGenerator());

        final List<IBPMNBeanProvider> beanProviderList = Jdp.getAll(IBPMNBeanProvider.class);
        if (beanProviderList != null) {
            final Map<Object, Object> beans = new HashMap<>();

            for (IBPMNBeanProvider beanProvider : beanProviderList) {
                beans.putAll(beanProvider.getBPMNBeans());
            }

            engineConfiguration.setBeans(beans);
        }

        engineConfiguration.setCustomPreVariableSerializers(new ArrayList<>());
        engineConfiguration.getCustomPreVariableSerializers()
                           .add(new BonaparteTypeValueSerializer());
        engineConfiguration.setDefaultSerializationFormat(BonaparteTypeValueSerializer.DATA_FORMAT);

        engineConfiguration.setCreateDiagramOnDeploy(true);

        return engineConfiguration;
    }
}
