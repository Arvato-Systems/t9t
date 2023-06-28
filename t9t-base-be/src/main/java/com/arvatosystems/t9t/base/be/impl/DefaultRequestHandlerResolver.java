/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.IRequestHandler;
import com.arvatosystems.t9t.base.services.IRequestHandlerResolver;

import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

/** Implements the global request handler resolver, which returns cached instances of request handler classes.
 * Functionality to actively override implementations is provided.
 *
 * Modules plugging into this, for example cache layers for resolvers, would first obtain the default implementation via the getHandlerInstance
 * method, and then plug in their wrapper using the setHandlerInstance method.
 *
 * If the default naming functionality should be altered, then this class must be extended and the interface's default implementation
 * of getRequestHandlerClassname overridden.
 */
@Singleton
public class DefaultRequestHandlerResolver implements IRequestHandlerResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestHandlerResolver.class);
    private final Map<String, IRequestHandler<?>> cachedHandlerInstances = new ConcurrentHashMap<>(250);

    /** Obtains a cached instance of a request handler, or creates a default one. */
    @Override
    public <RQ extends RequestParameters> IRequestHandler<RQ> getHandlerInstance(final Class<RQ> requestClass) {
        final IRequestHandler<?> instance = cachedHandlerInstances.computeIfAbsent(requestClass.getCanonicalName(), (x) -> {
            // create a new instance. It is accepted that in the beginning at application start, due to race conditions, multiple
            // instances of the same handler may be created and subsequently used. Long term, the last stored instance will be used
            // by subsequent calls.
            final List<String> handlerCandidates = getRequestHandlerClassnameCandidates(requestClass);
            String cause = "";
            for (final String handlerClassNameCandidate: handlerCandidates) {
                try {
                    final Class<?> handlerClass = Class.forName(handlerClassNameCandidate);
                    if (!IRequestHandler.class.isAssignableFrom(handlerClass)) {
                        LOGGER.error("Class {} is not a request handler class (i.e. does not implement the IRequestHandler interface",
                          handlerClass.getCanonicalName());
                        return new NoHandlerPresentRequestHandler(handlerClass.getCanonicalName() + " does not implement IRequestHandler<?>");
                    } else {
                        return (IRequestHandler<?>) handlerClass.newInstance();
                    }
                } catch (final ClassNotFoundException e) {
                    LOGGER.debug("Class {} not found - trying next candidate", handlerClassNameCandidate);
                } catch (final Exception e) {
                    // track causes
                    LOGGER.error("Cannot instantiate RequestHandler " + handlerClassNameCandidate, e);
                    cause = (cause.length() == 0 ? "" : cause + "; ") + handlerClassNameCandidate + ": " + ExceptionUtil.causeChain(e);
                }
            }
            final String finalCause = handlerCandidates.isEmpty() ? " (No candicates)"
              : (cause.length() == 0 ? " (None of the candidates found)" : (" (Reason: " + cause + ")"));
            LOGGER.error("Required request handler for {} not found, creating an exception handler.{}",
              requestClass.getCanonicalName(), finalCause);
            return new NoHandlerPresentRequestHandler(finalCause);
        });
        return (IRequestHandler<RQ>) instance;
    }

    /** Defines a new request handler for subsequent use. */
    @Override
    public <RQ extends RequestParameters> void setHandlerInstance(final Class<RQ> requestClass, final IRequestHandler<RQ> newInstance) {
        cachedHandlerInstances.put(requestClass.getCanonicalName(), newInstance);
    }
}
