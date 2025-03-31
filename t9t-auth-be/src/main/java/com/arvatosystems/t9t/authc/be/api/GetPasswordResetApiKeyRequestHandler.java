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
package com.arvatosystems.t9t.authc.be.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authc.api.GetPasswordResetApiKeyRequest;
import com.arvatosystems.t9t.authc.api.GetPasswordResetApiKeyResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

public class GetPasswordResetApiKeyRequestHandler extends AbstractReadOnlyRequestHandler<GetPasswordResetApiKeyRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPasswordResetApiKeyRequestHandler.class);

    final T9tServerConfiguration serverCfg = ConfigProvider.getConfiguration();

    @Override
    public GetPasswordResetApiKeyResponse execute(RequestContext ctx, GetPasswordResetApiKeyRequest rq) {
        final GetPasswordResetApiKeyResponse resp = new GetPasswordResetApiKeyResponse();
        if (serverCfg.getPasswordResetApiKey() == null) {
            LOGGER.warn("No API key configured for Password RESET - forgot to migrate from properties file into server-config.xml?");
            // fall through
        }
        resp.setApiKey(serverCfg.getPasswordResetApiKey());  // null is allowed
        return resp;
    }
}
