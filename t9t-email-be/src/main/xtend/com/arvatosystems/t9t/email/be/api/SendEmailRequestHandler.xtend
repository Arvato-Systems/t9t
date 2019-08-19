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
package com.arvatosystems.t9t.email.be.api

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.IRefGenerator
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.email.EmailDTO
import com.arvatosystems.t9t.email.T9tEmailException
import com.arvatosystems.t9t.email.api.SendEmailRequest
import com.arvatosystems.t9t.email.api.SendEmailResponse
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import java.util.UUID

@AddLogger
class SendEmailRequestHandler extends AbstractRequestHandler<SendEmailRequest> {
    public static final String DEFAULT_IMPLEMENTATION = "SMTP"

    @Inject
    protected IEmailModuleCfgDtoResolver moduleCfgResolver

    @Inject
    protected IRefGenerator refGenerator

    @Inject
    protected IEmailPersistenceAccess emailPersistenceAccess

    override SendEmailResponse execute(RequestContext ctx, SendEmailRequest rq) {

        // create a UUID for this message
        val messageId       = UUID.randomUUID

        // persist the message (optional: body)
        val messageRef      = refGenerator.generateRef(EmailDTO.class$rtti)

        // generate an OK message
        val okResponse = new SendEmailResponse => [
            emailRef        = messageRef
            emailMessageId  = messageId
        ]

        // persist the message, and possibly also attachments
        emailPersistenceAccess.persistEmail(messageRef, messageId, ctx, rq.email, rq.sendSpooled, rq.storeEmail)

        if (!rq.sendSpooled) {
            // TODO: refactor this code into a separate class which is used for spooled sending / resends as well
            if (ConfigProvider.isMocked("SMTP")) {
                LOGGER.info("email sending inhibited by configuration - skipping it")
                return okResponse
            } else {
                // read the module configuration
                val moduleCfg = moduleCfgResolver.getModuleConfiguration()
                val implementation = moduleCfg?.implementation ?: "SMTP";
                val implementingInstance = Jdp.getOptional(IEmailSender, implementation)
                if (implementingInstance === null) {
                    LOGGER.error("invalid configuration: Referenced implementation {} does not exist", implementation)
                    throw new T9tException(T9tEmailException.SMTP_IMPLEMENTATION_MISSING, implementation)
                }
                try {
                    implementingInstance.sendEmail(messageRef, messageId, rq.email, moduleCfg)  // all implementations return null, or throw an error
                    return okResponse
                } catch (Exception e) {
                    LOGGER.error("email sending exception {}: {}", e.class.simpleName, e.message)
                    e.printStackTrace
                    throw e
                }
            }
        } else {
            // OK so far, issues may pop up later
            return okResponse
        }
    }
}
