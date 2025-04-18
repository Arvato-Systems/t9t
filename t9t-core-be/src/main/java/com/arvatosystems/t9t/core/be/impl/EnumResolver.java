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
package com.arvatosystems.t9t.core.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEnumResolver;
import com.arvatosystems.t9t.init.InitContainers;
import com.google.common.base.Strings;
import de.jpaw.bonaparte.pojos.meta.EnumDefinition;
import de.jpaw.bonaparte.pojos.meta.EnumSetDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumDefinition;
import de.jpaw.bonaparte.pojos.meta.XEnumSetDefinition;
import de.jpaw.dp.Singleton;
import de.jpaw.enums.XEnumFactory;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class EnumResolver implements IEnumResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumResolver.class);

    @Override
    public Object getTokenBySetPqonAndInstance(final String enumsetPqon, final String instanceName) {
        final EnumSetDefinition def = InitContainers.getEnumsetByPQON(enumsetPqon);
        if (def == null) {
            LOGGER.error("Not an enumset: PQON {}", enumsetPqon);
            throw new T9tException(T9tException.NOT_AN_ENUMSET, enumsetPqon);
        }
        return getTokenByDefAndInstance(def.getBaseEnum(), instanceName, enumsetPqon);
    }

    @Override
    public Object getTokenByPqonAndInstance(final String enumPqon, final String instanceName) {
        final EnumDefinition def = InitContainers.getEnumByPQON(enumPqon);
        if (def == null) {
            LOGGER.error("Not an enum: PQON {}", enumPqon);
            throw new T9tException(T9tException.NOT_AN_ENUM, enumPqon);
        }
        return getTokenByDefAndInstance(def, instanceName, enumPqon);
    }

    protected Object getTokenByDefAndInstance(final EnumDefinition def, final String instanceName, final String pqon) {
        final int i = getOrdinalForInstance(def.getIds(), instanceName);
        if (i < 0) {
            LOGGER.error("Enum(set) of PQON {} does not have instance of name {}", pqon, instanceName);
            throw new T9tException(T9tException.NOT_ENUM_INSTANCE, pqon + ":" + instanceName);
        }
        return getTokenByDefAndOrdinal(def, i);
    }

    protected Object getTokenByDefAndOrdinal(final EnumDefinition def, final int ordinal) {
        if (def.getTokens() == null) {
            // numeric search
            return Integer.valueOf(ordinal);
        } else {
            // alphanumeric enum
            final String token = def.getTokens().get(ordinal);
            if (Strings.isNullOrEmpty(token)) {
                // use nullfilter
                return null;
            } else {
                return token;
            }
        }
    }

    private static int getOrdinalForInstance(final List<String> ids, final String instanceName) {
        int i = 0;
        for (final String inst : ids) {
            if (inst.equals(instanceName)) {
                return i;
            }
            ++i;
        }
        return -1;  // not found
    }

    @Override
    public String getTokenByXEnumSetPqonAndInstance(final String xenumsetPqon, final String instanceName) {
        final XEnumSetDefinition xSetdef = InitContainers.getXEnumsetByPQON(xenumsetPqon);
        if (xSetdef == null) {
            LOGGER.error("Not an xenumset: PQON {}", xenumsetPqon);
            throw new T9tException(T9tException.NOT_AN_XENUMSET, xenumsetPqon);
        }
        final XEnumDefinition baseXEnum = xSetdef.getBaseXEnum();
        return getTokenByXEnumPqonAndInstance(baseXEnum.getName(), instanceName);
    }

    @Override
    public String getTokenByXEnumPqonAndInstance(final String xenumPqon, final String instanceName) {
        final XEnumFactory<?> factory = XEnumFactory.getFactoryByPQON(xenumPqon);
        if (factory == null) {
            LOGGER.error("Not an xenum: PQON {}", xenumPqon);
            throw new T9tException(T9tException.NOT_AN_XENUM, xenumPqon);
        }
        return factory.getByName(instanceName).getToken();
    }

    @Override
    public Object getTokenByPqonAndOrdinal(String enumPqon, Integer ordinal) {
        final EnumDefinition def = InitContainers.getEnumByPQON(enumPqon);
        if (def == null) {
            LOGGER.error("Not an enum: PQON {}", enumPqon);
            throw new T9tException(T9tException.NOT_AN_ENUM, enumPqon);
        }

        if (def.getIds().size() < ordinal) {
            LOGGER.error("Enum of PQON {} has no ordinal {}", enumPqon, ordinal);
            throw new T9tException(T9tException.INVALID_ENUM_VALUE, enumPqon + ":" + ordinal);
        }

        return getTokenByDefAndOrdinal(def, ordinal);
    }

    @Override
    public List<Object> getTokensByPqonAndInstances(String enumPqon, List<String> instanceNames) {
        final EnumDefinition def = InitContainers.getEnumByPQON(enumPqon);
        if (def == null) {
            LOGGER.error("Not an enum: PQON {}", enumPqon);
            throw new T9tException(T9tException.NOT_AN_ENUM, enumPqon);
        }

        List<Object> tokens = new ArrayList<>(instanceNames.size());
        for (String instanceName : instanceNames) {
            tokens.add(getTokenByDefAndInstance(def, instanceName, enumPqon));
        }

        return tokens;
    }

    @Override
    public List<Object> getTokensByPqonAndOrdinals(String enumPqon, List<Integer> ordinals) {
        final EnumDefinition def = InitContainers.getEnumByPQON(enumPqon);
        if (def == null) {
            LOGGER.error("Not an enum: PQON {}", enumPqon);
            throw new T9tException(T9tException.NOT_AN_ENUM, enumPqon);
        }

        List<Object> tokens = new ArrayList<>(ordinals.size());
        for (Integer ordinal : ordinals) {
            if (def.getIds().size() < ordinal) {
                LOGGER.error("Enum of PQON {} has no ordinal {}", enumPqon, ordinal);
                throw new T9tException(T9tException.INVALID_ENUM_VALUE, enumPqon + ":" + ordinal);
            }

            if (def.getIds().size() < ordinal) {
                LOGGER.error("Enum of PQON {} has no ordinal {}", enumPqon, ordinal);
                throw new T9tException(T9tException.INVALID_ENUM_VALUE, enumPqon + ":" + ordinal);
            }
            tokens.add(getTokenByDefAndOrdinal(def, ordinal));
        }

        return tokens;
    }
}
