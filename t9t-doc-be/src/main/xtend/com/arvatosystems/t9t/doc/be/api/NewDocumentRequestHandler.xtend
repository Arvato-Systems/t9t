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
package com.arvatosystems.t9t.doc.be.api;

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.AbstractRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.base.types.Recipient
import com.arvatosystems.t9t.doc.DocComponentDTO
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.doc.api.NewDocumentRequest
import com.arvatosystems.t9t.doc.api.NewDocumentResponse
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.recipients.RecipientArchive
import com.arvatosystems.t9t.doc.recipients.RecipientGenericTarget
import com.arvatosystems.t9t.doc.recipients.RecipientResponse
import com.arvatosystems.t9t.doc.services.IDocArchiveDistributor
import com.arvatosystems.t9t.doc.services.IDocConverter
import com.arvatosystems.t9t.doc.services.IDocEmailDistributor
import com.arvatosystems.t9t.doc.services.IDocFormatter
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess
import com.arvatosystems.t9t.doc.services.IDocUnknownDistributor
import com.arvatosystems.t9t.email.api.RecipientEmail
import com.arvatosystems.t9t.server.services.IAsyncRequestSender
import com.arvatosystems.t9t.server.services.IEvent
import com.google.common.collect.ImmutableList
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.dp.Inject
import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.function.Function

import static extension de.jpaw.dp.Jdp.*

// some comparison of image storing methods in on https://sendgrid.com/blog/embedding-images-emails-facts/
// also a good read: https://sendgrid.com/blog/keep-email-looking-great-in-any-inbox/

@AddLogger
class NewDocumentRequestHandler extends AbstractRequestHandler<NewDocumentRequest>  {
    static final List<String>   EMPTY_LIST = #[]
    static final DocEmailReceiverDTO BLANK_EMAIL_SETTINGS = new DocEmailReceiverDTO()

    @Inject IDocPersistenceAccess       persistenceAccess
    @Inject IDocModuleCfgDtoResolver    moduleConfigResolver
    @Inject IDocFormatter               docFormatter
    @Inject IAsyncRequestSender         remoter
    @Inject IEvent                      event
    @Inject IDocArchiveDistributor      docArchiveDistributor
    @Inject IDocEmailDistributor        docEmailDistributor
    @Inject IDocUnknownDistributor      docUnknownDistributor

    def protected String basename(String filename) {
        val i = filename.lastIndexOf("/")
        if (i < 0)
            return filename
        return filename.substring(i+1)
    }

    def protected boolean emailSettingsExist(DocEmailReceiverDTO it) {
        return replaceTo || replaceCc || replaceBcc || replaceReplyTo || replaceFrom
            || storeEmail
            || sendSpooled
            || emailSubject   !== null
            || defaultFrom    !== null
            || defaultReplyTo !== null
            || extraTo        !== null
            || extraCc        !== null
            || extraBcc       !== null
    }

    def protected List<String> merge(List<String> org, String add, boolean replace) {
        val addressesToAdd = if (add.nullOrEmpty) EMPTY_LIST else add.split(';').map[trim]
        if (replace)
            return addressesToAdd
        if (addressesToAdd.isEmpty)
            return org
        if (org.nullOrEmpty)
            return addressesToAdd
        // both are not empty, merge the lists
        val it = ImmutableList.<String>builder
        addAll(org)
        addAll(addressesToAdd)
        return build
    }

    def protected mergeEmailSettings(Recipient rcp, DocEmailReceiverDTO add) {
        if (add !== null) {
            if (rcp instanceof RecipientEmail) {
                return new RecipientEmail => [
                    replyTo = if (add.replaceReplyTo) add.defaultReplyTo else (rcp.replyTo ?: add.defaultReplyTo)
                    from    = if (add.replaceFrom)    add.defaultFrom    else (rcp.from    ?: add.defaultFrom)
                    to      = rcp.to.merge(add.extraTo, add.replaceTo)
                    cc      = rcp.cc.merge(add.extraCc, add.replaceCc)
                    bcc     = rcp.bcc.merge(add.extraBcc, add.replaceBcc)
                ]
            }
        }
        return rcp
    }

    def protected effectiveDocConfig(String templateId) {
        var String nextTemplateId = templateId
        for (;;) {
            val docConfigDto = persistenceAccess.getDocConfigDTO(nextTemplateId)
            if (docConfigDto.mappedId === null)
                return null     // mapped to "no op" / skip this document
            if (docConfigDto.documentId == docConfigDto.mappedId || Boolean.TRUE != docConfigDto.followMappedId)
                return docConfigDto  // found effective one, no further mapping
            nextTemplateId = docConfigDto.mappedId
        }
    }

    override NewDocumentResponse execute(RequestContext ctx, NewDocumentRequest request) {
        // read the tenant configuration
        val moduleCfg = moduleConfigResolver.moduleConfiguration
        val sharedTenantRef = ctx.tenantMapping.getSharedTenantRef(DocComponentDTO.class$rtti)
        var MediaData whereToSetAttachmentFileName = null

        // step 1: determine the actual target templateId
        val DocConfigDTO docConfigDto = effectiveDocConfig(request.documentId)
        if (docConfigDto === null) {
            // this case is not a problem but a regular configuration case, therefore it should log at debug level max.
            LOGGER.debug("TemplateId [{}] has not been mapped to a document. No document will be created!", request.documentId)
            return new NewDocumentResponse  // no List of archiveSinkRefs included in this case
        }
        val effectiveTimeZone = docConfigDto.timeZone ?: request.timeZone

        // step 2: check for altered email distribution list
        val emailRecipientOrNull = request.recipientList?.findFirst[RecipientEmail.isAssignableFrom(class)]
        val containsEmailRecipient = emailRecipientOrNull !== null
        val docEmailReceiverDto = (
            if (containsEmailRecipient && docConfigDto.emailConfigPerSelector) {
                // email settings have been configured to be language specific and are actually required for the specified distribution list: resolve them
                persistenceAccess.getDocEmailCfgDTO(moduleCfg, docConfigDto.documentId, request.documentSelector)?.emailSettings
            } else {
                // use the default settings
                docConfigDto.emailSettings
            }) ?: BLANK_EMAIL_SETTINGS

        val effectiveMainDocumentId = docConfigDto.mappedId ?: docConfigDto.documentId
        // step 3: check for forwarding to remote formatter
        if (docConfigDto.forwardToChannel !== null) {
            if (containsEmailRecipient && docEmailReceiverDto.emailSettingsExist) {
                // must merge email settings (extra CC or BCCs for example) into recipient list
                // respect that the original parameters are immutable
                val it = new NewDocumentRequest
                documentId       = effectiveMainDocumentId
                documentSelector = request.documentSelector
                data             = request.data
                recipientList    = request.recipientList.map[mergeEmailSettings(docEmailReceiverDto)]
                remoter.asyncRequest(docConfigDto.forwardToChannel, docConfigDto.forwardToAddress, it, docConfigDto.communicationFormat)
            } else {
                // simple forward, 1:1
                remoter.asyncRequest(docConfigDto.forwardToChannel, docConfigDto.forwardToAddress, request, docConfigDto.communicationFormat)
            }
            return new NewDocumentResponse  // no List of archiveSinkRefs included in this case
        }

        // step 4: format the document (textual conversions, i.e. into text, markdown, TeX, LaTeX etc)
        LOGGER.info("New document request for templateId {} (mapped to {}) with selectors {}", request.documentId, effectiveMainDocumentId, request.documentSelector)
        val generatedCidMap = new HashMap<String, MediaData>(32);
        val useCidsInMainDocument = docConfigDto.useCids && docConfigDto.emailBodyTemplateId === null  // CIDs are either used in the main document (no separate email body) or in the email body
        val formatted = docFormatter.formatDocument(sharedTenantRef, TemplateType.DOCUMENT_ID, effectiveMainDocumentId,
            request.documentSelector, effectiveTimeZone, request.data, if (useCidsInMainDocument) generatedCidMap
        )
        val attachmentList = new ArrayList<MediaData>(16)

        val please = new HashMap<MediaXType, MediaData>(8);     // map of the formatted document in various media types
        please.put(formatted.mediaType, formatted)

        // step 5: convert into DVI / PDF / PS etc. if requested
        var target = formatted
        if (docConfigDto.communicationFormat !== null && docConfigDto.communicationFormat != formatted.mediaType) {
            LOGGER.debug("Formatted document to {}, now converting to {}", formatted.mediaType, docConfigDto.communicationFormat)
            target = IDocConverter.getOptional(docConfigDto.communicationFormat.name)?.convert(formatted)
            if (target === null)
                throw new T9tException(T9tException.FIELD_MAY_NOT_BE_CHANGED, "Cannot convert to " + docConfigDto.communicationFormat.name)
            please.put(target.mediaType, target)
        }

        // if an email is among the distribution list and a separate email template has been defined (i.e. the actual document is an attachment),
        // also format the email body (otherwise, save the work for performance reasons)
        val emailBody =
            if (containsEmailRecipient && docConfigDto.emailBodyTemplateId !== null) {
                whereToSetAttachmentFileName = target
                attachmentList.add(target)
                docFormatter.formatDocument(sharedTenantRef, TemplateType.DOCUMENT_ID, docConfigDto.emailBodyTemplateId,
                    request.documentSelector, effectiveTimeZone, request.data, if (docConfigDto.useCids) generatedCidMap
                )
            } else {
                formatted  // use the text form
            }

        // Add all provided attachments
        if (request.attachments !== null)
            attachmentList.addAll(request.attachments);

        val emailSubject =
            if (containsEmailRecipient)
                docFormatter.formatDocument(
                    sharedTenantRef,
                    docEmailReceiverDto.subjectType  ?: TemplateType.COMPONENT,
                    docEmailReceiverDto.emailSubject ?: docConfigDto.documentId + "_subject",  // by default, use a component of name (templateId)_subject
                    request.documentSelector,
                    effectiveTimeZone,
                    request.data,
                    null
                )

        val sinkRefs = new ArrayList<Long>(request.recipientList.size)
        val datas = new ArrayList<MediaData>(request.recipientList.size)

        val Function<MediaXType, MediaData> toFormatConverter = [
            please.get(it) ?: IDocConverter.getRequired(name)?.convert(formatted) => [ away | please.put(it, away) ]
        ];
        // step 6: distribute it as requested
        for (rcpt : request.recipientList) {
            LOGGER.debug("distributing document in format {} / {} to {}", formatted.mediaType, target.mediaType, rcpt)
            switch (rcpt) {
            RecipientResponse:
                datas.add(toFormatConverter.apply(rcpt.communicationFormat ?: target.mediaType))
            RecipientGenericTarget:
                event.asyncEvent(rcpt.channel, rcpt.address, toFormatConverter.apply(rcpt.communicationFormat ?: target.mediaType))
            RecipientArchive: {
                val archiveResult = docArchiveDistributor.transmit(rcpt, toFormatConverter, target.mediaType, request.documentId, request.documentSelector)
                sinkRefs.add(archiveResult.sinkRef)
                if (whereToSetAttachmentFileName !== null && archiveResult.fileOrQueueName !== null) {
                    val fileBasename = basename(archiveResult.fileOrQueueName)
                    LOGGER.debug("Setting name of attachment of media type {} to {}", whereToSetAttachmentFileName.mediaType, fileBasename)
                    if (whereToSetAttachmentFileName.z === null)
                        whereToSetAttachmentFileName.z = new HashMap<String,Object>()
                    whereToSetAttachmentFileName.z.put("attachmentName", fileBasename)
                }
            }
            RecipientEmail: {
                // nothing here, emails delayed and sent as last receivers
            }
            default:
                docUnknownDistributor.transmit(rcpt, toFormatConverter, target.mediaType, request.documentId, request.documentSelector)
            }
        }

        if (containsEmailRecipient) {
            // optionally generate alternate email body (usually as plain text)
            val alternateBody =
                if (docConfigDto.alternateTemplateId !== null)
                    docFormatter.formatDocument(
                        sharedTenantRef,
                        TemplateType.DOCUMENT_ID, docConfigDto.alternateTemplateId,
                        request.documentSelector,
                        effectiveTimeZone,
                        request.data,
                        null
                    )
            for (rcpt : request.recipientList.filter[RecipientEmail.isAssignableFrom(class)]) {
                docEmailDistributor.transmit(rcpt.mergeEmailSettings(docEmailReceiverDto) as RecipientEmail, toFormatConverter, target.mediaType,
                    request.documentId, request.documentSelector,
                    emailSubject,
                    emailBody,
                    generatedCidMap,
                    alternateBody,
                    attachmentList,
                    docEmailReceiverDto.storeEmail, docEmailReceiverDto.sendSpooled
                )
            }
        }

        val response = new NewDocumentResponse
        if (sinkRefs.size > 0)
            response.archiveSinkRefs = sinkRefs;
        if (datas.size > 0)
            response.data = datas;
        return response
    }
}
