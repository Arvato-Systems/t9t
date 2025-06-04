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

import static com.arvatosystems.t9t.base.T9tUtil.nvl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.Recipient;
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocEmailCfgDTO;
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.api.GeneralizedAttachment;
import com.arvatosystems.t9t.doc.api.NewDocumentRequest;
import com.arvatosystems.t9t.doc.api.NewDocumentResponse;
import com.arvatosystems.t9t.doc.api.TemplateType;
import com.arvatosystems.t9t.doc.recipients.RecipientArchive;
import com.arvatosystems.t9t.doc.recipients.RecipientGenericTarget;
import com.arvatosystems.t9t.doc.recipients.RecipientResponse;
import com.arvatosystems.t9t.doc.services.DocArchiveResult;
import com.arvatosystems.t9t.doc.services.IDocArchiveDistributor;
import com.arvatosystems.t9t.doc.services.IDocConverter;
import com.arvatosystems.t9t.doc.services.IDocEmailDistributor;
import com.arvatosystems.t9t.doc.services.IDocFormatter;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.doc.services.IDocPersistenceAccess;
import com.arvatosystems.t9t.doc.services.IDocUnknownDistributor;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.server.services.IAsyncRequestSender;
import com.arvatosystems.t9t.server.services.IEvent;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.bonaparte.util.FreezeTools;
import de.jpaw.dp.Jdp;

//some comparison of image storing methods in on https://sendgrid.com/blog/embedding-images-emails-facts/
//also a good read: https://sendgrid.com/blog/keep-email-looking-great-in-any-inbox/

public class NewDocumentRequestHandler extends AbstractRequestHandler<NewDocumentRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewDocumentRequestHandler.class);
    private static final List<String> EMPTY_LIST = ImmutableList.of();

    private static final DocEmailReceiverDTO BLANK_EMAIL_SETTINGS = new DocEmailReceiverDTO();

    private final IDocPersistenceAccess persistenceAccess = Jdp.getRequired(IDocPersistenceAccess.class);
    private final IDocModuleCfgDtoResolver moduleConfigResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    private final IDocFormatter docFormatter = Jdp.getRequired(IDocFormatter.class);
    private final IAsyncRequestSender remoter = Jdp.getRequired(IAsyncRequestSender.class);
    private final IEvent event = Jdp.getRequired(IEvent.class);
    private final IDocArchiveDistributor docArchiveDistributor = Jdp.getRequired(IDocArchiveDistributor.class);
    private final IDocEmailDistributor docEmailDistributor = Jdp.getRequired(IDocEmailDistributor.class);
    private final IDocUnknownDistributor docUnknownDistributor = Jdp.getRequired(IDocUnknownDistributor.class);

    private boolean emailSettingsExist(final DocEmailReceiverDTO it) {
        return it.getReplaceTo()    || it.getReplaceCc()  || it.getReplaceBcc() || it.getReplaceReplyTo()
            || it.getReplaceFrom()  || it.getStoreEmail() || it.getSendSpooled()
            || it.getEmailSubject() != null || it.getDefaultFrom() != null || it.getDefaultReplyTo() != null
            || it.getExtraTo()      != null || it.getExtraCc()     != null || it.getExtraBcc()       != null;
    }

    private static List<String> merge(final List<String> org, String add, final boolean replace) {
        final List<String> addressesToAdd;
        if (add == null || add.isEmpty()) {
            addressesToAdd = EMPTY_LIST;
        } else {
            final String[] addresses = add.split(";");
            addressesToAdd = new ArrayList<>(addresses.length);
            for (final String s: addresses) {
                addressesToAdd.add(s.trim());
            }
        }

        if (replace) {
            return addressesToAdd;
        }

        if (addressesToAdd.isEmpty()) {
            return org;
        }
        if (org == null || org.isEmpty()) {
            return addressesToAdd;
        }
        // both are not empty, merge the lists
        final List<String> combined = new ArrayList<>(org.size() + addressesToAdd.size());
        combined.addAll(org);
        combined.addAll(addressesToAdd);
        return combined;
    }

    private static void noOp() {
    }

    private static Recipient mergeEmailSettings(final Recipient rcp, final DocEmailReceiverDTO add) {
        if (add != null) {
            if (rcp instanceof RecipientEmail rcpIn) {
                final RecipientEmail rcpOut = new RecipientEmail();
                rcpOut.setReplyTo(add.getReplaceReplyTo() ? add.getDefaultReplyTo() : nvl(rcpIn.getReplyTo(), add.getDefaultReplyTo()));
                rcpOut.setFrom   (add.getReplaceFrom()    ? add.getDefaultFrom()    : nvl(rcpIn.getFrom(),    add.getDefaultFrom()));
                rcpOut.setTo     (merge(rcpIn.getTo(),  add.getExtraTo(),  add.getReplaceTo()));
                rcpOut.setCc     (merge(rcpIn.getCc(),  add.getExtraCc(),  add.getReplaceCc()));
                rcpOut.setBcc    (merge(rcpIn.getBcc(), add.getExtraBcc(), add.getReplaceBcc()));
                return rcpOut;
            }
        }
        return rcp;
    }

    private static List<Recipient> mergeEmailSettings(final List<Recipient> rcps, final DocEmailReceiverDTO add) {
        final List<Recipient> mergedResult = new ArrayList<>(rcps.size());
        for (final Recipient rc: rcps) {
            mergedResult.add(mergeEmailSettings(rc, add));
        }
        return mergedResult;
    }

    private DocConfigDTO effectiveDocConfig(final String templateId) {
        String nextTemplateId = templateId;
        for (;;) {
            final DocConfigDTO docConfigDto = persistenceAccess.getDocConfigDTO(nextTemplateId);
            if (docConfigDto.getMappedId() == null)
                return null;     // mapped to "no op" / skip this document
            if (docConfigDto.getDocumentId().equals(docConfigDto.getMappedId()) || !Boolean.TRUE.equals(docConfigDto.getFollowMappedId()))
                return docConfigDto;  // found effective one, no further mapping
            nextTemplateId = docConfigDto.getMappedId();
        }
    }

    @Override
    public NewDocumentResponse execute(RequestContext ctx, NewDocumentRequest request) {
        // read the tenant configuration
        final DocModuleCfgDTO moduleCfg = moduleConfigResolver.getModuleConfiguration();
        final String sharedTenantId = ctx.tenantMapping.getSharedTenantId(DocComponentDTO.class$rtti());
        MediaData whereToSetAttachmentFileName = null;

        // step 1: determine the actual target templateId
        final DocConfigDTO docConfigDto = effectiveDocConfig(request.getDocumentId());
        if (docConfigDto == null) {
            // this case is not a problem but a regular configuration case, therefore it should log at debug level max.
            LOGGER.debug("TemplateId [{}] has not been mapped to a document. No document will be created!", request.getDocumentId());
            return new NewDocumentResponse();
        }

        final String effectiveTimeZone = nvl(docConfigDto.getTimeZone(), request.getTimeZone());

        // step 2: check for altered email distribution list
        RecipientEmail emailRecipientOrNull = null;
        if (request.getRecipientList() != null) {
            for (final Recipient rcp: request.getRecipientList()) {
                if (rcp instanceof RecipientEmail rcpEmail) {
                    emailRecipientOrNull = rcpEmail;
                    break;
                }
            }
        }
        // determine the email receiver (if any)
        final boolean containsEmailRecipient = emailRecipientOrNull != null;
        DocEmailReceiverDTO docEmailReceiverDto = null;
        if (containsEmailRecipient && docConfigDto.getEmailConfigPerSelector()) {
            // email settings have been configured to be language specific and are actually required for the specified distribution list: resolve them
            final DocEmailCfgDTO dec = persistenceAccess.getDocEmailCfgDTO(moduleCfg, docConfigDto.getDocumentId(), request.getDocumentSelector());
            if (dec != null) {
                docEmailReceiverDto = dec.getEmailSettings();
            }
        } else {
            // use the default settings
            docEmailReceiverDto = docConfigDto.getEmailSettings();
        }
        if (docEmailReceiverDto == null) {
            docEmailReceiverDto = BLANK_EMAIL_SETTINGS;
        }

        final String effectiveMainDocumentId = nvl(docConfigDto.getMappedId(), docConfigDto.getDocumentId());
        // step 3: check for forwarding to remote formatter
        if (docConfigDto.getForwardToChannel() != null) {
            if (containsEmailRecipient && emailSettingsExist(docEmailReceiverDto)) {
                // must merge email settings (extra CC or BCCs for example) into recipient list
                // respect that the original parameters are immutable
                final NewDocumentRequest ndr = new NewDocumentRequest();
                ndr.setDocumentId(effectiveMainDocumentId);
                ndr.setDocumentSelector(request.getDocumentSelector());
                ndr.setData(request.getData());
                ndr.setRecipientList(mergeEmailSettings(request.getRecipientList(), docEmailReceiverDto));
                remoter.asyncRequest(docConfigDto.getForwardToChannel(), docConfigDto.getForwardToAddress(), ndr, docConfigDto.getCommunicationFormat());
            } else {
                // simple forward, 1:1
                remoter.asyncRequest(docConfigDto.getForwardToChannel(), docConfigDto.getForwardToAddress(), request, docConfigDto.getCommunicationFormat());
            }
            return new NewDocumentResponse();  // no List of archiveSinkRefs included in this case
        }

        // step 4: format the document (textual conversions, i.e. into text, markdown, TeX, LaTeX etc)
        LOGGER.info("New document request for templateId {} (mapped to {}) with selectors {}",
          request.getDocumentId(), effectiveMainDocumentId, request.getDocumentSelector());
        final Map<String, MediaData> generatedCidMap = new HashMap<String, MediaData>(32);
        final boolean useCidsInMainDocument = docConfigDto.getUseCids() && docConfigDto.getEmailBodyTemplateId() == null;
        // CIDs are either used in the main document (no separate email body) or in the email body
        final MediaData formatted = docFormatter.formatDocument(ctx.tenantId, sharedTenantId, TemplateType.DOCUMENT_ID, effectiveMainDocumentId,
            request.getDocumentSelector(), effectiveTimeZone, request.getData(), useCidsInMainDocument ? generatedCidMap : null
        );
        final List<MediaData> attachmentList = new ArrayList<MediaData>(16);

        final Map<MediaXType, MediaData> dataCache = new HashMap<MediaXType, MediaData>(8);     // map of the formatted document in various media types
        dataCache.put(formatted.getMediaType(), formatted);

        // step 5: convert into DVI / PDF / PS etc. if requested
        final MediaData target = this.convertDocument(docConfigDto, formatted);
        if (!Objects.equals(target, formatted)) { // put to cache if it has been converted
            dataCache.put(target.getMediaType(), target);
        }

        // if an email is among the distribution list and a separate email template has been defined (i.e. the actual document is an attachment),
        // also format the email body (otherwise, save the work for performance reasons)
        final MediaData emailBody;
        if (containsEmailRecipient && docConfigDto.getEmailBodyTemplateId() != null) {
            // scenario where the main document is the attachment and the referenced document is the email body
            whereToSetAttachmentFileName = target;
            attachmentList.add(target);
            emailBody = docFormatter.formatDocument(ctx.tenantId, sharedTenantId, TemplateType.DOCUMENT_ID, docConfigDto.getEmailBodyTemplateId(),
                request.getDocumentSelector(), effectiveTimeZone, request.getData(), docConfigDto.getUseCids() ? generatedCidMap : null
            );
        } else {
            emailBody = formatted;  // use the text form
        }

        // Add all provided attachments
        if (request.getAttachments() != null) {
            attachmentList.addAll(request.getAttachments());
        }

        final RecipientArchive recipientArchive = request.getRecipientList().stream().filter(RecipientArchive.class::isInstance)
                .map(RecipientArchive.class::cast).findFirst().orElse(null);
        final Map<String, Long> generalAttachmentSinkRefs = this.addGeneralAttachments(ctx, request, recipientArchive, attachmentList, sharedTenantId,
                effectiveTimeZone);

        final MediaData emailSubject = containsEmailRecipient
          ? docFormatter.formatDocument(
                ctx.tenantId,
                sharedTenantId,
                nvl(docEmailReceiverDto.getSubjectType(), TemplateType.COMPONENT),
                nvl(docEmailReceiverDto.getEmailSubject(), docConfigDto.getDocumentId() + "_subject"),  // default component of name (templateId)_subject
                request.getDocumentSelector(),
                effectiveTimeZone,
                request.getData(),
                null
            )
          : null;

        final List<Long> sinkRefs = new ArrayList<Long>(request.getRecipientList().size());
        final List<MediaData> datas = new ArrayList<MediaData>(request.getRecipientList().size());

        final Function<MediaXType, MediaData> toFormatConverter = (mediaType) -> {
            MediaData result = dataCache.get(mediaType);
            if (result == null) {
                LOGGER.debug("Found no data for [{}] in dataCache", mediaType);
                final IDocConverter converter = Jdp.getRequired(IDocConverter.class, mediaType.name());
                result = converter.convert(formatted);
                dataCache.put(mediaType, result);  // store the new intermediate result
            }
            return result;
        };

        // step 6: distribute it as requested
        for (Recipient rcpt: request.getRecipientList()) {
            LOGGER.debug("distributing document in format {} / {} to {}", formatted.getMediaType(), target.getMediaType(), rcpt);
            // switch (rcpt) {  // need Java 17 for typed switch
            if (rcpt instanceof RecipientResponse) {
                datas.add(toFormatConverter.apply(nvl(rcpt.getCommunicationFormat(), target.getMediaType())));
            } else if (rcpt instanceof RecipientGenericTarget rcptt) {
                event.asyncEvent(rcptt.getChannel(), rcptt.getAddress(), toFormatConverter.apply(nvl(rcptt.getCommunicationFormat(), target.getMediaType())));
            } else if (rcpt instanceof RecipientArchive rcpta) {
                final DocArchiveResult archiveResult = docArchiveDistributor.transmit(rcpta, toFormatConverter, target.getMediaType(),
                  request.getDocumentId(), request.getDocumentSelector());
                sinkRefs.add(archiveResult.sinkRef());
                this.prepareAttachmentName(whereToSetAttachmentFileName, archiveResult);
            } else if (rcpt instanceof RecipientEmail) {
                // nothing here, emails delayed and sent as last receivers
                noOp();  // stupid code to keep checkstyle happy (don't want negative conditions)
            } else {
                docUnknownDistributor.transmit(rcpt, toFormatConverter, target.getMediaType(), request.getDocumentId(), request.getDocumentSelector());
            }
        }

        final List<Long> emailRefs = new ArrayList<>(4);
        if (containsEmailRecipient) {
            // optionally generate alternate email body (usually as plain text)
            final MediaData alternateBody = docConfigDto.getAlternateTemplateId() != null
              ? docFormatter.formatDocument(
                    ctx.tenantId,
                    sharedTenantId,
                    TemplateType.DOCUMENT_ID, docConfigDto.getAlternateTemplateId(),
                    request.getDocumentSelector(),
                    effectiveTimeZone,
                    request.getData(),
                    null)
                : null;
            for (final Recipient rcpt : request.getRecipientList()) {
                if (rcpt instanceof RecipientEmail rcpe) {
                    emailRefs.add(docEmailDistributor.transmit((RecipientEmail)mergeEmailSettings(rcpe, docEmailReceiverDto),
                        toFormatConverter,
                        target.getMediaType(),
                        request.getDocumentId(), request.getDocumentSelector(),
                        emailSubject,
                        emailBody,
                        generatedCidMap,
                        alternateBody,
                        attachmentList,
                        docEmailReceiverDto.getStoreEmail(), docEmailReceiverDto.getSendSpooled(), docEmailReceiverDto.getReturnPath()
                    ));
                }
            }
        }

        final NewDocumentResponse response = new NewDocumentResponse();
        response.setGeneralizedAttachmentSinkRefs(generalAttachmentSinkRefs);
        response.setArchiveSinkRefs(sinkRefs);
        response.setData(datas);
        response.setEmailRefs(emailRefs);
        return response;
    }

    private Map<String, Long> addGeneralAttachments(RequestContext ctx, NewDocumentRequest request, RecipientArchive rcpta, List<MediaData> attachmentList,
            String sharedTenantId, String effectiveTimeZone) {
        final List<GeneralizedAttachment> attachments = request.getGeneralAttachments();
        if (attachments == null) {
            return null;
        }
        final Map<String, Long> generalAttachmentSinkRefs = new HashMap<>(FreezeTools.getInitialHashMapCapacity(attachments.size()));
        for (GeneralizedAttachment ga: attachments) {
            MediaData data = ga.getProvidedAttachment();
            if (data == null && ga.getDocumentId() != null) {
                // generate data from another template
                final DocConfigDTO docConfigDto = effectiveDocConfig(ga.getDocumentId());
                if (docConfigDto == null) {
                    // this case is not a problem but a regular configuration case, therefore it should log at debug level max.
                    LOGGER.debug("Attachment templateId [{}] for {} has not been mapped to a document. Attachment will be skipped",
                      ga.getDocumentId(), request.getDocumentId());
                } else {
                    // generate the attachment data based on the separate document config
                    data = docFormatter.formatDocument(ctx.tenantId, sharedTenantId, TemplateType.DOCUMENT_ID, docConfigDto.getDocumentId(),
                      request.getDocumentSelector(), effectiveTimeZone, nvl(ga.getData(), request.getData()), null);
                    // convert if required
                    data = this.convertDocument(docConfigDto, data);
                }
            }

            if (data != null) {
                // check if we should store it
                if (ga.getDataSinkId() != null) {
                    // store it (recipient archive)
                    RecipientArchive gaRcpta = rcpta.ret$MutableClone(true, false);
                    gaRcpta.setDataSinkId(ga.getDataSinkId()); // overwrite dataSinkId to use from GA
                    final MediaData finalData = data;
                    final DocArchiveResult archiveResult = docArchiveDistributor.transmit(gaRcpta, x -> finalData, data.getMediaType(), ga.getDocumentId(),
                            request.getDocumentSelector());
                    generalAttachmentSinkRefs.put(ga.getId(), archiveResult.sinkRef());
                    this.prepareAttachmentName(finalData, archiveResult);
                }
                // now add it as attachment
                attachmentList.add(data);
            }
        }
        return generalAttachmentSinkRefs;
    }

    /**
     * Prepare the name for the file/attachment and add it to the zMap. This will be read by {@link IDocEmailDistributor} later.
     *
     * @param mediaData the attachment object
     * @param archiveResult the result of distributor transmit
     */
    private void prepareAttachmentName(final MediaData mediaData, final DocArchiveResult archiveResult) {
        if (mediaData != null && archiveResult.relativePath() != null) {
            final String attachmentFileName = this.getBasename(archiveResult.relativePath());
            LOGGER.debug("Setting name of attachment of media type {} to {}", mediaData.getMediaType(), attachmentFileName);
            if (mediaData.getZ() == null) {
                mediaData.setZ(new HashMap<String, Object>());
            }
            mediaData.getZ().put(T9tConstants.DOC_MEDIA_ATTACHMENT_NAME, attachmentFileName);
        }
    }

    private String getBasename(final String filename) {
        final int i = filename.lastIndexOf("/");
        return i < 0 ? filename : filename.substring(i + 1);
    }

    private MediaData convertDocument(final DocConfigDTO docConfigDto, final MediaData data) {
        if (docConfigDto.getCommunicationFormat() != null && !docConfigDto.getCommunicationFormat().equals(data.getMediaType())) {
            LOGGER.debug("Formatted document to {}, now converting to {}", data.getMediaType(), docConfigDto.getCommunicationFormat());
            final IDocConverter myConverter = Jdp.getOptional(IDocConverter.class, docConfigDto.getCommunicationFormat().name());
            if (myConverter == null) {
                throw new T9tException(T9tException.FIELD_MAY_NOT_BE_CHANGED, "Cannot convert to " + docConfigDto.getCommunicationFormat().name());
            }
            return myConverter.convert(data);
        }
        return data;
    }
}
