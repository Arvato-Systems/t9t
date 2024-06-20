/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.email.be.vertx.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.services.IEmailSender;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailAttachment;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.MailResult;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.mail.impl.MailAttachmentImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of IEmailSender using the vert.x client. This is an
 * asynchronous implementation.
 */
@Singleton
@Named("VERTX")
public class VertxEmailService implements IEmailSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(VertxEmailService.class);

    protected static final String STANDARD_ENCODING = "UTF-8";
    protected static final String DEFAULT_SMTP_SERVER = "cmail.servicemail24.de"; // "smtp.gmail.com")

    @Override
    public ServiceResponse sendEmail(final Long messageRef, final UUID messageId, final EmailMessage msg, final EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via vert.x implementation", messageRef, messageId, msg.getRecipient().getTo().get(0),
                configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());

        final RecipientEmail recipient = msg.getRecipient();
        final MailMessage emailMessage = new MailMessage();
        if (msg.getMailSubject() != null) {
            emailMessage.setSubject(msg.getMailSubject());
        }

        if (recipient.getFrom() != null) {
            emailMessage.setFrom(recipient.getFrom());
        }

        if (recipient.getReplyTo() != null) {
            emailMessage.addHeader("Reply-To", recipient.getReplyTo());
        }

        if (recipient.getTo() != null) {
            emailMessage.setTo(recipient.getTo());
        }

        if (recipient.getCc() != null) {
            emailMessage.setCc(recipient.getCc());
        }

        if (recipient.getBcc() != null) {
            emailMessage.setBcc(recipient.getBcc());
        }

        final String returnPath = msg.getReturnPath() != null ? msg.getReturnPath() : configuration.getDefaultReturnPath();
        emailMessage.setBounceAddress(returnPath);

        // set mail body
        if (MediaTypes.MEDIA_XTYPE_TEXT.equals(msg.getMailBody().getMediaType())) {
            emailMessage.setText(msg.getMailBody().getText());
        }
        if (MediaTypes.MEDIA_XTYPE_HTML.equals(msg.getMailBody().getMediaType()) || MediaTypes.MEDIA_XTYPE_XHTML.equals(msg.getMailBody().getMediaType())) {
            emailMessage.setHtml(msg.getMailBody().getText());
            if (msg.getAlternateBody() != null && MediaTypes.MEDIA_XTYPE_TEXT.equals(msg.getAlternateBody().getMediaType()) && msg.getAlternateBody() != null) {
                emailMessage.setText(msg.getAlternateBody().getText());
            }
        }

        // set attachments
        if (msg.getCids() != null && !msg.getCids().isEmpty()) {
            emailMessage.setInlineAttachment(toVertx(msg.getCids()));
        }

        if (msg.getAttachments() != null || !msg.getAttachments().isEmpty()) {
            emailMessage.setAttachment(toVertx(msg.getAttachments()));
        }

        final MailClient mailClient = createClient(configuration);
        mailClient.sendMail(emailMessage, (AsyncResult<MailResult> result) -> {
            if (result.succeeded()) {
                LOGGER.debug("Email of ref {}, UUID {} has been sent, result = {}", messageRef, messageId, result.result());
            } else {
                LOGGER.error("Email of ref {}, UUID {} could not be sent, cause = {}", messageRef, messageId, ExceptionUtil.causeChain(result.cause()));
            }
        });

        return null;
    }

    protected MailClient createClient(final EmailModuleCfgDTO configuration) {
        final String clientPoolId = Integer.toString(configuration.hashCode());
        final MailConfig config = new MailConfig();
        config.setHostname(configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());
        config.setPort(configuration.getSmtpServerPort() == null ? (configuration.getSmtpServerTls() ? 587 : 25) : configuration.getSmtpServerPort());
        if (Boolean.TRUE.equals(configuration.getSmtpServerTls())) {
            config.setStarttls(StartTLSOptions.REQUIRED);
        }
        if (configuration.getSmtpServerUserId() != null && !configuration.getSmtpServerUserId().isEmpty()) {
            config.setUsername(configuration.getSmtpServerUserId());
        }
        if (configuration.getSmtpServerPassword() != null && !configuration.getSmtpServerPassword().isEmpty()) {
            config.setPassword(configuration.getSmtpServerPassword());
        }
        config.setMaxPoolSize(4); // default is 10 which may be a bit too much
        final Vertx vertx = Jdp.getRequired(Vertx.class);
        return MailClient.createShared(vertx, config, clientPoolId);
    }

    // add attachments to the message - the list of attachments is not empty
    protected List<MailAttachment> toVertx(final List<MediaData> attachments) {
        final List<MailAttachment> result = new ArrayList<>(attachments.size());
        for (MediaData attachment : attachments) {
            final MediaDataSource mds = new MediaDataSource(attachment);
            final String fileName = attachment.getZ() == null ? null : (String) attachment.getZ().get(T9tConstants.DOC_MEDIA_ATTACHMENT_NAME);
            final MailAttachmentImpl mailAttachmentImpl = new MailAttachmentImpl();
            mailAttachmentImpl.setContentType(mds.getContentType());
            mailAttachmentImpl.setData(mds.asBuffer());
            if (fileName != null) {
                mailAttachmentImpl.setName(fileName);
            }
            result.add(mailAttachmentImpl);
        }
        return result;
    }

    // add CIDs to an HTML email
    // add inline attachments to the message - the list of attachments is not empty
    protected List<MailAttachment> toVertx(final Map<String, MediaData> cids) {
        final List<MailAttachment> result = new ArrayList<MailAttachment>(cids.size());
        for (MediaData md : cids.values()) {
            final MediaDataSource mds = new MediaDataSource(md);
            final MailAttachmentImpl mai = new MailAttachmentImpl();
            mai.setContentType(mds.getContentType());
            mai.setData(mds.asBuffer());
            mai.setContentId(mds.getName());
            mai.setDisposition("inline");
            result.add(mai);
        }
        return result;
    }
}
