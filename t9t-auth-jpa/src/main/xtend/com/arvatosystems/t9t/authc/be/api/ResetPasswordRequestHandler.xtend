/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.authc.be.api

import com.arvatosystems.t9t.auth.PasswordUtil
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.NewDocumentRequest
import com.arvatosystems.t9t.email.api.RecipientEmail
import javax.persistence.NoResultException
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import com.arvatosystems.t9t.base.T9tException

// ADMIN functionality: reset the password of another user, or self service
@AddLogger
class ResetPasswordRequestHandler extends AbstractRequestHandler<ResetPasswordRequest>{
    @Inject IExecutor executor
    @Inject IUserEntityResolver userEntityResolver
    @Inject IPasswordSettingService passwordSettingService

    static final String RESET_PASSWORD_DOC_ID = "passwordReset"

    def protected UserEntity getUserIgnoringTenant(String userId) {
        val query = userEntityResolver.getEntityManager().createQuery(
            "SELECT e FROM UserEntity e WHERE e.userId = :userId", UserEntity);
        query.setParameter("userId", userId);
        try {
            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    override ServiceResponse execute(RequestContext ctx, ResetPasswordRequest rq) {
        // enabled only for administrators, by permissions - but this one is handled centrally, so we ignore
        val userEntity = getUserIgnoringTenant(rq.userId)
        if (userEntity === null)
            throw new T9tException(T9tException.NOT_AUTHENTICATED)  // user does not exist

        // validate that the user is active and that the email address matches
        if (!userEntity.isActive || !rq.emailAddress.equalsIgnoreCase(userEntity.emailAddress))
            throw new T9tException(T9tException.NOT_AUTHENTICATED)  // wrong email address

        // checks OK, proceed

        // The request creates an initial random password for the specified user, or creates a new password for that user.
        val newPassword = PasswordUtil.generateRandomPassword(T9tConstants.DEFAULT_RANDOM_PASS_LENGTH)

        passwordSettingService.setPasswordForUser(ctx, userEntity, newPassword)

        // The password is emailed to the user
        val emailRequest = new NewDocumentRequest => [
            documentId       = RESET_PASSWORD_DOC_ID
            data             = #{ "password" -> newPassword }

            // currently most selectors set to default TODO: is there a way this is not default?
            documentSelector = new DocumentSelector => [
                entityId     = "-"
                countryCode  = "XX"
                currencyCode = "XXX"
                languageCode = ctx.internalHeaderParameters.languageCode ?: "en"
            ]
            recipientList    = #[
                new RecipientEmail => [
                    to = newImmutableList(userEntity.emailAddress ?: "noEmail@void.com")
                ]
            ]
        ]
        executor.executeSynchronous(emailRequest)
        return ok
    }
}
