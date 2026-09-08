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
package com.arvatosystems.t9t.auth.services;

import jakarta.annotation.Nonnull;

/**
 * Generic service for synchronizing user passwords to/from an external secrets store.
 * Implementations may target OpenBao, HashiCorp Vault, AWS Secrets Manager, etc.
 *
 * <p>All methods return a {@link PasswordSyncStatus} to indicate whether the operation
 * succeeded, was intentionally skipped (no backend configured), or failed.
 * Callers are responsible for surfacing {@code ERROR} results to the operator.</p>
 */
public interface IPasswordSyncService {

    /**
     * Writes or updates the password secret for the given userId in the external store.
     *
     * @param userId      the user ID (used as the secret path key)
     * @param tenantId    the tenant ID (used to namespace the secret path)
     * @param password    the plaintext password to store
     * @return {@link PasswordSyncStatus#SUCCESS} if stored successfully,
     *         {@link PasswordSyncStatus#DISABLED} if no backend is configured,
     *         {@link PasswordSyncStatus#ERROR} if the operation failed
     */
    @Nonnull
    PasswordSyncStatus storePassword(@Nonnull String userId, @Nonnull String tenantId, @Nonnull String password);

    /**
     * Deletes the password secret for the given userId from the external store.
     *
     * @param userId      the user ID
     * @param tenantId    the tenant ID
     * @return {@link PasswordSyncStatus#SUCCESS} if deleted successfully,
     *         {@link PasswordSyncStatus#DISABLED} if no backend is configured,
     *         {@link PasswordSyncStatus#ERROR} if the operation failed
     */
    @Nonnull
    PasswordSyncStatus deletePassword(@Nonnull String userId, @Nonnull String tenantId);
}
