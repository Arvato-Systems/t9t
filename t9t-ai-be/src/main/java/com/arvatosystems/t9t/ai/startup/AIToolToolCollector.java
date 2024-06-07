package com.arvatosystems.t9t.ai.startup;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.ai.service.AIToolRegistry;
import com.arvatosystems.t9t.ai.service.IAITool;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(1234567)
public class AIToolToolCollector implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIToolToolCollector.class);

    @Override
    public void onStartup() {

        final Map<String, IAITool> tools = Jdp.getInstanceMapPerQualifier(IAITool.class);
        LOGGER.info("Registering {} tools", tools.size());
        for (final Map.Entry<String, IAITool> tool : tools.entrySet()) {
            final String pqon = tool.getKey();
            try {
                // determine BonPortableClass for this qualifier
                final BonaPortableClass bclass = BonaPortableFactory.getBClassForPqon(pqon);
                AIToolRegistry.register(tool.getValue(), bclass);
            } catch (final Exception e) {
                LOGGER.error("Cannot register tool {}: no BonaPortableClass: {}", pqon, e.getMessage());
            }
        }
    }
}
