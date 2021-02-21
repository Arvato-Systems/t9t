package com.arvatosystems.t9t.plugins.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Response;
import com.arvatosystems.t9t.plugins.services.IPluginDispatcher;

import de.jpaw.dp.Jdp;

/**
 * This request handler is just a wrapper around functionality implemented in plugins.
 * It also serves as a simple example how to invoke a plugin.
 * Its input and output parameters are generic.
 * If pluggable request handlers with more specific parameters are required, it is advised to create a separate wrapper specifically for those.
 */
public class ExecutePluginV1RequestHandler extends AbstractRequestHandler<ExecutePluginV1Request> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutePluginV1RequestHandler.class);

    protected final IPluginDispatcher<ExecutePluginV1Request,ExecutePluginV1Response> pluginDispatcher = Jdp.getRequired(IPluginDispatcher.class);

    @Override
    public ExecutePluginV1Response execute(RequestContext ctx, ExecutePluginV1Request request) throws Exception {
        final ExecutePluginV1Response result = new ExecutePluginV1Response();
        LOGGER.info("Calling request handler plugin {} with parameters {}", request.getQualifier(), request);
        pluginDispatcher.execute(T9tConstants.PLUGIN_API_ID_REQUEST_HANDLER, request.getQualifier(), ctx, request, result);
        LOGGER.info("Returning from request handler plugin {} with response {}", request.getQualifier(), result);
        return result;
    }
}
