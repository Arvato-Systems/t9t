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
package com.arvatosystems.t9t.base.be.lifecycle;

import java.lang.reflect.Modifier;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.base.services.ICustomization;
import com.arvatosystems.t9t.base.services.ITenantCustomization;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.ReflectionsPackageCache;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import de.jpaw.util.ExceptionUtil;


// instantiate all LeanSearchRequestHandlers. This causes them to register in IAnyKeySearchRegistry, which is required for the ResolveAnyRefRequest to work

@Startup(30052)
public class Init30052AnyRefResolver implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init30052AnyRefResolver.class);

    @Override
    public void onStartup() {
        LOGGER.info("Collecting any LeanSearchRequest handlers...");
        final ICustomization customizationProvider = Jdp.getRequired(ICustomization.class);
        final ITenantCustomization globalCustomization = customizationProvider.getTenantCustomization(T9tConstants.GLOBAL_TENANT_REF42, T9tConstants.GLOBAL_TENANT_ID);
        // reuse the results of some prior scan (or scan now)
        final Reflections reflections = ReflectionsPackageCache.get(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        int count = 0;
        for (Class<? extends LeanSearchRequest> cls : reflections.getSubTypesOf(LeanSearchRequest.class)) {
            if (!Modifier.isAbstract(cls.getModifiers())) {
                // create an instance of this request, and use it to access the request handler
                try {
                    final LeanSearchRequest dummyRequestInstance = cls.getDeclaredConstructor().newInstance();
                    globalCustomization.getRequestHandler(dummyRequestInstance);
                    ++count;
                } catch (Exception e) {
                    LOGGER.warn("Exception obtaining instance of request class {}: {}", cls.getCanonicalName(), ExceptionUtil.causeChain(e));
                }
            }
        }
        LOGGER.info("Found {} instances of LeanSearchRequest {}", count);
    }
}
