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
package com.arvatosystems.t9t.base.be.lifecycle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;
import com.arvatosystems.t9t.server.services.IRequestLogger;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(30000)
public class Init30000FrameworkAndPlausis implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(Init30000FrameworkAndPlausis.class);
    private static final String UNSPECIFIED = "noop";

    @Override
    public void onStartup() {
        final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);
        // verify that RequestHandler implementations are not in any (no)DI scope (wouldn't hurt, but indicates extra overhead)
        Jdp.forAllEntries(AbstractRequestHandler.class, cls -> {
            LOGGER.warn("RequestHandlers don't need a Jdp scope: {}", cls.actualType.getCanonicalName());
        });

        // select the message log microservice
        Jdp.bindByQualifierWithFallback(IRequestLogger.class,
            cfg.getLogWriterConfiguration() == null ? UNSPECIFIED : cfg.getLogWriterConfiguration().getStrategy());

        // launch SOLR or a comparable text search engine
        Jdp.bindByQualifierWithFallback(ITextSearch.class,
            cfg.getSearchConfiguration() == null ? UNSPECIFIED : cfg.getSearchConfiguration().getStrategy());

        // Jdp.bindByQualifierWithFallback(ICustomization.class, "hammanish");  // use the default
    }
}
