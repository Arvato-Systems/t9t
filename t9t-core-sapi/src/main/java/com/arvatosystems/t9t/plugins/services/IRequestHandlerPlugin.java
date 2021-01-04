package com.arvatosystems.t9t.plugins.services;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Request;
import com.arvatosystems.t9t.plugins.request.ExecutePluginV1Response;

public interface IRequestHandlerPlugin extends PluginMethod<ExecutePluginV1Request, ExecutePluginV1Response> {

    /** Returns the API implemented. Will usually be provided by a default method. */
    @Override
    default String implementsApi() { return T9tConstants.PLUGIN_API_ID_REQUEST_HANDLER; }
}
