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
package com.arvatosystems.t9t.doc.be.impl

import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.api.DocumentSelector
import com.arvatosystems.t9t.doc.services.IDocEmailDistributor
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.email.api.SendEmailRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Singleton
import java.util.Collection
import java.util.List
import java.util.Map
import java.util.function.Function
import com.arvatosystems.t9t.email.api.EmailMessage

@Singleton
@AddLogger
class DocEmailDistributor implements IDocEmailDistributor {

    @Inject IExecutor executor
    @Inject Provider<RequestContext> ctxProvider

    override transmit(
        RecipientEmail      rcpt,
        Function<MediaXType, MediaData> toFormatConverter,
        MediaXType          primaryFormat,
        String              documentTemplateId,      // the unmapped template ID
        DocumentSelector    documentSelector,
        MediaData           emailSubject,
        MediaData           emailBody,
        Map<String,MediaData> cids,
        MediaData           alternateBody,
        List<MediaData>     attachments,
        boolean             storeEmail,
        boolean             sendSpooled
    ) {
        val payload         = toFormatConverter.apply(rcpt.communicationFormat ?: primaryFormat)
        val rq              = new SendEmailRequest => [
            email = new EmailMessage => [
                recipient       = rcpt
                mailSubject     = emailSubject?.text
                mailBody        = emailBody ?: payload
                it.alternateBody = alternateBody
                it.cids         = cids
                it.attachments  = attachments
            ]
            it.storeEmail   = storeEmail
            it.sendSpooled  = sendSpooled
        ]
        LOGGER.info("Sending email in format {} for document ID {}, selector {} with {} attachments, {} CIDs, alternate body is of type {}, and subject {}",
            primaryFormat.name,
            documentTemplateId,
            documentSelector,
            if (attachments !== null) attachments.size else 0,
            if (cids !== null) cids.size else 0,
            alternateBody?.mediaType?.name ?: "NULL",
            emailSubject
        )
        attachments?.list("Attachment")
        cids?.values?.list("CID")

        if (rcpt.to.nullOrEmpty)
            LOGGER.info("Email has no TO: recipients, skipping it...")
        else
            executor.executeSynchronous(ctxProvider.get, rq)
    }

    def protected void list(Collection<MediaData> data, String what) {
        for (a : data)
            LOGGER.debug("    {} has type {} and CID {} and file name {}", what, a.mediaType.name, a.field("cid"), a.field("attachmentName"))
    }
    def protected field(MediaData it, String id) {
        return z?.get(id) ?: '-'
    }
}
