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
package com.arvatosystems.t9t.base.services;

import jakarta.annotation.Nonnull;

/**
 * Generic Interface which provides a notification method to the user to inform about a new password.
 */
public interface IPasswordResetNotificationService {

    /**
     * Determines if password resets are allowed / supported for this server type.
     */
    boolean isPasswordResetAllowed(@Nonnull RequestContext ctx, @Nonnull String userId);

    /**
     * Notifies the user (for example via email).
     **/
    void notifyUser(@Nonnull RequestContext ctx, @Nonnull String emailAddress, @Nonnull String newPassword);
}
