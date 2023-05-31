package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.request.GetServerInformationRequest;
import com.arvatosystems.t9t.base.request.GetServerInformationResponse;
import com.arvatosystems.t9t.base.request.StagingType;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

public class GetServerInformationRequestHandler extends AbstractReadOnlyRequestHandler<GetServerInformationRequest> {

    @Override
    public GetServerInformationResponse execute(RequestContext ctx, GetServerInformationRequest request) throws Exception {
        final GetServerInformationResponse resp = new GetServerInformationResponse();
        final T9tServerConfiguration serverCfg = ConfigProvider.getConfiguration();
        resp.setServerIdSelf(serverCfg.getServerIdSelf());
        resp.setStagingType(serverCfg.getStagingType() == null ? null : StagingType.valueOf(serverCfg.getStagingType()));
        return resp;
    }
}
