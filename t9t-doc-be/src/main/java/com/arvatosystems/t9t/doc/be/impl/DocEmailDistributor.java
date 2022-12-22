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
package com.arvatosystems.t9t.doc.be.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.doc.api.DocumentSelector;
import com.arvatosystems.t9t.doc.services.IDocEmailDistributor;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.api.SendEmailRequest;
import com.arvatosystems.t9t.email.api.SendEmailResponse;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DocEmailDistributor implements IDocEmailDistributor {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocEmailDistributor.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    // documentTemplateId - the unmapped template ID
    @Override
    public Long transmit(final RecipientEmail rcpt, final Function<MediaXType, MediaData> toFormatConverter, final MediaXType primaryFormat,
            final String documentTemplateId, final DocumentSelector documentSelector, final MediaData emailSubject, final MediaData emailBody,
            final Map<String, MediaData> cids, final MediaData alternateBody, final List<MediaData> attachments, final boolean storeEmail,
            final boolean sendSpooled) {
        final MediaData payload = toFormatConverter.apply(rcpt.getCommunicationFormat() == null ? primaryFormat : rcpt.getCommunicationFormat());
        final SendEmailRequest rq = new SendEmailRequest();
        final EmailMessage emailMessage = new EmailMessage();
        emailMessage.setRecipient(rcpt);
        emailMessage.setMailSubject(emailSubject == null ? null : emailSubject.getText());
        emailMessage.setMailBody(emailBody == null ? payload : emailBody);
        emailMessage.setAlternateBody(alternateBody);
        emailMessage.setCids(cids);
        emailMessage.setAttachments(attachments);
        rq.setEmail(emailMessage);
        rq.setStoreEmail(storeEmail);
        rq.setSendSpooled(sendSpooled);

        final int attachmentsSize = attachments != null ? attachments.size() : 0;
        final int cidsSize = cids != null ? cids.size() : 0;
        final String alternateBodyType;
        if (alternateBody != null && alternateBody.getMediaType() != null && alternateBody.getMediaType().name() != null) {
            alternateBodyType = alternateBody.getMediaType().name();
        } else {
            alternateBodyType = "NULL";
        }
        LOGGER.info("Sending email in format {} for document ID {}, selector {} with {} attachments, {} CIDs, alternate body is of type {}, and subject {}",
                primaryFormat.name(), documentTemplateId, documentSelector, attachmentsSize, cidsSize, alternateBodyType, emailSubject);
        if (attachments != null) {
            list(attachments, "Attachment");
        }
        if (cids != null && cids.values() != null) {
            list(cids.values(), "CID");
        }

        final List<String> to = rcpt.getTo();
        if (to == null || to.isEmpty()) {
            LOGGER.info("Email has no TO: recipients, skipping it...");
            return null;
        } else {
            final SendEmailResponse resp = executor.executeSynchronousAndCheckResult(rq, SendEmailResponse.class);
            return resp.getEmailRef();
        }
    }

    protected void list(final Collection<MediaData> data, final String what) {
        for (final MediaData a : data) {
            LOGGER.debug("    {} has type {} and CID {} and file name {}", what, a.getMediaType().name(), field(a, "cid"),
                    field(a, T9tConstants.DOC_MEDIA_ATTACHMENT_NAME));
        }
    }

    protected Object field(final MediaData it, final String id) {
        Object value = null;
        final Map<String, Object> z = it.getZ();
        if (z != null && z.get(id) != null) {
            value = z.get(id);
        }
        if (value == null) {
            return "-";
        }
        return value;
    }
}
