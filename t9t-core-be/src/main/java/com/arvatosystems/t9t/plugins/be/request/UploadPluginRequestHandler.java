package com.arvatosystems.t9t.plugins.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.PluginInfo;
import com.arvatosystems.t9t.plugins.request.UploadPluginRequest;
import com.arvatosystems.t9t.plugins.request.UploadPluginResponse;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.dp.Jdp;

public class UploadPluginRequestHandler extends AbstractRequestHandler<UploadPluginRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadPluginRequestHandler.class);

    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public UploadPluginResponse execute(RequestContext ctx, UploadPluginRequest rq) throws Exception {
        final PluginInfo info = pluginManager.loadPlugin(ctx.tenantRef, rq.getJarFile());
        final UploadPluginResponse resp = new UploadPluginResponse();
        LOGGER.info("Temporarily loaded plugin {} of version {}", info.getPluginId(), info.getVersion());
        resp.setPluginInfo(info);
        return resp;
    }
}
