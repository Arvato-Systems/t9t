/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.T9tEmailException;
import com.arvatosystems.t9t.email.api.SendEmailRequest;
import com.arvatosystems.t9t.email.api.SendEmailResponse;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess;
import com.arvatosystems.t9t.email.services.IEmailSender;

import de.jpaw.dp.Jdp;

public class SendEmailRequestHandler extends AbstractRequestHandler<SendEmailRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEmailRequestHandler.class);

    public static final String DEFAULT_IMPLEMENTATION = "SMTP";

    protected final IEmailModuleCfgDtoResolver moduleCfgResolver = Jdp.getRequired(IEmailModuleCfgDtoResolver.class);
    protected final IRefGenerator refGenerator = Jdp.getRequired(IRefGenerator.class);
    protected final IEmailPersistenceAccess emailPersistenceAccess = Jdp.getRequired(IEmailPersistenceAccess.class);

    @Override
    public SendEmailResponse execute(final RequestContext ctx, final SendEmailRequest rq) throws Exception {
        // create a UUID for this message
        final UUID messageId = UUID.randomUUID();

        // persist the message, and possibly also attachments
        final Long messageRef = emailPersistenceAccess.persistEmail(ctx, messageId, rq.getEmail(), rq.getSendSpooled(), rq.getStoreEmail());

        // generate an OK message
        final SendEmailResponse okResponse = new SendEmailResponse();
        okResponse.setEmailRef(messageRef);
        okResponse.setEmailMessageId(messageId);

        if (!rq.getSendSpooled()) {
            // TODO: refactor this code into a separate class which is used for spooled sending / resends as well
            if (ConfigProvider.isMocked("SMTP")) {
                LOGGER.info("email sending inhibited by configuration - skipping it");
                return okResponse;
            } else {
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
                    implementingInstance.sendEmail(messageRef, messageId, rq.getEmail(), moduleCfg);
                    return okResponse;
                } catch (Exception e) {
                    LOGGER.error("email sending exception {}: {}", e.getClass().getSimpleName(), e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
            }
        } else {
            // OK so far, issues may pop up later
            return okResponse;
        }
    }
}
