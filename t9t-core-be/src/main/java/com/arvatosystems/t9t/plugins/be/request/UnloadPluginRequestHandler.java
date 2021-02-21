package com.arvatosystems.t9t.plugins.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.request.UnloadPluginRequest;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.dp.Jdp;

public class UnloadPluginRequestHandler extends AbstractRequestHandler<UnloadPluginRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnloadPluginRequestHandler.class);

    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, UnloadPluginRequest rq) throws Exception {
        final boolean removed = pluginManager.removePlugin(ctx.tenantRef, rq.getPluginId());
        LOGGER.info("The plugin {} {}", rq.getPluginId(), removed ? "has been removed" : "was not found");
        return ok();
    }
}
