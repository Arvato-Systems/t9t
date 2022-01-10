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
package com.arvatosystems.t9t.email.be.stubs;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.services.IEmailSender;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IEmailSender using a /dev/null (stub).
 */
@Singleton
@Named("NULL")
public class NoopEmailService implements IEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopEmailService.class);

    @Override
    public ServiceResponse sendEmail(final Long messageRef, final UUID messageId, final EmailMessage msg, final EmailModuleCfgDTO configuration) {
        LOGGER.info("Not sending email ref {}, ID {} to {} (stub NULL configured)", messageRef, messageId, msg.getRecipient().getTo().get(0));
        return new ServiceResponse();
    }
}
