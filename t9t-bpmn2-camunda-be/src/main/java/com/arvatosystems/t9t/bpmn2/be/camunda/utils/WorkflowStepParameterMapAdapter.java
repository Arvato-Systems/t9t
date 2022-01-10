/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter for Camunda VariableScope to be available as Map<String, Object> as used by T9T workflow steps. In contrast
 * to regular VariableScope::getVariables() write operations to this map are passed to the variable scope. keySet(),
 * entrySet() and values() will return independent and immutable collections.
 *
 * @author TWEL006
 */
public class WorkflowStepParameterMapAdapter implements Map<String, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowStepParameterMapAdapter.class);

    private final VariableScope variables;

    public WorkflowStepParameterMapAdapter(VariableScope variables) {
        this.variables = variables;
    }

    @Override
    public int size() {
        LOGGER.warn("Requesting size() of workflow parameter map will load all available parameters, which is a performance impact! Consider not using this call.");
        return variables.getVariables().size();
    }

    @Override
    public boolean isEmpty() {
        return !variables.hasVariables();
    }

    @Override
    public boolean containsKey(Object key) {
        return variables.hasVariable((String) key);
    }

    @Override
    public boolean containsValue(Object value) {
        LOGGER.warn("Requesting containsValue() of workflow parameter map will load all available parameters, which is a performance impact! Consider not using this call.");
        return variables.getVariables().containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return variables.getVariable((String) key);
    }

    @Override
    public Object put(String key, Object value) {
        // This might be removed due to performance impact (might trigger a variable load), if not used.
        // But this would break the interface contract of java.util.Map.
        final Object oldValue = variables.getVariable(key);

        variables.setVariableLocal(key, value);

        return oldValue;
    }

    @Override
    public Object remove(Object key) {
        // This might be removed due to performance impact (might trigger a variable load), if not used.
        // But this would break the interface contract of java.util.Map.
        final Object oldValue = variables.getVariable((String) key);

        variables.removeVariableLocal((String) key);

        return oldValue;
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        variables.setVariablesLocal((Map<String, Object>) m);
    }

    @Override
    public void clear() {
        variables.removeVariablesLocal();
    }

    @Override
    public Set<String> keySet() {
        LOGGER.warn("Requesting keySet() of workflow parameter map will load all available parameters, which is a performance impact! Consider not using this call.");
        return unmodifiableSet(variables.getVariables().keySet());
    }

    @Override
    public Collection<Object> values() {
        LOGGER.warn("Requesting values() of workflow parameter map will load all available parameters, which is a performance impact! Consider not using this call.");
        return unmodifiableCollection(variables.getVariables().values());
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        LOGGER.warn("Requesting entrySet() of workflow parameter map will load all available parameters, which is a performance impact! Consider not using this call.");
        return unmodifiableSet(variables.getVariables().entrySet());
    }
}
