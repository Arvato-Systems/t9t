/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.stream.Collectors.toMap;

import java.util.Map;

import org.camunda.bpm.model.bpmn.instance.BaseElement;
import org.camunda.bpm.model.bpmn.instance.ExtensionElements;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperty;

public abstract class BPMNExtensionHelper {

    public static Map<String, String> getAllProperties(BaseElement baseElement) {
        final ExtensionElements extensionElements = baseElement.getExtensionElements();

        if (extensionElements != null) {
            return unmodifiableMap(extensionElements.getElementsQuery()
                                                    .filterByType(CamundaProperties.class)
                                                    .list()
                                                    .stream()
                                                    .flatMap(properties -> properties.getCamundaProperties()
                                                                                     .stream())
                                                    .collect(toMap(CamundaProperty::getCamundaName,
                                                                   CamundaProperty::getCamundaValue)));
        } else {
            return emptyMap();
        }
    }

    public static String getProperty(BaseElement baseElement, String name, String defaultValue) {
        final ExtensionElements extensionElements = baseElement.getExtensionElements();

        if (extensionElements == null) {
            return defaultValue;
        }

        return extensionElements.getElementsQuery()
                                .filterByType(CamundaProperties.class)
                                .list()
                                .stream()
                                .flatMap(properties -> properties.getCamundaProperties()
                                                                 .stream())
                                .filter(property -> name.equals(property.getCamundaName()))
                                .map(CamundaProperty::getCamundaValue)
                                .findFirst()
                                .orElse(defaultValue);
    }

}
