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
import jakarta.annotation.Nullable;

/**
 * Generic Interface which provides a notification method to inform about a high risk situation.
 */
public interface IHighRiskSituationNotificationService {

    /**
     * Initiates implementation specific action to notify about user specific high risk actions.
     *
     * @param ctx the request context
     * @param changeType the type of the change
     * @param userId the user ID involved
     * @param userName the name of the user
     * @param emailAddress the email address of the user
     * @param newEmailAddress the new email address (in case of email change)
     */
    void notifyChange(@Nonnull RequestContext ctx, @Nonnull String changeType, @Nonnull String userId, @Nullable String userName,
       @Nullable String emailAddress, @Nullable String newEmailAddress    // the new email address (in case of email change)
    );
}
