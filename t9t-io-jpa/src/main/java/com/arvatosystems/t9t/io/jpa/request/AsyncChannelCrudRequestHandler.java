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
package com.arvatosystems.t9t.io.jpa.request;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.AsyncChannelDTO;
import com.arvatosystems.t9t.io.AsyncChannelRef;
import com.arvatosystems.t9t.io.jpa.entities.AsyncChannelEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IAsyncChannelDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IAsyncChannelEntityResolver;
import com.arvatosystems.t9t.io.request.AsyncChannelCrudRequest;

import de.jpaw.dp.Jdp;

public class AsyncChannelCrudRequestHandler extends
        AbstractCrudSurrogateKeyRequestHandler<AsyncChannelRef, AsyncChannelDTO, FullTrackingWithVersion, AsyncChannelCrudRequest, AsyncChannelEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncChannelCrudRequestHandler.class);

    protected final IAsyncChannelEntityResolver resolver = Jdp.getRequired(IAsyncChannelEntityResolver.class);
    protected final IAsyncChannelDTOMapper mapper = Jdp.getRequired(IAsyncChannelDTOMapper.class);

    @Override
    public CrudSurrogateKeyResponse<AsyncChannelDTO, FullTrackingWithVersion> execute(final RequestContext ctx,
            final AsyncChannelCrudRequest request) throws Exception {
        if (request.getData() != null) {
            final String authParam = request.getData().getAuthParam();
            if (authParam != null && authParam.startsWith(T9tConstants.HTTP_AUTH_PREFIX_USER_PW) && authParam.indexOf(':') > 0) {
                LOGGER.debug("auto-replacing http Basic auth with base64 encoded form");
                // auto-base64-encode
                final String encoded = Base64.getEncoder().encodeToString(
                    authParam.substring(T9tConstants.HTTP_AUTH_PREFIX_USER_PW.length()).getBytes(StandardCharsets.UTF_8));
                request.getData().setAuthParam(T9tConstants.HTTP_AUTH_PREFIX_USER_PW + encoded);
            }
        }
        return execute(ctx, mapper, resolver, request);
    }
}
