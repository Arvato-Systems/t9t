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

import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import de.jpaw.annotations.AddLogger
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver
import de.jpaw.dp.Inject
import com.arvatosystems.t9t.base.services.IRefGenerator
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess
import java.util.UUID
import com.arvatosystems.t9t.email.EmailDTO

import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.email.services.IEmailSender
import de.jpaw.dp.Jdp
import com.arvatosystems.t9t.email.T9tEmailException
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.email.api.SendTestEmailResponse
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.email.api.SendTestEmailRequest
import com.arvatosystems.t9t.email.api.SendEmailRequest
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.api.EmailMessage
import java.util.HashMap

@AddLogger
class SendTestEmailRequestHandler extends AbstractRequestHandler<SendTestEmailRequest>{
    public static final String DEFAULT_IMPLEMENTATION = "SMTP"

    @Inject
    protected IEmailModuleCfgDtoResolver moduleCfgResolver

    @Inject
    protected IRefGenerator refGenerator

    @Inject
    protected IEmailPersistenceAccess emailPersistenceAccess

     override SendTestEmailResponse execute(RequestContext ctx, SendTestEmailRequest rq){

     // create a UUID for this message
        val messageId       = UUID.randomUUID

        // persist the message (optional: body)
        val messageRef      = refGenerator.generateRef(EmailDTO.class$rtti)

        // generate an OK message
        val okResponse = new SendTestEmailResponse => [
            ref  = messageRef
            id  = messageId
        ]

        val recipientEmail = new RecipientEmail => [
            from = moduleCfgResolver.moduleConfiguration.smtpServerAddress
                        to = #[rq.emailAddress]
        ]

        val sendEmailRequest = new SendEmailRequest => [
            email = new EmailMessage => [
                mailBody = rq.emailBody
                mailSubject = rq.emailSubject
                recipient = recipientEmail
                attachments = #[]
                cids = new HashMap
            ]
        ]

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
                    implementingInstance.sendEmail(messageRef, messageId, sendEmailRequest.email, moduleCfg)  // all implementations return null, or throw an error
                    return okResponse
                } catch (Exception e) {
                    LOGGER.error("email sending exception {}: {}", e.class.simpleName, e.message)
                    e.printStackTrace
                    throw e
                }
   }
   }
   }
