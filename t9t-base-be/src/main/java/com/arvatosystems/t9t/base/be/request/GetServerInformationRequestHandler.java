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
