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
package com.arvatosystems.t9t.base.be.auth;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.LogoutRequest;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAuthSessionService;
import com.arvatosystems.t9t.base.services.RequestContext;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutRequestHandler extends AbstractRequestHandler<LogoutRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutRequestHandler.class);

    private final IAuthSessionService authSessionService = Jdp.getRequired(IAuthSessionService.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final LogoutRequest request) throws Exception {
        final JwtInfo jwtInfo = ctx.internalHeaderParameters.getJwtInfo();
        if (jwtInfo.getSessionRef() == null) {
            throw new T9tException(T9tException.JWT_INCOMPLETE, "No session reference in JWT");
        }
        LOGGER.debug("Sending request to log out session {} for user {}", jwtInfo.getSessionRef(), jwtInfo.getUserId());
        authSessionService.loginSessionInvalidation(ctx, jwtInfo.getSessionRef());
        return ok();
    }
}
