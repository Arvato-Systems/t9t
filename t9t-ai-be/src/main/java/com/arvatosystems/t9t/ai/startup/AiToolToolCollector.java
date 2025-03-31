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
package com.arvatosystems.t9t.ai.startup;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.AiToolRegistry;
import com.arvatosystems.t9t.ai.service.IAiTool;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(1234567)
public class AiToolToolCollector implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(AiToolToolCollector.class);

    @Override
    public void onStartup() {

        final Map<String, IAiTool> tools = Jdp.getInstanceMapPerQualifier(IAiTool.class);
        LOGGER.info("Registering {} tools", tools.size());
        for (final Map.Entry<String, IAiTool> tool : tools.entrySet()) {
            final String pqon = tool.getKey();
            try {
                // determine BonPortableClass for this qualifier
                final BonaPortableClass bclass = BonaPortableFactory.getBClassForPqon(pqon);
                AiToolRegistry.register(tool.getValue(), bclass);
            } catch (final Exception e) {
                LOGGER.error("Cannot register tool {}: no BonaPortableClass: {}", pqon, e.getMessage());
            }
        }
    }
}
