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
package com.arvatosystems.t9t.server.services;

import jakarta.annotation.Nonnull;

import com.arvatosystems.t9t.base.auth.AuthenticationInfo;

/**
 * This interface provides implementations which transform Http request headers into prepared authentication information.
 * Implementations will use the configured issuers, and validate using nimbus-jose-jwt.
 */
public interface IExtAuthenticationProcessor {
    /**
     * Return encoded JWT as well as JwtInfo.
     * Throws an exception if authentication has been denied.
     *
     * @param encodedJwt the content of the encoded JWT, expected to be a Bearer token, without the "Bearer " prefix.
     **/
    @Nonnull
    AuthenticationInfo validateAndParseJwt(@Nonnull String encodedJwt);
}
