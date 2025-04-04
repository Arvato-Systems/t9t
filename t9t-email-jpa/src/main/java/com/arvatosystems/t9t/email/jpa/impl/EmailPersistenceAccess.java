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
package com.arvatosystems.t9t.email.jpa.impl;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.email.EmailStatus;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.jpa.entities.EmailAttachmentsEntity;
import com.arvatosystems.t9t.email.jpa.entities.EmailEntity;
import com.arvatosystems.t9t.email.jpa.persistence.IEmailAttachmentsEntityResolver;
import com.arvatosystems.t9t.email.jpa.persistence.IEmailEntityResolver;
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.util.UUID;

@Singleton
public class EmailPersistenceAccess implements IEmailPersistenceAccess {

    private static final String RECIPIENT_DELIMITER = ";";

    private final IEmailEntityResolver            emailResolver = Jdp.getRequired(IEmailEntityResolver.class);
    private final IEmailAttachmentsEntityResolver emailAttachmentsEntityResolver = Jdp.getRequired(IEmailAttachmentsEntityResolver.class);

    @Override
    public Long persistEmail(final RequestContext ctx, final UUID myMessageId, final EmailMessage msg, final boolean sendSpooled, final boolean storeEmail,
        final String defaultReturnPath) {
        int numAttachments = 0;
        if (msg.getAttachments() != null) {
            numAttachments = msg.getAttachments().size();
        }
        final Long newKey = emailResolver.createNewPrimaryKey();
        final EmailEntity email = new EmailEntity();
        email.setObjectRef(newKey);
        email.setMessageId(myMessageId);
        email.setEmailSubject(msg.getMailSubject());
        email.setEmailFrom(msg.getRecipient().getFrom());
        email.setReplyTo(msg.getRecipient().getReplyTo());
        email.setEmailTo(String.join(RECIPIENT_DELIMITER, msg.getRecipient().getTo()));
        if (msg.getRecipient().getCc() != null) {
            email.setEmailCc(String.join(RECIPIENT_DELIMITER, msg.getRecipient().getCc()));
        }
        if (msg.getRecipient().getBcc() != null) {
            email.setEmailBcc(String.join(RECIPIENT_DELIMITER, msg.getRecipient().getBcc()));
        }
        email.setNumberOfAttachments(numAttachments);
        if (sendSpooled) {
            email.setEmailStatus(EmailStatus.UNSENT);
        } else {
            email.setEmailStatus(EmailStatus.SENT);
        }
        email.setReturnPath(msg.getReturnPath() != null ? msg.getReturnPath() : defaultReturnPath);
        emailResolver.save(email);                      // sets shared tenantId and persists

        if (sendSpooled || storeEmail) {
            // save any attachments if required (currently only body implemented)
            final EmailAttachmentsEntity emailBody = new EmailAttachmentsEntity();
            emailBody.setEmailRef(newKey);
            emailBody.setAttachmentNo(0);
            emailBody.setDocument(msg.getMailBody());
            emailAttachmentsEntityResolver.save(emailBody);   // sets shared tenantId and persists
        }
        return newKey;
    }

    @Override
    public Long createNewEmailPrimaryKey() {
        return emailResolver.createNewPrimaryKey();
    }
}
