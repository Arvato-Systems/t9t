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
package com.arvatosystems.t9t.out.services.oauth;

import com.arvatosystems.t9t.io.oauth.AccessTokenDTO;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Map;

public interface IAccessTokenClient<KEY> {

    /**
     * Get access token from cache or request new access token from OAuth server.
     *
     * @param cacheKey          cache key
     * @param clientId          client id
     * @param clientSecret      client secret
     * @param authServerUrl     OAuth server URL
     * @param authType          token auth type, if null then clientId and clientSecret will be sent in request body, else in header
     * @param additionalParam   implementation specific params
     * @param scope             OAuth scope
     * @return {@link AccessTokenDTO}
     */
    AccessTokenDTO getAccessToken(@Nonnull KEY cacheKey, @Nonnull String clientId, @Nonnull String clientSecret, @Nonnull String authServerUrl,
        @Nullable String authType, @Nullable Map<String, Object> additionalParam, @Nullable String scope);

    /**
     * Get access token from cache or request new access token from OAuth server.
     *
     * @param cacheKey          cache key
     * @param clientId          client id
     * @param clientSecret      client secret
     * @param authServerUrl     OAuth server URL
     * @param authType          token auth type, if null then clientId and clientSecret will be sent in request body, else in header
     * @param additionalParam   implementation specific params
     * @return {@link AccessTokenDTO}
     */
    AccessTokenDTO getAccessToken(@Nonnull KEY cacheKey, @Nonnull String clientId, @Nonnull String clientSecret, @Nonnull String authServerUrl,
        @Nullable String authType, @Nullable Map<String, Object> additionalParam);

    /**
     * Invalidate access token.
     * @param cacheKey      cache key
     */
    void invalidateAccessToken(@Nonnull KEY cacheKey);
}
