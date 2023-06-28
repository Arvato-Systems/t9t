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
package com.arvatosystems.t9t.email.be.commons.impl;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.email.EmailModuleCfgDTO;
import com.arvatosystems.t9t.email.T9tEmailException;
import com.arvatosystems.t9t.email.api.EmailMessage;
import com.arvatosystems.t9t.email.api.RecipientEmail;
import com.arvatosystems.t9t.email.be.smtp.impl.MediaDataSource;
import com.arvatosystems.t9t.email.services.IEmailSender;

import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.mail.Authenticator;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Implementation of IEmailSender using the Apache Commons-Email library (which is a wrapper around jakarta.mail). */
@Singleton
@Named("COMMONS")
public class CommonsEmailService implements IEmailSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonsEmailService.class);

    protected static final String STANDARD_ENCODING     = "UTF-8";
    protected static final String DEFAULT_SMTP_SERVER   = "cmail.servicemail24.de"; // "smtp.gmail.com"

    @Override
    public ServiceResponse sendEmail(final Long messageRef, final UUID messageId, final EmailMessage msg, final EmailModuleCfgDTO configuration) {
        LOGGER.info("Sending email ref {}, ID {} to {} using server {} via COMMONS library", messageRef, messageId, msg.getRecipient().getTo().get(0),
                configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());

        final RecipientEmail recipient = msg.getRecipient();
        final Email email = composeMessage(recipient, msg);
        createSession(email, configuration);
        try {
            if (msg.getMailSubject() != null) {
                email.setSubject(msg.getMailSubject());
            }
            if (recipient.getFrom() != null) {
                email.setFrom(recipient.getFrom());
            }
            if (recipient.getReplyTo() != null) {
                email.addReplyTo(recipient.getReplyTo());
            }
            if (recipient.getTo() != null) {
                for (final String to: recipient.getTo()) {
                    email.addTo(to);
                }
            }
            if (recipient.getCc() != null) {
                for (final String cc: recipient.getCc()) {
                    email.addCc(cc);
                }
            }
            if (recipient.getBcc() != null) {
                for (final String bcc: recipient.getBcc()) {
                    email.addBcc(bcc);
                }
            }

            email.send();
        } catch (final EmailException e) {
            LOGGER.error("Failed to send email problem: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tEmailException.EMAIL_SEND_ERROR, e.getClass().getSimpleName() + ": " + e.getMessage());
        }

        final ServiceResponse okResponse = new ServiceResponse();
        return okResponse;
    }

    protected void createSession(final Email email, final EmailModuleCfgDTO configuration) {
        final int port;
        if (configuration.getSmtpServerPort() == null) {
            if (configuration.getSmtpServerTls()) {
                port = 587;
            } else {
                port = 25;
            }
        } else {
            port = configuration.getSmtpServerPort();
        }

        if (Boolean.TRUE.equals(configuration.getSmtpServerTls())) {
            email.setSSLOnConnect(true);
        }
        email.setHostName(configuration.getSmtpServerAddress() == null ? DEFAULT_SMTP_SERVER : configuration.getSmtpServerAddress());
        email.setSmtpPort(port);

        if (configuration.getSmtpServerPassword() != null && configuration.getSmtpServerUserId() != null) {
            // authenticated access
            final Authenticator defaultAuthenticator = new DefaultAuthenticator(configuration.getSmtpServerUserId(), configuration.getSmtpServerPassword());
            email.setAuthenticator(defaultAuthenticator);
        }
    }

    private Email composeMessage(final RecipientEmail recipient, final EmailMessage msg) {
        try {
            if (msg.getAttachments() == null || msg.getAttachments().isEmpty() && msg.getMailBody().getMediaType() == MediaTypes.MEDIA_XTYPE_TEXT) {
                // simple email - shortcut for special case
                final Email email = new SimpleEmail();
                email.setCharset(STANDARD_ENCODING);
                email.setMsg(msg.getMailBody().getText());
                return email;
            } else {
                final HtmlEmail email = new HtmlEmail();
                email.setCharset(STANDARD_ENCODING);
                email.setHtmlMsg(msg.getMailBody().getText());
                if (msg.getAlternateBody() != null) {
                    email.setTextMsg(msg.getAlternateBody().getText());
                }
                if (msg.getAttachments() != null) {
                    addAttachments(email, msg.getAttachments());
                }
                if (msg.getCids() != null) {
                    addCids(email, msg.getCids());
                }
                return email;
            }
        } catch (final EmailException e) {
            LOGGER.error("SMTP message composition problem: {}: {}", e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tEmailException.MIME_MESSAGE_COMPOSITION_PROBLEM, e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    // add attachments to a multipart email
    protected void addAttachments(final MultiPartEmail email, final List<MediaData> attachments) throws EmailException {
        for (final MediaData attachment: attachments) {
            final MediaDataSource mds = new MediaDataSource(attachment);
            final String fileName;
            if (attachment.getZ() != null && attachment.getZ().containsKey(T9tConstants.DOC_MEDIA_ATTACHMENT_NAME)) {
                fileName = attachment.getZ().get(T9tConstants.DOC_MEDIA_ATTACHMENT_NAME).toString();
            } else {
                fileName = null;
            }
            email.attach(mds, fileName, fileName);
        }
    }

    // add CIDs to an HTML email
    protected void addCids(final HtmlEmail email, final Map<String, MediaData> cids) throws EmailException {
        for (final MediaData cidsValue: cids.values()) {
            final MediaDataSource mds = new MediaDataSource(cidsValue);
            email.embed(mds, mds.getName(), mds.getName());
        }
    }

}
