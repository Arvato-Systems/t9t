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
package com.arvatosystems.t9t.core.be.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

@Singleton
public class EnumResolver implements IEnumResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnumResolver.class);

    @Override
    public Object getTokenBySetPqonAndInstance(String enumsetPqon, String instanceName) {
        EnumSetDefinition def = InitContainers.getEnumsetByPQON(enumsetPqon);
        if (def == null) {
            LOGGER.error("Not an enumset: PQON {}", enumsetPqon);
            throw new T9tException(T9tException.NOT_AN_ENUMSET, enumsetPqon);
        }
        return getTokenByDefAndInstance(def.getBaseEnum(), instanceName, enumsetPqon);
    }

    @Override
    public Object getTokenByPqonAndInstance(String enumPqon, String instanceName) {
        EnumDefinition def = InitContainers.getEnumByPQON(enumPqon);
        if (def == null) {
            LOGGER.error("Not an enum: PQON {}", enumPqon);
            throw new T9tException(T9tException.NOT_AN_ENUM, enumPqon);
        }
        return getTokenByDefAndInstance(def, instanceName, enumPqon);
    }

    protected Object getTokenByDefAndInstance(EnumDefinition def, String instanceName, String pqon) {
        int i = getOrdinalForInstance(def.getIds(), instanceName);
        if (i < 0) {
            LOGGER.error("Enum(set) of PQON {} does not have instance of name {}", pqon, instanceName);
            throw new T9tException(T9tException.NOT_ENUM_INSTANCE, pqon + ":" + instanceName);
        }
        if (def.getTokens() == null) {
            // numeric search
            return Integer.valueOf(i);
        } else {
            // alphanumeric enum
            String token = def.getTokens().get(i);
            if (Strings.isNullOrEmpty(token)) {
                // use nullfilter
                return null;
            } else {
                return token;
            }
        }
    }

    private static int getOrdinalForInstance(List<String> ids, String instanceName) {
        int i = 0;
        for (String inst : ids) {
            if (inst.equals(instanceName))
                return i;
            ++i;
        }
        return -1;  // not found
    }

    @Override
    public String getTokenByXEnumSetPqonAndInstance(String xenumsetPqon, String instanceName) {
        XEnumSetDefinition xSetdef = InitContainers.getXEnumsetByPQON(xenumsetPqon);
        if (xSetdef == null) {
            LOGGER.error("Not an xenumset: PQON {}", xenumsetPqon);
            throw new T9tException(T9tException.NOT_AN_XENUMSET, xenumsetPqon);
        }
        XEnumDefinition baseXEnum = xSetdef.getBaseXEnum();
        return getTokenByXEnumPqonAndInstance(baseXEnum.getName(), instanceName);
    }

    @Override
    public String getTokenByXEnumPqonAndInstance(String xenumPqon, String instanceName) {
        XEnumFactory<?> factory = XEnumFactory.getFactoryByPQON(xenumPqon);
        if (factory == null) {
            LOGGER.error("Not an xenum: PQON {}", xenumPqon);
            throw new T9tException(T9tException.NOT_AN_XENUM, xenumPqon);
        }
        return factory.getByName(instanceName).getToken();
    }
}
