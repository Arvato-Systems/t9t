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
package com.arvatosystems.t9t.unittest;

import de.jpaw.dp.Jdp;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.internal.configuration.InjectingAnnotationEngine;

import java.lang.reflect.Field;

/**
 * Annotation injection bridge to combine JDP with Mockito
 */
public class JdpMockitoInjectingAnnotationEngine extends InjectingAnnotationEngine {

    @Override
    public void injectMocks(Object testClassInstance) {
        initJdp(testClassInstance);
        super.injectMocks(testClassInstance);
    }

    private void initJdp(Object testClassInstance) {
        Jdp.reset();

        scanAndBind(testClassInstance, testClassInstance.getClass());
    }

    @SuppressWarnings("unchecked")
    private void scanAndBind(Object testClassInstance, Class<?> clazz) {
        if (clazz.getSuperclass() != null) {
            scanAndBind(testClassInstance, clazz.getSuperclass());
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Mock.class)
                || field.isAnnotationPresent(Spy.class)) {

                try {
                    field.setAccessible(true);
                    Jdp.bindInstanceTo(field.get(testClassInstance), (Class<Object>)field.getType());
                } catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
                    throw new RuntimeException("Error during getting field " + field + " to bind in JDP", e);
                }
            }
        }
    }

}
