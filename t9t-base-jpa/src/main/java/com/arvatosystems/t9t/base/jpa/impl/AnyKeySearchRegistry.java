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
package com.arvatosystems.t9t.base.jpa.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;
import com.arvatosystems.t9t.base.services.IAnyKeySearchRegistry;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Singleton;

@Singleton
public class AnyKeySearchRegistry implements IAnyKeySearchRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnyKeySearchRegistry.class);

    private static final Map<Integer, String> CLASSNAME_BY_RTTI = new ConcurrentHashMap<>(50);
    private static final Map<Integer, BiFunction<RequestContext, Long, List<Description>>> RESOLVER_BY_RTTI = new ConcurrentHashMap<>(50);

    @Override
    public void registerLeanSearchRequest(final BiFunction<RequestContext, Long, List<Description>> resolver, final int rtti, final String classname) {
        if (rtti <= 0) {
            LOGGER.error("Cannot register resolver for rtti <= 0: {}", classname);
            return;
        }
        final String theClassname = classname.endsWith("Entity") ? classname.substring(0, classname.length() - 6) : classname;
        final Integer rttiObject = Integer.valueOf(rtti);  // ensure a single instance is used for both maps, do not autobox twice
        final String previousClassname = CLASSNAME_BY_RTTI.put(rttiObject, theClassname);
        if (previousClassname != null) {
            LOGGER.error("RTTI used twice: {} for {} and {}", rttiObject, theClassname, previousClassname);
        }
        RESOLVER_BY_RTTI.put(rttiObject, resolver);
        LOGGER.debug("Registered resolver for {} by RTTI {}", theClassname, rttiObject);
    }

    @Override
    public ResolveAnyRefResponse performLookup(final RequestContext ctx, final Long ref) {
        final Integer rtti = (int)(ref % 10000L);
        final BiFunction<RequestContext, Long, List<Description>> resolver = RESOLVER_BY_RTTI.get(rtti);
        final ResolveAnyRefResponse resp = new ResolveAnyRefResponse();
        resp.setEntityClass(CLASSNAME_BY_RTTI.get(rtti));
        if (resolver != null) {
            // it is possible to find a description
            final List<Description> descs = resolver.apply(ctx, ref);
            if (!descs.isEmpty()) {
                for (final Description desc : descs) {
                    if (desc.getObjectRef().equals(ref)) {
                        resp.setDescription(desc);
                        break;
                    }
                }
            }
        }
        return resp;
    }
}
