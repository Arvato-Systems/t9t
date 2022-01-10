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
package com.arvatosystems.t9t.email.be.api;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.email.EmailDTO;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.T9tEmailException;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.api.SendEmailRequest;
import com.arvatosystems.t9t.email.api.SendTestEmailRequest;
import com.arvatosystems.t9t.email.api.SendTestEmailResponse;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess;
import com.arvatosystems.t9t.email.services.IEmailSender;

import de.jpaw.dp.Jdp;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendTestEmailRequestHandler extends AbstractRequestHandler<SendTestEmailRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendTestEmailRequestHandler.class);

    public static final String DEFAULT_IMPLEMENTATION = "SMTP";

    protected final IEmailModuleCfgDtoResolver moduleCfgResolver = Jdp.getRequired(IEmailModuleCfgDtoResolver.class);
    protected final IRefGenerator refGenerator = Jdp.getRequired(IRefGenerator.class);
    protected final IEmailPersistenceAccess emailPersistenceAccess = Jdp.getRequired(IEmailPersistenceAccess.class);

    @Override
    public SendTestEmailResponse execute(final RequestContext ctx, final SendTestEmailRequest rq) throws Exception {
        // create a UUID for this message
        final UUID messageId = UUID.randomUUID();

        // persist the message (optional: body)
        final long messageRef = refGenerator.generateRef(EmailDTO.class$rtti());

        // generate an OK message
        final SendTestEmailResponse okResponse = new SendTestEmailResponse();
        okResponse.setRef(messageRef);
        okResponse.setId(messageId);

        if (ConfigProvider.isMocked("SMTP")) {
            LOGGER.info("email sending inhibited by configuration - skipping it");
            return okResponse;
        } else {
            final RecipientEmail recipientEmail = new RecipientEmail();
            recipientEmail.setFrom(moduleCfgResolver.getModuleConfiguration().getSmtpServerAddress());
            recipientEmail.setTo(Collections.singletonList(rq.getEmailAddress()));

            final EmailMessage email = new EmailMessage();
            email.setMailBody(rq.getEmailBody());
            email.setMailSubject(rq.getEmailSubject());
            email.setRecipient(recipientEmail);
            email.setAttachments(Collections.emptyList());
            email.setCids(new HashMap<>());

            final SendEmailRequest sendEmailRequest = new SendEmailRequest();
            sendEmailRequest.setEmail(email);

            // read the module configuration
            final EmailModuleCfgDTO moduleCfg = moduleCfgResolver.getModuleConfiguration();
            final String implementation = moduleCfg == null ? "SMTP" : moduleCfg.getImplementation();
            final IEmailSender implementingInstance = Jdp.getOptional(IEmailSender.class, implementation);
            if (implementingInstance == null) {
                LOGGER.error("invalid configuration: Referenced implementation {} does not exist", implementation);
                throw new T9tException(T9tEmailException.SMTP_IMPLEMENTATION_MISSING, implementation);
            }
            try {
                // all implementations return null, or throw an error
                implementingInstance.sendEmail(messageRef, messageId, sendEmailRequest.getEmail(), moduleCfg);
                return okResponse;
            } catch (Exception e) {
                LOGGER.error("email sending exception {}: {}", e.getClass().getSimpleName(), e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
    }
}
