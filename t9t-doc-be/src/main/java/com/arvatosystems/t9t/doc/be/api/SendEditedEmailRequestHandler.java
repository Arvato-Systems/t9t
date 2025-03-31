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
package com.arvatosystems.t9t.doc.be.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocEmailCfgDTO;
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.T9tDocExtException;
import com.arvatosystems.t9t.doc.api.SendEditedEmailRequest;
import com.arvatosystems.t9t.doc.api.SendEditedEmailResponse;
import com.arvatosystems.t9t.doc.recipients.RecipientArchive;
import com.arvatosystems.t9t.doc.services.DocArchiveResult;
import com.arvatosystems.t9t.doc.services.IDocArchiveDistributor;
import com.arvatosystems.t9t.doc.services.IDocConverter;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.api.SendEmailRequest;
import com.arvatosystems.t9t.email.api.SendEmailResponse;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class SendEditedEmailRequestHandler extends AbstractRequestHandler<SendEditedEmailRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendEditedEmailRequestHandler.class);
    private static final DocEmailReceiverDTO BLANK_EMAIL_SETTINGS = new DocEmailReceiverDTO();
    private static final String DATA_SINK_ID_CS_EMAIL_OUT = "CSEmailOut";

    private final IDocPersistenceAccess persistenceAccess = Jdp.getRequired(IDocPersistenceAccess.class);
    private final IDocModuleCfgDtoResolver moduleConfigResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    private final IDocArchiveDistributor docArchiveDistributor = Jdp.getRequired(IDocArchiveDistributor.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public SendEditedEmailResponse execute(final RequestContext ctx, final SendEditedEmailRequest request) {
        final DocModuleCfgDTO moduleCfg = moduleConfigResolver.getModuleConfiguration();
        final DocConfigDTO docConfigDto = effectiveDocConfig(request.getDocumentId());

        if (docConfigDto == null) {
            LOGGER.error("Document Config for documentID {} is not being setup.", request.getDocumentId());
            throw new T9tException(T9tDocExtException.CONFIGURATION_NOT_FOUND_ERROR, "DocumentId " + request.getDocumentId());
        }

        DocEmailReceiverDTO docEmailReceiverDTO = null;
        if (docConfigDto.getEmailConfigPerSelector()) {
            final DocEmailCfgDTO docEmailCfgDTO = persistenceAccess.getDocEmailCfgDTO(moduleCfg, request.getDocumentId(), request.getDocumentSelector());
            if (docEmailCfgDTO != null) {
                docEmailReceiverDTO = docEmailCfgDTO.getEmailSettings();
            }
        } else {
            docEmailReceiverDTO = docConfigDto.getEmailSettings();
        }
        if (docEmailReceiverDTO == null) {
            docEmailReceiverDTO = BLANK_EMAIL_SETTINGS;
        }

        final RecipientEmail recipientEmail = new RecipientEmail();
        if (docEmailReceiverDTO != null) {
            recipientEmail.setFrom(docEmailReceiverDTO.getDefaultFrom());
        }
        recipientEmail.setTo(Collections.singletonList(request.getTo()));

        final SendEmailRequest sendEmailRequest = new SendEmailRequest();
        final EmailMessage emailMessage = new EmailMessage();
        emailMessage.setMailBody(request.getMailMediaData());
        emailMessage.setRecipient(recipientEmail);
        emailMessage.setAttachments(Collections.emptyList());
        emailMessage.setCids(new HashMap<>());
        emailMessage.setReturnPath(docEmailReceiverDTO.getReturnPath());
        sendEmailRequest.setEmail(emailMessage);

        final RecipientArchive recipientArchive = new RecipientArchive();
        recipientArchive.setDataSinkId(DATA_SINK_ID_CS_EMAIL_OUT);
        recipientArchive.setOutputSessionParameters(new HashMap<>());

        final Map<MediaXType, MediaData> please = new HashMap<>(8);
        please.put(request.getMailMediaData().getMediaType(), request.getMailMediaData());

        final Function<MediaXType, MediaData> toFormatConverter = (final MediaXType it) -> {
            final MediaData away;
            if (please.get(it) != null) {
                away = please.get(it);
            } else {
                away = Jdp.getRequired(IDocConverter.class, it.name()).convert(request.getMailMediaData());
            }
            please.put(it, away);
            return away;
        };

        final DocArchiveResult archiveResult = docArchiveDistributor.transmit(recipientArchive, toFormatConverter, request.getMailMediaData().getMediaType(),
                request.getDocumentId(), request.getDocumentSelector());

        final SendEmailResponse res = executor.executeSynchronousAndCheckResult(ctx, sendEmailRequest, SendEmailResponse.class);
        final SendEditedEmailResponse rs = new SendEditedEmailResponse();
        rs.setReturnCode(res.getReturnCode());
        rs.setArchiveSinkRefs(archiveResult.sinkRef());
        rs.setEmailRef(rs.getEmailRef());

        if (res.getReturnCode() != ApplicationException.SUCCESS) {
            rs.setErrorDetails(res.getErrorDetails());
            res.setErrorMessage(res.getErrorMessage());
        }

        return rs;
    }

    protected DocConfigDTO effectiveDocConfig(final String templateId) {
        String nextTemplateId = templateId;
        for (;;) {
            final DocConfigDTO docConfigDto = persistenceAccess.getDocConfigDTO(nextTemplateId);
            if (docConfigDto.getMappedId() == null) {
                return null; // mapped to "no op" / skip this document
            }
            if (docConfigDto.getDocumentId().equals(docConfigDto.getMappedId()) || !Boolean.TRUE.equals(docConfigDto.getFollowMappedId())) {
                return docConfigDto; // found effective one, no further mapping
            }
            nextTemplateId = docConfigDto.getMappedId();
        }
    }
}
