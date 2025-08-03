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
package com.arvatosystems.t9t.metrics.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * {@link MeterBinder} for exporting t9t version to metrics endpoint.<br>
 * Sample output:
 * <pre>t9t_info{version="9.0-SNAPSHOT",} 1.0</pre>
 */
public class T9tVersionMetrics implements MeterBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(T9tVersionMetrics.class);

    private static final String NAME = "t9t.info";
    private static final String DESCRIPTION = "t9t version info";
    private static final String UNKNOWN = "unknown";
    private static final String VERSION = "version";

    @Override
    public void bindTo(final MeterRegistry registry) {
        String implementationVersion = this.getClass().getPackage().getImplementationVersion();
        if (T9tUtil.isBlank(implementationVersion)) { // normal when starting from eclipse
            LOGGER.debug("Cannot read implementation version from maven manifest");
            implementationVersion = UNKNOWN;
        }

        Gauge.builder(NAME, () -> Long.valueOf(1)).description(DESCRIPTION).tags(VERSION, implementationVersion).strongReference(true).register(registry);
    }
}
