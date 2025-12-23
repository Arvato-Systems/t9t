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
package com.arvatosystems.t9t.base.services;

import jakarta.annotation.Nonnull;

public interface IAuthSessionService {

    /** Invalidate (log out) a login session given by its session reference.
     *
     * @param ctx The request context.
     * @param sessionRef The reference of the session to be logged out.
     */
    void loginSessionInvalidation(@Nonnull RequestContext ctx, long sessionRef);

    /** Handle user session invalidation (all JWTs for that user).
     *
     * @param ctx The request context.
     * @param userId The user ID whose sessions are to be invalidated.
     * @param removeInvalidation If true, removes the invalidation entry instead of adding one.
     */
    void userSessionInvalidation(@Nonnull RequestContext ctx, @Nonnull String userId, boolean removeInvalidation);

    /** Handle user session invalidation (all JWTs for that user). Execute only on current and its cluster nodes (if there is any).
     *
     * @param ctx The request context.
     * @param userId The user ID whose sessions are to be invalidated.
     * @param removeInvalidation If true, removes the invalidation entry instead of adding one.
     */
    void userSessionInvalidationOnCurrentServer(@Nonnull RequestContext ctx, @Nonnull String userId, boolean removeInvalidation);

    /** Handle user session invalidation on uplink servers with internal service (all JWTs for that user).
     *
     * @param userId The user ID whose sessions are to be invalidated.
     * @param removeInvalidation If true, removes the invalidation entry instead of adding one.
     * @param encodedJwt The JWT to be used for authentication on the uplink server.
     */
    void userSessionInvalidationOnUplinkServer(@Nonnull String userId, boolean removeInvalidation, @Nonnull String encodedJwt);

    /** Handle API key session invalidation (all JWTs for that API key).
     *
     * @param ctx The request context.
     * @param apiKeyRef The API key objectRef whose sessions are to be invalidated.
     * @param removeInvalidation If true, removes the invalidation entry instead of adding one.
     */
    void apiKeySessionInvalidation(@Nonnull RequestContext ctx, long apiKeyRef, boolean removeInvalidation);
}
