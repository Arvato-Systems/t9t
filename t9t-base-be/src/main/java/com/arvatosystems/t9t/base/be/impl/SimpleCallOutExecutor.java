/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.client.jdk11.RemoteConnection;

import de.jpaw.annotations.AddLogger;
import de.jpaw.dp.Singleton;

@AddLogger
@Singleton
public class SimpleCallOutExecutor implements IForeignRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleCallOutExecutor.class);

    protected final IRemoteConnection remoteConnection;

    public SimpleCallOutExecutor(String url) {
        LOGGER.info("Simple call out connector - custom for URL {}", url);
        remoteConnection = new RemoteConnection(url);
    }

    @Override
    public ServiceResponse execute(RequestContext ctx, RequestParameters rp) {
        final ServiceResponse resp = remoteConnection.execute("Bearer " + ctx.internalHeaderParameters.getEncodedJwt(), rp);
        if (resp == null) {
            final ServiceResponse resp2 = new ServiceResponse();
            resp2.setReturnCode(T9tException.UPSTREAM_NULL_RESPONSE);
            return resp2;
        }
        if (resp instanceof ServiceResponse) {
            return resp;
        } else {
            final ServiceResponse resp2 = new ServiceResponse();
            resp2.setReturnCode(T9tException.UPSTREAM_BAD_RESPONSE);
            resp2.setErrorDetails(resp.ret$PQON());
            return resp2;
        }
    }
}
