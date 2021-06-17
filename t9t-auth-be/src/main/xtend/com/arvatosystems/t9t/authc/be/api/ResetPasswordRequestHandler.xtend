/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.api.NewDocumentRequest
import com.arvatosystems.t9t.email.api.RecipientEmail
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject

// ADMIN functionality: reset the password of another user, or self service
@AddLogger
class ResetPasswordRequestHandler extends AbstractRequestHandler<ResetPasswordRequest>{
    @Inject IExecutor executor
    @Inject IAuthPersistenceAccess authPersistenceAccess

    static final String RESET_PASSWORD_DOC_ID = "passwordReset"


    override ServiceResponse execute(RequestContext ctx, ResetPasswordRequest rq) {
        // enabled only for administrators, by permissions - but this one is handled centrally, so we ignore
        val newPassword = authPersistenceAccess.assignNewPasswordIfEmailMatches(ctx, rq.userId, rq.emailAddress);

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
                    to = newImmutableList(rq.emailAddress)
                ]
            ]
        ]
        executor.executeSynchronous(emailRequest)
        return ok
    }
}
