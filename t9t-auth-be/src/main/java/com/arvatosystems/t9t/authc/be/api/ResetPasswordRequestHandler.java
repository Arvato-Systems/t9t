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
package com.arvatosystems.t9t.authc.be.api;

import com.arvatosystems.t9t.auth.T9tAuthException;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAuthCacheInvalidation;
import com.arvatosystems.t9t.base.services.IPasswordResetNotificationService;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

//ADMIN functionality: reset the password of another user, or self service
public class ResetPasswordRequestHandler extends AbstractRequestHandler<ResetPasswordRequest> {
    private final IPasswordResetNotificationService passwordResetNotificationService = Jdp.getRequired(IPasswordResetNotificationService.class);
    private final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);
    private final IAuthCacheInvalidation cacheInvalidator = Jdp.getRequired(IAuthCacheInvalidation.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ResetPasswordRequest request) throws Exception {
        // enabled only for administrators, by permissions - but this one is handled centrally, so we ignore
        if (!passwordResetNotificationService.isPasswordResetAllowed(ctx, request.getUserId())) {
            throw new T9tException(T9tAuthException.PASSWORD_RESET_NOT_ALLOWED);
        }
        final String newPassword = authPersistenceAccess.assignNewPasswordIfEmailMatches(ctx, request.getUserId(), request.getEmailAddress());
        cacheInvalidator.invalidateAuthCache(ctx, UserDTO.class.getSimpleName(), null, request.getUserId());

        passwordResetNotificationService.notifyUser(ctx, request.getEmailAddress(), newPassword);
        return ok();
    }
}
