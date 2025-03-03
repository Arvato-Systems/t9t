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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.request.GetConfigPQONsRequest;
import com.arvatosystems.t9t.base.request.GetConfigPQONsResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.Table;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class GetConfigPQONsRequestHandler extends AbstractReadOnlyRequestHandler<GetConfigPQONsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetConfigPQONsRequestHandler.class);
    protected static final String PACKAGE_PREFIX = "com.arvatosystems.t9t";
    protected static final String CFG_ENTITY_IDENTIFIER = "_cfg_";
    protected static final String FIELD_TABLE_NAME = "TABLE_NAME";
    protected static final String METHOD_DATA_PQON = "class$DataPQON";

    @Nonnull
    @Override
    public GetConfigPQONsResponse execute(@Nonnull final RequestContext ctx, @Nonnull final GetConfigPQONsRequest request) throws Exception {

        final Set<String> pqons = new HashSet<>(64);
        Reflections reflections = new Reflections(PACKAGE_PREFIX);
        Set<Class<?>> entityClasses = reflections.getTypesAnnotatedWith(Table.class);
        for (Class<?> entityClass : entityClasses) {
            if (isCfgEntity(entityClass)) {
                final String pqon = getPQON(entityClass);
                if (pqon != null) {
                    pqons.add(pqon);
                }
            }
        }
        LOGGER.debug("Found {} entity classes and {} config PQONs", entityClasses.size(), pqons.size());
        final GetConfigPQONsResponse response = new GetConfigPQONsResponse();
        response.setPqons(pqons);
        return response;
    }

    // Check if the entity class is a configuration entity
    protected boolean isCfgEntity(@Nonnull final Class<?> entityClass) throws IllegalAccessException {
        final Field[] fields = entityClass.getFields();
        for (Field field : fields) {
            if (field.getName().equalsIgnoreCase(FIELD_TABLE_NAME)) {
                final String tableName = (String) field.get(null);
                if (tableName != null && tableName.contains(CFG_ENTITY_IDENTIFIER)) {
                    return true;
                }
            }
        }
        return false;
    }

    // Get the PQON of the entity class
    @Nullable
    protected String getPQON(@Nonnull final Class<?> entityClass) throws InvocationTargetException, IllegalAccessException {
        final Method[] methods = entityClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals(METHOD_DATA_PQON)) {
                return (String) method.invoke(null);
            }
        }
        return null;
    }
}
