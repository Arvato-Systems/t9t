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
package com.arvatosystems.t9t.auth.services;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.AuthenticationIssuerType;
import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.SessionParameters;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Various utility methods for authentication request handlers.
 */
public interface IAuthResponseUtil {
    boolean isUserAllowedToLogOn(@Nonnull RequestContext ctx, @Nonnull UserDTO userDto);
    boolean isApiKeyAllowed(@Nonnull RequestContext ctx, @Nonnull ApiKeyDTO apiKey);
    String authResponseFromJwt(@Nonnull JwtInfo jwt, @Nullable SessionParameters sp, @Nullable JwtInfo continuesFromJwt, @Nullable Long apiKeyRef);

    /** Creates a JWT authenticated via OpenID Connect or userId / password. */
    JwtInfo createJwt(@Nonnull UserDTO user, @Nonnull TenantDTO tenantDTO, @Nonnull AuthenticationIssuerType issuerType);

    /** Creates a JWT authenticated via API key. */
    JwtInfo createJwt(@Nonnull ApiKeyDTO apiKey, @Nonnull TenantDTO tenantDTO, @Nonnull UserDTO userDTO);
}
