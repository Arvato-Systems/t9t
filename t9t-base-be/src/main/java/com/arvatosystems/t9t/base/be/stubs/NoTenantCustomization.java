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
package com.arvatosystems.t9t.base.be.stubs;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.IRequestHandlerResolver;
import com.arvatosystems.t9t.base.services.ITenantCustomization;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

/** This implementation serves as a base implementation which provides some of the features required to do tenant specific customization,
 * such as the map for tenant specific request handlers.
 *
 * This implementation provides no
 */
public class NoTenantCustomization implements ITenantCustomization {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoTenantCustomization.class);

    private final IRequestHandlerResolver requestHandlerResolver = Jdp.getRequired(IRequestHandlerResolver.class);
    private final boolean usesCustomRequestHandlers;

    // index is the pqon
    protected static final Map<String, IRequestHandler<?>> DEFAULT_REQUEST_HANDLER_CACHE = new ConcurrentHashMap<>(1000);

    // index is the pqon
    protected final Map<String, IRequestHandler<?>> tenantRequestHandlerCache;

    public NoTenantCustomization() {
        this.usesCustomRequestHandlers = false;
        tenantRequestHandlerCache = usesCustomRequestHandlers ? new ConcurrentHashMap<>(1000) : DEFAULT_REQUEST_HANDLER_CACHE;
    }

    @Override
    public <ENTITY> ENTITY newEntityInstance(final int rtti, final Class<ENTITY> baseClass) {
        try {
            return baseClass.getDeclaredConstructor().newInstance();
        } catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException | InstantiationException e) {
            LOGGER.error("This should not happen: {}: {}: {}", e.getClass().getCanonicalName(), e.getMessage(), ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.METHOD_INSTANTIATION_EXCEPTION, baseClass.getCanonicalName());
        } catch (final IllegalAccessException e) {
            LOGGER.error("This should not happen: {}: {}: {}", e.getClass().getCanonicalName(), e.getMessage(), ExceptionUtil.causeChain(e));
            throw new T9tException(T9tException.CONSTRUCTOR_ILLEGAL_ACCESS_EXCEPTION, baseClass.getCanonicalName());
        }
    }

    @Override
    public <ENTITY> Class<ENTITY> getEntityClass(final int rtti, final Class<ENTITY> baseClass) {
        return baseClass;
    }

    @Override
    public List<String> getRequestHandlerClassnameCandidates(final RequestParameters params) {
        return requestHandlerResolver.getRequestHandlerClassnameCandidates(params.getClass());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <P extends RequestParameters> IRequestHandler<P> getRequestHandler(final P params) {
//        return (IRequestHandler<P>) requestHandlerLookup.getIfPresent(params);
        IRequestHandler<?> hdlr = tenantRequestHandlerCache.get(params.ret$PQON());
        if (hdlr == null) {
            // must create an instance. Only store it if no other is there first, to ensure that only the first one is ever used.
            hdlr = requestHandlerResolver.getHandlerInstance(params.getClass());
            tenantRequestHandlerCache.putIfAbsent(params.ret$PQON(), hdlr);
        }
        return (IRequestHandler<P>) hdlr;
    }
}
