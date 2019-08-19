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
package com.arvatosystems.t9t.base.jpa.impl;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import javax.persistence.criteria.From;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.jpa.api.JpaPathResolver;

public class PathResolver implements JpaPathResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathResolver.class);
    private static final String COMPOUND_ATTRIBUTE_SPLIT_SEPARATOR = "\\.";

    private final Class<?> entityClass;
    private final From<?, ?> root;

    public PathResolver(Class<?> entityClass, From<?, ?> root) {
        this.entityClass = entityClass;
        this.root = root;
    }

    // looks for the field of name fieldname in the class, or returns null if it cannot be found (which is not good)
    private Field searchField(Class<?> current, String fieldname) {
        for (;;) {
            // getField() returns only public fields, therefore we have to use getDeclaredField and recurse the class inheritance tree

            try {
                return current.getDeclaredField(fieldname);
            } catch (SecurityException e) {
                LOGGER.error("Denied to get field {} of class {}", fieldname, current.getCanonicalName());
                throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "Denied access to field of name " + fieldname + " in class "
                        + current.getCanonicalName());
            } catch (NoSuchFieldException e2) {
                // try the superclass, unless it's "Object"
                Class<?> parent = current.getSuperclass();
                // if parent is null, we did not find the field. Not good. Most likely an error in the query syntax.
                if ((parent == null) || (parent == Object.class)) {
                    throw new T9tException(T9tException.INVALID_FILTER_PARAMETERS, "No field of name " + fieldname);
                }
                current = parent; // next iteration
            }
        }
    }

    @Override
    public Path<?> getPath(String fieldName) {
        Class<?> currentClass = entityClass;

        if (fieldName.indexOf('.') >= 0) {
            Path<?> compoundAttributePath = root;
            String[] components = fieldName.split(COMPOUND_ATTRIBUTE_SPLIT_SEPARATOR);
            for (int i = 0; i < components.length; ++i) {
                if (i > 0) {
                    // currently, we can only join at the root level
                    compoundAttributePath = compoundAttributePath.get(components[i]);
                } else {
                    final Field fld = searchField(currentClass, components[i]);
                    Class<?> thisType = fld.getType();
                    if (Collection.class.isAssignableFrom(thisType) || Map.class.isAssignableFrom(thisType)) {
                        // use of join is required
                        LOGGER.debug("generic search uses join for path component {} of {} in {}", components[i], currentClass.getSimpleName(),
                                entityClass.getSimpleName());
                        compoundAttributePath = root.join(components[i], JoinType.LEFT);
                        // the next path component is the first or second type parameter
                        //Type fldArgs = fld.getGenericType();
                    } else {
                        compoundAttributePath = compoundAttributePath.get(components[i]);
                        currentClass = thisType;
                    }
                }
            }
            return compoundAttributePath;
        } else {
            return root.get(fieldName);
        }
    }
}
