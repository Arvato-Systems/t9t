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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.bpmn2.be.camunda.startup.IProcessEngineConfigurationFactory;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupShutdown;

@Startup(30300)
public class InitBpmn implements StartupShutdown {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitBpmn.class);

    @Override
    public void onStartup() {
        final IProcessEngineConfigurationFactory engineConfigurationFactory = Jdp.getRequired(IProcessEngineConfigurationFactory.class);
        LOGGER.info("Using {} to configure Camunda BPMN engine", engineConfigurationFactory.getClass()
                                                                                           .getName());

        final ProcessEngineConfiguration engineConfiguration = engineConfigurationFactory.createConfiguration();
        final ProcessEngine engine = engineConfiguration.buildProcessEngine();
        Jdp.bindInstanceTo(engine, ProcessEngine.class);

        Jdp.bindInstanceTo(engine.getRepositoryService(), RepositoryService.class);
        Jdp.bindInstanceTo(engine.getRuntimeService(), RuntimeService.class);
        Jdp.bindInstanceTo(engine.getHistoryService(), HistoryService.class);
        Jdp.bindInstanceTo(engine.getManagementService(), ManagementService.class);
        Jdp.bindInstanceTo(engine.getIdentityService(), IdentityService.class);
    }

    @Override
    public void onShutdown() {
        final ProcessEngine engine = Jdp.getRequired(ProcessEngine.class);

        engine.close();
    }

}
