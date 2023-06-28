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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.IBPMObjectFactory;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepCache;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class WorkflowStepCache implements IWorkflowStepCache {
    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStepCache.class);
    private final Map<String, IWorkflowStep> workflowSteps = new ConcurrentHashMap<String, IWorkflowStep>(200);

    @IsLogicallyFinal
    private Map<String, IBPMObjectFactory> bpmObjectFactories = null;

    @Override
    public void loadCaches() {
        try {
            workflowSteps.putAll(Jdp.getInstanceMapPerQualifier(IWorkflowStep.class));  // allow extending this one later
            bpmObjectFactories = Jdp.getInstanceMapPerQualifier(IBPMObjectFactory.class);
        } catch (final Exception e) {
            LOGGER.error("Initializer exception due to ", e);
        }

        LOGGER.info("Found {} BPM object factories and {} BPM workflow step implementations", bpmObjectFactories.size(), workflowSteps.size());
        for (final Map.Entry<String, IBPMObjectFactory> of : bpmObjectFactories.entrySet()) {
            LOGGER.debug("BPM object factory {} is implemented by {}", of.getKey(), of.getValue().getClass().getCanonicalName());
        }
        for (final Map.Entry<String, IWorkflowStep> ws : workflowSteps.entrySet()) {
            final String factory = ws.getValue().getFactoryName();
            final boolean factoryExists = factory == null || bpmObjectFactories.get(factory) != null;
            if (LOGGER.isDebugEnabled()) {
                final String factoryText = factory != null
                  ? " (using factory " + factory + ")"
                  : "";
                LOGGER.debug("BPM workflow step {} is implemented by {}{}", ws.getKey(), ws.getValue().getClass().getCanonicalName(), factoryText
                );
            }
            if (!factoryExists) {
                LOGGER.error("*** Factory {} referenced by {} does not exist! ***", factory, ws.getValue().getClass().getCanonicalName());
            }
        }
    }

    @Override
    public IBPMObjectFactory<?> getBPMObjectFactoryForName(final String name) {
        final IBPMObjectFactory factory = this.bpmObjectFactories.get(name);
        if (factory == null) {
            throw new T9tException(T9tBPMException.BPM_OBJECT_FACTORY_NOT_FOUND, name);
        }
        return factory;
    }

    @Override
    public IWorkflowStep<?> getWorkflowStepForName(final String name) {
        final IWorkflowStep step = this.workflowSteps.get(name);
        if ((step == null)) {
            throw new T9tException(T9tBPMException.BPM_STEP_NOT_FOUND, name);
        }
        return step;
    }

    @Override
    public void addToCache(final IWorkflowStep<?> step, final String name) {
        workflowSteps.put(name, step);
        Jdp.bindInstanceTo(step, IWorkflowStep.class, name);
    }
}
