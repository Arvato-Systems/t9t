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
package com.arvatosystems.t9t.bpmn.be.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepCache;
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepPlugin;
import com.arvatosystems.t9t.plugins.services.IPluginManager;
import com.arvatosystems.t9t.plugins.services.IPluginMethodLifecycle;
import com.arvatosystems.t9t.plugins.services.Plugin;
import com.arvatosystems.t9t.plugins.services.PluginMethod;

import de.jpaw.dp.Jdp;

/**
 * For workflow steps, an additional plugin manager is required, which registers newly loaded plugins.
 *
 * @param <T> the data type
 * @param <S> the minimum type the new step must conform to
 * @param <M> the type of the generated proxy
 */
public abstract class AbstractWorkflowStepPluginManager<T, S extends IWorkflowStep<T>, M extends S> implements IPluginMethodLifecycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWorkflowStepPluginManager.class);

    protected final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);
    protected final IWorkflowStepCache stepCache = Jdp.getRequired(IWorkflowStepCache.class);

    /** Returns the minimum common denominator for an existing implementation. */
    abstract protected Class<S> getCompatibleSuperclass();  // FIXME: should be Class<S>, but does not compile due to generics

    /** Returns the class which is dynamically generated as a proxy. */
    abstract protected Class<M> getPluginWrapperClass(IWorkflowStep<T> pluginInstance);  // FIXME: should be Class<M>, but does not compile due to generics

    /** Create a wrapper / dispatcher / proxy for the step. */
    abstract protected M createWrapper(String qualifier, S existingInstance);

    @Override
    public void registerPluginMethod(final Long tenantRef, final Plugin loadedPlugin, final PluginMethod method, boolean before) {
        if (before) {
            return;
        }
        registerInstance((IWorkflowStepPlugin<T>)method);
    }

    private void registerInstance(IWorkflowStepPlugin<T> step) {
        final String qualifier = step.getQualifier();
        final S oldInstance = (S)Jdp.getOptional(IWorkflowStep.class, qualifier);
        final M newInstance;
        if (oldInstance == null) {
            // no old exists: create a new
            newInstance = createWrapper(qualifier, null);
            stepCache.addToCache(newInstance, qualifier);
        } else if (oldInstance.getClass().equals(getPluginWrapperClass(step))) {
            // exists already, only register in Manager
            newInstance = (M)oldInstance;
        } else if (!getCompatibleSuperclass().isAssignableFrom(oldInstance.getClass())) {
           throw new T9tException(T9tBPMException.BPM_PLUGIN_INCOMPATIBLE, getCompatibleSuperclass().getCanonicalName() + " <> " + oldInstance.getClass().getCanonicalName());
        } else {
            // it is a valid fallback, unless there is a clash in factories
            if (oldInstance.getFactoryName() != null && step.getFactoryName() != null && !oldInstance.getFactoryName().equals(step.getFactoryName())) {
                LOGGER.error("Factory of hardcoded implementation {} is {}, factory of plugin {} is {}",
                    oldInstance.getClass().getCanonicalName(), oldInstance.getFactoryName(),
                    step.getClass().getCanonicalName(), step.getFactoryName());
                throw new T9tException(T9tBPMException.BPM_PLUGIN_FACTORY_INCOMPATIBLE, oldInstance.getFactoryName() + " <> " + step.getFactoryName());
            }
            newInstance = createWrapper(qualifier, oldInstance);
            stepCache.addToCache(newInstance, qualifier);
        }
    }
}
