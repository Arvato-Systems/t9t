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
package com.arvatosystems.t9t.email.jpa.impl

import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.email.EmailStatus
import com.arvatosystems.t9t.email.api.EmailMessage
import com.arvatosystems.t9t.email.jpa.entities.EmailAttachmentsEntity
import com.arvatosystems.t9t.email.jpa.entities.EmailEntity
import com.arvatosystems.t9t.email.jpa.persistence.IEmailAttachmentsEntityResolver
import com.arvatosystems.t9t.email.jpa.persistence.IEmailEntityResolver
import com.arvatosystems.t9t.email.services.IEmailPersistenceAccess
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.UUID

@Singleton
class EmailPersistenceAccess implements IEmailPersistenceAccess {

    @Inject IEmailEntityResolver            emailEntityResolver
    @Inject IEmailAttachmentsEntityResolver emailAttachmentsEntityResolver

    override persistEmail(long myEmailRef, UUID myMessageId, RequestContext ctx, EmailMessage msg, boolean sendSpooled, boolean storeEmail) {
        val numAttachments = if (msg.attachments === null) 0 else msg.attachments.size
        val email = new EmailEntity => [
            objectRef                           = myEmailRef
            messageId                           = myMessageId
            emailSubject                        = msg.mailSubject
            emailFrom                           = msg.recipient.from
            replyTo                             = msg.recipient.replyTo
            emailTo                             = msg.recipient.to.join(';')
            emailCc                             = msg.recipient.cc?.join(';')
            emailBcc                            = msg.recipient.bcc?.join(';')
            numberOfAttachments                 = numAttachments
            emailStatus                         = if (!sendSpooled) EmailStatus.SENT else EmailStatus.UNSENT
        ]
        emailEntityResolver.save(email)  // sets shared tenantRef and persists

        if (sendSpooled || storeEmail) {
            // save any attachments if required (currently only body implemented)
            val emailBody = new EmailAttachmentsEntity => [
                emailRef                            = myEmailRef
                attachmentNo                        = 0
                document                            = msg.mailBody
            ]
            emailAttachmentsEntityResolver.save(emailBody)   // sets shared tenantRef and persists
        }
    }
}
