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
package com.arvatosystems.t9t.auth.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.auth.services.IPasswordSyncService;
import com.arvatosystems.t9t.auth.services.PasswordSyncStatus;

/**
 * No-op fallback implementation of {@link IPasswordSyncService}.
 * Used when no password sync backend module (e.g. t9t-auth-be-openbao) is on the classpath.
 * Always returns {@link PasswordSyncStatus#DISABLED}.
 */
@Fallback
@Singleton
public class NoOpPasswordSyncService implements IPasswordSyncService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpPasswordSyncService.class);

    @Override
    public PasswordSyncStatus storePassword(final String userId, final String tenantId, final String password) {
        LOGGER.debug("Password sync is not configured - skipping storePassword for user {}/{}", tenantId, userId);
        return PasswordSyncStatus.DISABLED;
    }

    @Override
    public PasswordSyncStatus deletePassword(final String userId, final String tenantId) {
        LOGGER.debug("Password sync is not configured - skipping deletePassword for user {}/{}", tenantId, userId);
        return PasswordSyncStatus.DISABLED;
    }
}
